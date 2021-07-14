package com.github.twitch4j.chat;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.chat.enums.CommandSource;
import com.github.twitch4j.chat.enums.TMIConnectionState;
import com.github.twitch4j.chat.events.CommandEvent;
import com.github.twitch4j.chat.events.IRCEventHandler;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.common.config.ProxyConfig;
import com.github.twitch4j.common.util.CryptoUtils;
import com.github.twitch4j.common.util.EscapeUtils;
import com.github.twitch4j.common.util.ExponentialBackoffStrategy;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import io.github.bucket4j.Bucket;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class TwitchChat implements ITwitchChat {

    public static final int REQUIRED_THREAD_COUNT = 2;

    /**
     * EventManager
     */
    @Getter
    private final EventManager eventManager;

    /**
     * CredentialManager
     */
    @Getter
    private final CredentialManager credentialManager;

    /**
     * OAuth2Credential, used to sign in to twitch chat
     */
    private OAuth2Credential chatCredential;

    /**
     * Twitch's official WebSocket Server
     */
    public static final String TWITCH_WEB_SOCKET_SERVER = "wss://irc-ws.chat.twitch.tv:443";

    /**
     * ThirdParty WebSocket Server for Testing
     */
    public static final String FDGT_TEST_SOCKET_SERVER = "wss://irc.fdgt.dev";

    /**
     * The websocket url for the chat client to connect to.
     */
    protected final String baseUrl;

    /**
     * Whether the {@link OAuth2Credential} password should be sent when the baseUrl does not
     * match the official twitch websocket server, thus bypassing a security check in the library.
     */
    protected final boolean sendCredentialToThirdPartyHost;

    /**
     * WebSocket Client
     */
    private volatile WebSocket webSocket;

    /**
     * The connection state
     * Default: ({@link TMIConnectionState#DISCONNECTED})
     */
    @Getter
    private volatile TMIConnectionState connectionState = TMIConnectionState.DISCONNECTED;

    /**
     * Channel Cache Lock
     */
    private final ReentrantLock channelCacheLock = new ReentrantLock();

    /**
     * Current Channels
     */
    protected final Set<String> currentChannels = ConcurrentHashMap.newKeySet();

    /**
     * Cache: ChannelId to ChannelName
     */
    protected final Map<String, String> channelIdToChannelName = new ConcurrentHashMap<>();

    /**
     * Cache: ChannelName to ChannelId
     */
    protected final Map<String, String> channelNameToChannelId = new ConcurrentHashMap<>();

    /**
     * IRC Message Bucket
     */
    protected final Bucket ircMessageBucket;

    /**
     * IRC Whisper Bucket
     */
    protected final Bucket ircWhisperBucket;

    /**
     * IRC Join Bucket
     */
    protected final Bucket ircJoinBucket;

    /**
     * IRC Command Queue
     */
    protected final BlockingQueue<String> ircCommandQueue;

    /**
     * IRC Command Queue Thread
     */
    protected final ScheduledFuture<?> queueThread;

    /**
     * Whether {@link #flushCommand} is currently executing
     */
    private final AtomicBoolean flushing = new AtomicBoolean();

    /**
     * Whether an expedited flush has already been submitted
     */
    private final AtomicBoolean flushRequested = new AtomicBoolean();

    /**
     * The {@link Runnable} for flushing the {@link #ircCommandQueue}
     */
    private final Runnable flushCommand;

    /**
     * Command Queue Thread stop flag
     */
    protected volatile boolean stopQueueThread = false;

    /**
     * Bot Owner IDs
     */
    protected final Collection<String> botOwnerIds;

    /**
     * IRC Command Handlers
     */
    protected final List<String> commandPrefixes;

    /**
     * Thread Pool Executor
     */
    protected final ScheduledExecutorService taskExecutor;

    /**
     * Time to wait for an item on the chat queue before continuing to next iteration
     * If set too high your thread will be late check to shutdown
     */
    protected final long chatQueueTimeout;

    /**
     * WebSocket Factory
     */
    protected final WebSocketFactory webSocketFactory;

    /**
     * Whether one's own channel should automatically be joined
     */
    protected final boolean autoJoinOwnChannel;

    /**
     * Whether JOIN/PART events should be enabled
     */
    protected final boolean enableMembershipEvents;

    /**
     * Helper class to compute delays between connection retries.
     *
     * @see <a href="https://dev.twitch.tv/docs/irc/guide#re-connecting-to-twitch-irc">Official suggestion</a>
     */
    protected final ExponentialBackoffStrategy backoff = ExponentialBackoffStrategy.builder()
        .immediateFirst(true)
        .baseMillis(Duration.ofSeconds(1).toMillis())
        .jitter(true)
        .multiplier(2.0)
        .maximumBackoff(Duration.ofMinutes(5).toMillis())
        .build();

    /**
     * Calls {@link ExponentialBackoffStrategy#reset()} upon a successful websocket connection
     */
    private volatile Future<?> backoffClearer;

    /**
     * Constructor
     *
     * @param eventManager                   EventManager
     * @param credentialManager              CredentialManager
     * @param chatCredential                 Chat Credential
     * @param baseUrl                        The websocket url for the chat client to connect to
     * @param sendCredentialToThirdPartyHost Whether the password should be sent when the baseUrl is not official
     * @param commandPrefixes                Command Prefixes
     * @param chatQueueSize                  Chat Queue Size
     * @param ircMessageBucket               Bucket for chat
     * @param ircWhisperBucket               Bucket for whispers
     * @param ircJoinBucket                  Bucket for joins
     * @param taskExecutor                   ScheduledThreadPoolExecutor
     * @param chatQueueTimeout               Timeout to wait for events in Chat Queue
     * @param proxyConfig                    Proxy Configuration
     * @param autoJoinOwnChannel             Whether one's own channel should automatically be joined
     * @param enableMembershipEvents         Whether JOIN/PART events should be enabled
     * @param botOwnerIds                    Bot Owner IDs
     */
    public TwitchChat(EventManager eventManager, CredentialManager credentialManager, OAuth2Credential chatCredential, String baseUrl, boolean sendCredentialToThirdPartyHost, List<String> commandPrefixes, Integer chatQueueSize, Bucket ircMessageBucket, Bucket ircWhisperBucket, Bucket ircJoinBucket, ScheduledThreadPoolExecutor taskExecutor, long chatQueueTimeout, ProxyConfig proxyConfig, boolean autoJoinOwnChannel, boolean enableMembershipEvents, Collection<String> botOwnerIds) {
        this.eventManager = eventManager;
        this.credentialManager = credentialManager;
        this.chatCredential = chatCredential;
        this.baseUrl = baseUrl;
        this.sendCredentialToThirdPartyHost = sendCredentialToThirdPartyHost;
        this.commandPrefixes = commandPrefixes;
        this.botOwnerIds = botOwnerIds;
        this.ircCommandQueue = new ArrayBlockingQueue<>(chatQueueSize, true);
        this.ircMessageBucket = ircMessageBucket;
        this.ircWhisperBucket = ircWhisperBucket;
        this.ircJoinBucket = ircJoinBucket;
        this.taskExecutor = taskExecutor;
        this.chatQueueTimeout = chatQueueTimeout;
        this.autoJoinOwnChannel = autoJoinOwnChannel;
        this.enableMembershipEvents = enableMembershipEvents;

        // Create WebSocketFactory and apply proxy settings
        this.webSocketFactory = new WebSocketFactory();
        if (proxyConfig != null)
            proxyConfig.applyWs(webSocketFactory.getProxySettings());

        // credential validation
        if (this.chatCredential == null) {
            log.info("TwitchChat: No ChatAccount provided, Chat will be joined anonymously! Please look at the docs Twitch4J -> Chat if this is unintentional");
        } else if (this.chatCredential.getUserName() == null) {
            log.info("TwitchChat: AccessToken does not contain any user information, fetching using the CredentialManager ...");

            // credential manager
            Optional<OAuth2Credential> credential = credentialManager.getOAuth2IdentityProviderByName("twitch")
                .orElse(new TwitchIdentityProvider(null, null, null))
                .getAdditionalCredentialInformation(this.chatCredential);
            if (credential.isPresent()) {
                this.chatCredential = credential.get();
            } else {
                log.error("TwitchChat: Failed to get AccessToken Information, the token is probably not valid. Please check the docs Twitch4J -> Chat on how to obtain a valid token.");
            }
        }

        // register with serviceMediator
        this.eventManager.getServiceMediator().addService("twitch4j-chat", this);

        // register event listeners
        IRCEventHandler ircEventHandler = new IRCEventHandler(this);

        // connect to irc
        this.connect();

        // queue command worker
        this.flushCommand = () -> {
            if (flushing.getAndSet(true)) return;

            while (!stopQueueThread && connectionState == TMIConnectionState.CONNECTED) {
                String command = null;
                try {
                    // Send the command
                    command = ircCommandQueue.poll(this.chatQueueTimeout, TimeUnit.MILLISECONDS);
                    if (command == null) break;
                    sendTextToWebSocket(command, false);

                    // Logging
                    log.debug("Processed command from queue: [{}].", command.startsWith("PASS") ? "***OAUTH TOKEN HIDDEN***" : command);
                } catch (Exception ex) {
                    log.error("Chat: Unexpected error in worker thread", ex);

                    // Reschedule command for processing
                    if (command != null) {
                        try {
                            ircCommandQueue.offer(command, this.chatQueueTimeout, TimeUnit.MILLISECONDS);
                        } catch (Exception e) {
                            log.error("Failed to reschedule command", e);
                        }
                    }

                    break;
                }
            }

            flushRequested.set(false);
            flushing.set(false);
        };

        // Thread will start right now
        this.queueThread = taskExecutor.scheduleAtFixedRate(flushCommand, 0, this.chatQueueTimeout, TimeUnit.MILLISECONDS);
        log.debug("Started IRC Queue Worker");

        // Event Handlers
        log.debug("Registering the following command triggers: " + commandPrefixes.toString());

        // register event handler
        eventManager.onEvent("twitch4j-chat-command-trigger", ChannelMessageEvent.class, this::onChannelMessage);
        eventManager.onEvent(IRCMessageEvent.class, event -> {
            // we get at least one room state event with channel name + id when we join a channel, so we cache that to provide channel id + name for all events
            if ("ROOMSTATE".equalsIgnoreCase(event.getCommandType())) {
                // check that channel id / name are present and that we didn't leave the channel yet
                if (event.getChannelId() != null) {
                    channelCacheLock.lock();
                    try {
                        // store mapping info into channelIdToChannelName / channelNameToChannelId
                        event.getChannelName().map(String::toLowerCase).filter(currentChannels::contains).ifPresent(name -> {
                            String oldName = channelIdToChannelName.put(event.getChannelId(), name);
                            if (!name.equals(oldName)) {
                                if (oldName != null) channelNameToChannelId.remove(oldName, event.getChannelId());
                                channelNameToChannelId.put(name, event.getChannelId());
                            }
                        });
                    } finally {
                        channelCacheLock.unlock();
                    }
                }
            }
        });
    }

    /**
     * Connecting to IRC-WS
     */
    @Synchronized
    public void connect() {
        if (connectionState.equals(TMIConnectionState.DISCONNECTED) || connectionState.equals(TMIConnectionState.RECONNECTING)) {
            try {
                // Change Connection State
                connectionState = TMIConnectionState.CONNECTING;

                // Recreate Socket if state does not equal CREATED
                createWebSocket();

                // Connect to IRC WebSocket
                this.webSocket.connect();
            } catch (Exception ex) {
                log.error("Connection to Twitch IRC failed: Retrying ...", ex);
                // Sleep before trying to reconnect
                try {
                    backoff.sleep();
                } catch (Exception ignored) {

                } finally {
                    // reconnect
                    reconnect();
                }
            }
        }
    }

    /**
     * Disconnecting from IRC-WS
     */
    @Synchronized
    public void disconnect() {
        if (connectionState.equals(TMIConnectionState.CONNECTED)) {
            sendTextToWebSocket("QUIT", true); // safe disconnect
            connectionState = TMIConnectionState.DISCONNECTING;
        }

        connectionState = TMIConnectionState.DISCONNECTED;

        // CleanUp
        this.webSocket.clearListeners();
        this.webSocket.disconnect();
        this.webSocket = null;
    }

    /**
     * Reconnecting to IRC-WS
     */
    @Synchronized
    public void reconnect() {
        connectionState = TMIConnectionState.RECONNECTING;
        disconnect();
        connect();
    }

    /**
     * Recreate the WebSocket and the listeners
     */
    @Synchronized
    private void createWebSocket() {
        try {
            // WebSocket
            this.webSocket = webSocketFactory.createSocket(this.baseUrl);

            // WebSocket Listeners
            this.webSocket.clearListeners();
            this.webSocket.addListener(new WebSocketAdapter() {

                @Override
                public void onConnected(WebSocket ws, Map<String, List<String>> headers) {
                    log.info("Connecting to Twitch IRC {}", baseUrl);

                    // acquire capabilities
                    sendTextToWebSocket("CAP REQ :twitch.tv/tags twitch.tv/commands" + (enableMembershipEvents ? " twitch.tv/membership" : ""), true);
                    sendTextToWebSocket("CAP END", true);

                    // sign in
                    String userName;
                    if (chatCredential != null) {
                        boolean sendRealPass = sendCredentialToThirdPartyHost // check whether this security feature has been overridden
                            || baseUrl.equalsIgnoreCase(TWITCH_WEB_SOCKET_SERVER) // check whether the url is exactly the official one
                            || baseUrl.equalsIgnoreCase(TWITCH_WEB_SOCKET_SERVER.substring(0, TWITCH_WEB_SOCKET_SERVER.length() - 4)); // check whether the url matches without the port
                        sendTextToWebSocket(String.format("pass oauth:%s", sendRealPass ? chatCredential.getAccessToken() : CryptoUtils.generateNonce(30)), true);
                        userName = String.valueOf(chatCredential.getUserName()).toLowerCase();
                    } else {
                        userName = "justinfan" + ThreadLocalRandom.current().nextInt(100000);
                    }
                    sendTextToWebSocket(String.format("nick %s", userName), true);

                    // Join defined channels, in case we reconnect or weren't connected yet when we called joinChannel
                    for (String channel : currentChannels) {
                        issueJoin(channel);
                    }

                    // then join to own channel - required for sending or receiving whispers
                    if (chatCredential != null && chatCredential.getUserName() != null) {
                        if (autoJoinOwnChannel && !currentChannels.contains(userName))
                            joinChannel(userName);
                    } else {
                        log.warn("Chat: The whispers feature is currently not available because the provided credential does not hold information about the user. Please check the documentation on how to pass the token to the credentialManager where it will be enriched with the required information.");
                    }

                    // Connection Success
                    connectionState = TMIConnectionState.CONNECTED;
                    backoffClearer = taskExecutor.schedule(() -> {
                        if (connectionState == TMIConnectionState.CONNECTED)
                            backoff.reset();
                    }, 30, TimeUnit.SECONDS);
                }

                @Override
                public void onTextMessage(WebSocket ws, String text) {
                    Arrays.asList(text.replace("\n\r", "\n")
                        .replace("\r", "\n").split("\n"))
                        .forEach(message -> {
                            if (!message.equals("")) {
                                // Handle messages
                                log.trace("Received WebSocketMessage: " + message);
                                // - CAP
                                if (message.contains(":req Invalid CAP command")) {
                                    log.error("Failed to acquire requested IRC capabilities!");
                                }
                                // - CAP ACK
                                else if (message.contains(":tmi.twitch.tv CAP * ACK :")) {
                                    List<String> capabilities = Arrays.asList(message.replace(":tmi.twitch.tv CAP * ACK :", "").split(" "));
                                    capabilities.forEach(cap -> log.debug("Acquired chat capability: " + cap));
                                }
                                // - Ping
                                else if (message.contains("PING :tmi.twitch.tv")) {
                                    sendTextToWebSocket("PONG :tmi.twitch.tv", true);
                                    log.debug("Responding to PING request!");
                                }
                                // - Login failed.
                                else if (message.equals(":tmi.twitch.tv NOTICE * :Login authentication failed")) {
                                    log.error("Invalid IRC Credentials. Login failed!");
                                }
                                // - Parse IRC Message
                                else {
                                    try {
                                        IRCMessageEvent event = new IRCMessageEvent(message, channelIdToChannelName, channelNameToChannelId, botOwnerIds);

                                        if (event.isValid()) {
                                            eventManager.publish(event);
                                        } else {
                                            log.trace("Can't parse {}", event.getRawMessage());
                                        }
                                    } catch (Exception ex) {
                                        log.error(ex.getMessage(), ex);
                                    }
                                }
                            }
                        });
                }

                @Override
                public void onDisconnected(WebSocket websocket,
                                           WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
                                           boolean closedByServer) {
                    if (!connectionState.equals(TMIConnectionState.DISCONNECTING)) {
                        log.info("Connection to Twitch IRC lost (WebSocket)! Retrying soon ...");

                        // connection lost - reconnecting
                        if (backoffClearer != null) backoffClearer.cancel(false);
                        taskExecutor.schedule(() -> reconnect(), backoff.get(), TimeUnit.MILLISECONDS);
                    } else {
                        connectionState = TMIConnectionState.DISCONNECTED;
                        log.info("Disconnected from Twitch IRC (WebSocket)!");
                    }
                }

            });

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * Send IRC Command
     *
     * @param command IRC Command
     * @param args    command arguments
     */
    protected void sendCommand(String command, String... args) {
        sendRaw(String.format("%s %s", command.toUpperCase(), String.join(" ", args)));
    }

    /**
     * Send raw irc command
     *
     * @param command raw irc command
     */
    public boolean sendRaw(String command) {
        return ircMessageBucket.asAsyncScheduler().consume(1, taskExecutor).thenRunAsync(() -> queueCommand(command), taskExecutor) != null;
    }

    /**
     * Adds a raw irc command to the queue without checking bucket headroom.
     *
     * @param command Raw IRC command to be queued.
     */
    private void queueCommand(String command) {
        // Add command to the queue, waiting for a period of time if necessary
        if (!ircCommandQueue.offer(command)) {
            try {
                ircCommandQueue.offer(command, chatQueueTimeout, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.warn("Chat: unable to add command to full queue", e);
                return;
            }
        }

        // Expedite command execution if we aren't already flushing the queue and another expedition hasn't already been requested
        if (!flushing.get() && !flushRequested.getAndSet(true))
            taskExecutor.schedule(flushCommand, chatQueueTimeout / 20, TimeUnit.MILLISECONDS); // allow for some accumulation of requests before flushing
    }

    /**
     * Send IRC Command (for Login/...)
     * <p>
     * Sends important irc commands for login / capabilities and similar.
     * Will consume tokens to respect the ratelimit, but will bypass the limit if the bucket is empty.
     *
     * @param command      IRC Command
     * @param consumeToken should a token be consumed when sending this text?
     */
    private boolean sendTextToWebSocket(String command, Boolean consumeToken) {
        // will send text only if CONNECTED or CONNECTING
        if (!connectionState.equals(TMIConnectionState.CONNECTED) && !connectionState.equals(TMIConnectionState.CONNECTING)) {
            return false;
        }

        // consume tokens if available, but ignore if not as those are important system commands (CAP, Login, ...)
        if (consumeToken)
            ircMessageBucket.tryConsume(1L);

        // command will be uppercase.
        this.webSocket.sendText(command);

        return true;
    }

    /**
     * Joining the channel
     *
     * @param channelName channel name
     */
    @Override
    public void joinChannel(String channelName) {
        String lowerChannelName = channelName.toLowerCase();

        channelCacheLock.lock();
        try {
            if (currentChannels.add(lowerChannelName)) {
                issueJoin(lowerChannelName);
                log.debug("Joining Channel [{}].", lowerChannelName);
            } else {
                log.warn("Already joined channel {}", channelName);
            }
        } finally {
            channelCacheLock.unlock();
        }
    }

    private void issueJoin(String channelName) {
        ircJoinBucket.asAsyncScheduler().consume(1, taskExecutor).thenRunAsync(
            () -> queueCommand("JOIN #" + channelName.toLowerCase()),
            taskExecutor
        );
    }

    /**
     * leaving the channel
     *
     * @param channelName channel name
     */
    @Override
    public boolean leaveChannel(String channelName) {
        String lowerChannelName = channelName.toLowerCase();

        channelCacheLock.lock();
        try {
            if (currentChannels.remove(lowerChannelName)) {
                issuePart(lowerChannelName);
                log.debug("Leaving Channel [{}].", lowerChannelName);

                // clear cache
                String cachedId = channelNameToChannelId.remove(lowerChannelName);
                if (cachedId != null) channelIdToChannelName.remove(cachedId);

                return true;
            } else {
                log.warn("Already left channel {}", channelName);
                return false;
            }
        } finally {
            channelCacheLock.unlock();
        }
    }

    private void issuePart(String channelName) {
        ircJoinBucket.asAsyncScheduler().consume(1, taskExecutor).thenRunAsync(
            () -> queueCommand("PART #" + channelName.toLowerCase()),
            taskExecutor
        );
    }

    @Override
    public boolean sendMessage(String channel, String message, Map<String, Object> tags) {
        StringBuilder sb = new StringBuilder();
        if (tags != null && !tags.isEmpty()) {
            sb.append('@');
            tags.forEach((k, v) -> sb.append(k).append('=').append(EscapeUtils.escapeTagValue(v)).append(';'));
            sb.setCharAt(sb.length() - 1, ' '); // replace last semi-colon with space
        }
        sb.append("PRIVMSG #").append(channel.toLowerCase()).append(" :").append(message);

        log.debug("Adding message for channel [{}] with content [{}] to the queue.", channel.toLowerCase(), message);
        return sendRaw(sb.toString());
    }

    /**
     * Sends a user a private message
     *
     * @param targetUser username
     * @param message    message
     */
    public void sendPrivateMessage(String targetUser, String message) {
        log.debug("Adding private message for user [{}] with content [{}] to the queue.", targetUser, message);
        ircWhisperBucket.asAsyncScheduler().consume(1, taskExecutor).thenRunAsync(
            () -> queueCommand(String.format("PRIVMSG #%s :/w %s %s", chatCredential.getUserName().toLowerCase(), targetUser, message)),
            taskExecutor
        );
    }

    /**
     * On Channel Message
     *
     * @param event ChannelMessageEvent
     */
    private void onChannelMessage(ChannelMessageEvent event) {
        Optional<String> prefix = Optional.empty();
        Optional<String> commandWithoutPrefix = Optional.empty();

        // try to find a `command` based on the prefix
        for (String commandPrefix : this.commandPrefixes) {
            if (event.getMessage().startsWith(commandPrefix)) {
                prefix = Optional.of(commandPrefix);
                commandWithoutPrefix = Optional.of(event.getMessage().substring(commandPrefix.length()));
                break;
            }
        }

        // is command?
        if (commandWithoutPrefix.isPresent()) {
            log.debug("Detected a command in channel {} with content: {}", event.getChannel().getName(), commandWithoutPrefix.get());

            // dispatch command event
            eventManager.publish(new CommandEvent(CommandSource.CHANNEL, event.getChannel().getName(), event.getUser(), prefix.get(), commandWithoutPrefix.get(), event.getPermissions()));
        }
    }

    /**
     * Close
     */
    @Override
    public void close() {
        this.stopQueueThread = true;
        queueThread.cancel(false);
        this.disconnect();
    }

    @Override
    public boolean isChannelJoined(String channelName) {
        return currentChannels.contains(channelName.toLowerCase());
    }

    /**
     * Returns a set of all currently joined channels (without # prefix)
     *
     * @return a set of channel names
     * @deprecated use getChannels() instead
     */
    @Deprecated
    public List<String> getCurrentChannels() {
        return Collections.unmodifiableList(new ArrayList<>(currentChannels));
    }

    @Override
    public Set<String> getChannels() {
        return Collections.unmodifiableSet(currentChannels);
    }

    /**
     * @return the cached map used for channel id to name mapping
     */
    @Override
    public Map<String, String> getChannelIdToChannelName() {
        return Collections.unmodifiableMap(channelIdToChannelName);
    }

    /**
     * @return the cached map used for channel name to id mapping
     */
    @Override
    public Map<String, String> getChannelNameToChannelId() {
        return Collections.unmodifiableMap(channelNameToChannelId);
    }

}
