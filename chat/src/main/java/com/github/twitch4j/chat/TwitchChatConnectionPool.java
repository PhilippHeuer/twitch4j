package com.github.twitch4j.chat;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.chat.events.channel.ChannelNoticeEvent;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.common.annotation.Unofficial;
import com.github.twitch4j.common.pool.TwitchModuleConnectionPool;
import com.github.twitch4j.common.util.ChatReply;
import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A pool for {@link TwitchChat} connections.
 * <p>
 * This pool is easiest to use for:
 * <ul>
 *     <li>Reading from many channels without an account</li>
 *     <li>Reading from many channels with an account</li>
 *     <li>Reading from many channels using valid proxies</li>
 *     <li>Reading and sending messages/whispers with only one account</li>
 * </ul>
 * Other applications are possible, but harder to configure. See below for a list of warnings.
 * <p>
 * Warning: If a custom {@link java.util.concurrent.ScheduledThreadPoolExecutor} is specified,
 * its corePoolSize must be large enough for the threads required by connections made by this class.
 * <p>
 * Warning: If a chatAccount is to be shared across multiple connections and used to send messages,
 * one should use advancedConfiguration to ensure the two are using a shared {@link io.github.bucket4j.Bucket}.
 * <p>
 * Note: If whispers are to be sent using this pool, one must manually join the channel to send the whisper from first.
 * If chatAccount's are dynamically supplied such that no two connections are using the same account, one can set
 * twitchChatBuilder.withAutoJoinOwnChannel(true) via advancedConfiguration to avoid the manual join.
 */
@SuperBuilder
public class TwitchChatConnectionPool extends TwitchModuleConnectionPool<TwitchChat, String, String, Boolean, TwitchChatBuilder> implements ITwitchChat {

    private final String threadPrefix = "twitch4j-pool-" + RandomStringUtils.random(4, true, true) + "-chat-";

    /**
     * Provides a chat account to be used when constructing a new {@link TwitchChat} instance.
     * By default, this yields null, which corresponds to an anonymous connection.
     */
    @NonNull
    @Builder.Default
    protected final Supplier<OAuth2Credential> chatAccount = () -> null;

    /**
     * Whether chat connections should automatically part from channels they have been banned from.
     * This is useful for reclaiming subscription headroom so a minimal number of chat instances are running.
     * By default false so that a chat instance can (eventually) reconnect if a unban occurs.
     */
    @Builder.Default
    protected final boolean automaticallyPartOnBan = false;

    @Override
    public boolean sendMessage(String channel, String message, @Nullable Map<String, Object> tags) {
        return this.sendMessage(channel, channel, message, tags);
    }

    /**
     * Sends a message from the {@link TwitchChat} identified either to a channel or directly on the socket.
     *
     * @param channelToIdentifyChatInstance the channel used to identify which {@link TwitchChat} instance should be used to send the message; the instance must be subscribed to this channel.
     * @param targetChannel                 the channel to send the message to, if not null (otherwise it is sent directly on the socket).
     * @param message                       the message to be sent.
     * @return whether a {@link TwitchChat} instance was found and used to send the message
     */
    public boolean sendMessage(final String channelToIdentifyChatInstance, final String targetChannel, final String message) {
        return this.sendMessage(channelToIdentifyChatInstance, targetChannel, message, Collections.emptyMap());
    }

    /**
     * Sends a message from the identified {@link TwitchChat} instance with an optional nonce or reply parent.
     *
     * @param channelToIdentifyChatInstance the channel used to identify which {@link TwitchChat} instance should be used to send the message; the instance must be subscribed to this channel.
     * @param targetChannel                 the channel to send the message to, if not null (otherwise it is sent directly on the socket).
     * @param message                       the message to be sent.
     * @param nonce                         the cryptographic nonce (optional).
     * @param replyMsgId                    the msgId of the parent message being replied to (optional).
     * @return whether a {@link TwitchChat} instance was found and used to send the message
     */
    public boolean sendMessage(final String channelToIdentifyChatInstance, final String targetChannel, final String message, @Unofficial final String nonce, final String replyMsgId) {
        final Map<String, Object> tags = new LinkedHashMap<>();
        if (nonce != null) tags.put(IRCMessageEvent.NONCE_TAG_NAME, nonce);
        if (replyMsgId != null) tags.put(ChatReply.REPLY_MSG_ID_TAG_NAME, replyMsgId);
        return this.sendMessage(channelToIdentifyChatInstance, targetChannel, message, tags);
    }

    /**
     * Sends a message from the identified {@link TwitchChat} instance with the specified tags.
     *
     * @param channelToIdentifyChatInstance the channel used to identify which {@link TwitchChat} instance should be used to send the message; the instance must be subscribed to this channel.
     * @param targetChannel                 the channel to send the message to, if not null (otherwise it is sent directly on the socket).
     * @param message                       the message to be sent.
     * @param tags                          the message tags.
     * @return whether a {@link TwitchChat} instance was found and used to send the message
     */
    public boolean sendMessage(String channelToIdentifyChatInstance, String targetChannel, String message, @Nullable Map<String, Object> tags) {
        if (channelToIdentifyChatInstance == null)
            return false;

        final TwitchChat chat = subscriptions.get(channelToIdentifyChatInstance.toLowerCase());
        if (chat == null)
            return false;

        if (targetChannel != null) {
            chat.sendMessage(targetChannel, message, tags);
        } else {
            chat.sendRaw(message);
        }

        return true;
    }

    /**
     * Sends a whisper.
     *
     * @param channelToIdentifyChatInstance the channel used to identify which {@link TwitchChat} instance should be used to send the message; the instance must be subscribed to this channel.
     * @param toChannel                     the channel to send the whisper to.
     * @param message                       the message to send in the whisper.
     * @return whether a {@link TwitchChat} instance was identified to send the message from.
     * @throws NullPointerException if the identified {@link TwitchChat} does not have a valid chatCredential
     */
    public boolean sendPrivateMessage(final String channelToIdentifyChatInstance, final String toChannel, final String message) {
        final TwitchChat chat;
        if (channelToIdentifyChatInstance == null || (chat = subscriptions.get(channelToIdentifyChatInstance.toLowerCase())) == null)
            return false;

        chat.sendPrivateMessage(toChannel, message);
        return true;
    }

    /**
     * Joins a channel.
     *
     * @param s the channel name
     * @return the channel name
     */
    @Override
    public String subscribe(String s) {
        return s != null ? super.subscribe(s.toLowerCase()) : null;
    }

    @Override
    public void joinChannel(String channelName) {
        this.subscribe(channelName);
    }

    /**
     * Parts from a channel.
     *
     * @param s the channel name
     * @return a non-null class if able to part, null otherwise
     */
    @Override
    public Boolean unsubscribe(String s) {
        return super.unsubscribe(s != null ? s.toLowerCase() : null);
    }

    @Override
    public boolean leaveChannel(String channelName) {
        final Boolean b = this.unsubscribe(channelName);
        return b != null && b;
    }

    @Override
    public boolean isChannelJoined(String channelName) {
        return this.subscriptions.containsKey(channelName.toLowerCase());
    }

    @Override
    public Set<String> getChannels() {
        return Collections.unmodifiableSet(subscriptions.keySet());
    }

    @Override
    protected String handleSubscription(TwitchChat twitchChat, String s) {
        if (twitchChat == null) return null;
        twitchChat.joinChannel(s);
        return s;
    }

    @Override
    protected String handleDuplicateSubscription(TwitchChat twitchChat, TwitchChat old, String s) {
        return twitchChat != null && twitchChat != old && twitchChat.leaveChannel(s) ? s : null;
    }

    @Override
    protected Boolean handleUnsubscription(TwitchChat twitchChat, String s) {
        return twitchChat != null ? twitchChat.leaveChannel(s) : null;
    }

    @Override
    protected String getRequestFromSubscription(String s) {
        return s;
    }

    @Override
    protected int getSubscriptionSize(String s) {
        return 1;
    }

    @Override
    protected TwitchChat createConnection() {
        // Instantiate with configuration
        TwitchChat chat = advancedConfiguration.apply(
            TwitchChatBuilder.builder()
                .withChatAccount(chatAccount.get())
                .withEventManager(getConnectionEventManager())
                .withScheduledThreadPoolExecutor(getExecutor(threadPrefix + RandomStringUtils.random(4, true, true), TwitchChat.REQUIRED_THREAD_COUNT))
                .withProxyConfig(proxyConfig.get())
                .withAutoJoinOwnChannel(false) // user will have to manually send a subscribe call to enable whispers. this avoids duplicating whisper events
        ).build();

        // Reclaim channel headroom upon a ban
        chat.getEventManager().onEvent("twitch4j-chat-pool-ban-tracker", ChannelNoticeEvent.class, e -> {
            if (automaticallyPartOnBan && "msg_banned".equals(e.getMsgId())) {
                unsubscribe(e.getChannel().getName());
            }
        });

        // Return chat client
        return chat;
    }

    @Override
    protected void disposeConnection(TwitchChat connection) {
        connection.close();
    }

    /**
     * Note: this map does not dynamically update unlike {@link TwitchChat#getChannelIdToChannelName()}
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getChannelIdToChannelName() {
        return collectMapsFromConnections(TwitchChat::getChannelIdToChannelName);
    }

    /**
     * Note: this map does not dynamically update unlike {@link TwitchChat#getChannelNameToChannelId()}
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getChannelNameToChannelId() {
        return collectMapsFromConnections(TwitchChat::getChannelNameToChannelId);
    }

    private <K, V> Map<K, V> collectMapsFromConnections(final Function<TwitchChat, Map<K, V>> mapRetriever) {
        final Map<K, V> aggregated = new HashMap<>(numConnections() * maxSubscriptionsPerConnection);
        final Consumer<TwitchChat> retrieve = chat -> aggregated.putAll(mapRetriever.apply(chat));
        // Note: if connections are changing in saturation concurrently, this lock-free approach could skip over those instances
        saturatedConnections.forEach(retrieve);
        unsaturatedConnections.keySet().forEach(retrieve);
        return Collections.unmodifiableMap(aggregated);
    }

}
