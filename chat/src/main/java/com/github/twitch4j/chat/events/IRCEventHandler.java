package com.github.twitch4j.chat.events;

import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.*;
import com.github.twitch4j.chat.events.roomstate.*;
import com.github.twitch4j.common.enums.SubscriptionPlan;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.common.events.user.PrivateMessageEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.github.twitch4j.common.util.TwitchUtils.ANONYMOUS_CHEERER;
import static com.github.twitch4j.common.util.TwitchUtils.ANONYMOUS_GIFTER;

/**
 * IRC Event Handler
 *
 * Listens for any irc triggered events and created the corresponding events for the EventDispatcher.
 */
@Getter
@Slf4j
public class IRCEventHandler {

    /**
     * Twitch Client
     */
    private final TwitchChat twitchChat;

    /**
     * Event Manager
     */
    private final EventManager eventManager;

    /**
     * Constructor
     *
     * @param twitchChat The Twitch Chat instance
     */
    public IRCEventHandler(TwitchChat twitchChat) {
        this.twitchChat = twitchChat;
        this.eventManager = twitchChat.getEventManager();

        // register event handlers
        eventManager.onEvent("twitch4j-chat-message-trigger", IRCMessageEvent.class, this::onChannelMessage);
        eventManager.onEvent("twitch4j-chat-whisper-trigger", IRCMessageEvent.class, this::onWhisper);
        eventManager.onEvent("twitch4j-chat-bits-badge-trigger", IRCMessageEvent.class, this::onBitsBadgeTier);
        eventManager.onEvent("twitch4j-chat-cheer-trigger", IRCMessageEvent.class, this::onChannelCheer);
        eventManager.onEvent("twitch4j-chat-sub-trigger", IRCMessageEvent.class, this::onChannelSubscription);
        eventManager.onEvent("twitch4j-chat-clearchat-trigger", IRCMessageEvent.class, this::onClearChat);
        eventManager.onEvent("twitch4j-chat-join-trigger", IRCMessageEvent.class, this::onChannnelClientJoinEvent);
        eventManager.onEvent("twitch4j-chat-leave-trigger", IRCMessageEvent.class, this::onChannnelClientLeaveEvent);
        eventManager.onEvent("twitch4j-chat-mod-trigger", IRCMessageEvent.class, this::onChannelModChange);
        eventManager.onEvent("twitch4j-chat-notice-trigger", IRCMessageEvent.class, this::onNoticeEvent);
        eventManager.onEvent("twitch4j-chat-host-on-trigger", IRCMessageEvent.class, this::onHostOnEvent);
        eventManager.onEvent("twitch4j-chat-host-off-trigger", IRCMessageEvent.class, this::onHostOffEvent);
        eventManager.onEvent("twitch4j-chat-inbound-host-trigger", IRCMessageEvent.class, this::onInboundHostEvent);
        eventManager.onEvent("twitch4j-chat-list-mods-trigger", IRCMessageEvent.class, this::onListModsEvent);
        eventManager.onEvent("twitch4j-chat-list-vips-trigger", IRCMessageEvent.class, this::onListVipsEvent);
        eventManager.onEvent("twitch4j-chat-roomstate-trigger", IRCMessageEvent.class, this::onChannelState);
        eventManager.onEvent("twitch4j-chat-gift-trigger", IRCMessageEvent.class, this::onGiftReceived);
        eventManager.onEvent("twitch4j-chat-payforward-trigger", IRCMessageEvent.class, this::onPayForward);
        eventManager.onEvent("twitch4j-chat-raid-trigger", IRCMessageEvent.class, this::onRaid);
        eventManager.onEvent("twitch4j-chat-unraid-trigger", IRCMessageEvent.class, this::onUnraid);
        eventManager.onEvent("twitch4j-chat-rewardgift-trigger", IRCMessageEvent.class, this::onRewardGift);
        eventManager.onEvent("twitch4j-chat-ritual-trigger", IRCMessageEvent.class, this::onRitual);
        eventManager.onEvent("twitch4j-chat-delete-trigger", IRCMessageEvent.class, this::onMessageDeleteResponse);
        eventManager.onEvent("twitch4j-chat-userstate-trigger", IRCMessageEvent.class, this::onUserState);
        eventManager.onEvent("twitch4j-chat-globaluserstate-trigger", IRCMessageEvent.class, this::onGlobalUserState);
    }

    /**
     * ChatChannel Message Event
     * @param event IRCMessageEvent
     */
    public void onChannelMessage(IRCMessageEvent event) {
        if(event.getCommandType().equals("PRIVMSG")) {
            if(!event.getTags().containsKey("bits") && event.getMessage().isPresent()) {
                // Load Info
                EventChannel channel = event.getChannel();
                EventUser user = event.getUser();

                // Dispatch Event
                if(event.getMessage().get().startsWith("\u0001ACTION ")) {
                    // Action
                    eventManager.publish(new ChannelMessageActionEvent(channel, event, user, event.getMessage().get().substring(8), event.getClientPermissions()));
                } else {
                    // Regular Message
                    eventManager.publish(new ChannelMessageEvent(channel, event, user, event.getMessage().get(), event.getClientPermissions()));
                }
            }
        }
    }

    /**
     * Whisper Event
     * @param event IRCMessageEvent
     */
    public void onWhisper(IRCMessageEvent event) {
        if(event.getCommandType().equals("WHISPER")) {
            // Load Info
            EventUser user = event.getUser();

            // Dispatch Event
            eventManager.publish(new PrivateMessageEvent(user, event.getMessage().get(), event.getClientPermissions()));
        }
    }

    /**
     * ChatChannel Bits Badge Earned Event Parser
     *
     * @param event the {@link IRCMessageEvent} to be checked
     */
    public void onBitsBadgeTier(IRCMessageEvent event) {
        if ("USERNOTICE".equals(event.getCommandType()) && "bitsbadgetier".equalsIgnoreCase(event.getTags().get("msg-id"))) {
            // Load Info
            EventChannel channel = event.getChannel();
            EventUser user = event.getUser();

            String thresholdParam = event.getTags().get("msg-param-threshold");
            int bitsThreshold = thresholdParam != null ? Integer.parseInt(thresholdParam) : -1;

            // Dispatch Event
            eventManager.publish(new BitsBadgeEarnedEvent(channel, user, bitsThreshold));
        }
    }

    /**
     * ChatChannel Cheer (Bits) Event
     * @param event IRCMessageEvent
     */
    public void onChannelCheer(IRCMessageEvent event) {
        if(event.getCommandType().equals("PRIVMSG")) {
            if(event.getTags().containsKey("bits")) {
                // Load Info
                EventChannel channel = event.getChannel();
                EventUser user = event.getUser();
                String message = event.getMessage().orElse("");
                Integer bits = Integer.parseInt(event.getTags().get("bits"));
                int subMonths = event.getSubscriberMonths().orElse(0);
                int subTier = event.getSubscriptionTier().orElse(0);

                // Dispatch Event
                eventManager.publish(new CheerEvent(event, channel, user != null ? user : ANONYMOUS_CHEERER, message, bits, subMonths, subTier, event.getFlags()));
            }
        }
    }

    /**
     * ChatChannel Subscription Event
     *
     * @param event IRCMessageEvent
     */
    public void onChannelSubscription(IRCMessageEvent event) {
        final String msgId;
        if (event.getCommandType().equals("USERNOTICE") && (msgId = event.getTags().get("msg-id")) != null) {
            EventChannel channel = event.getChannel();

            // Sub
            if (msgId.equalsIgnoreCase("sub") || msgId.equalsIgnoreCase("resub")) {
                // Load Info
                EventUser user = event.getUser();
                String subPlan = event.getTagValue("msg-param-sub-plan").get();
                int cumulativeMonths = event.getTags().containsKey("msg-param-cumulative-months") ? Integer.parseInt(event.getTags().get("msg-param-cumulative-months")) : 0;
                //according to the Twitch docs, msg-param-months is used only for giftsubs, which are handled below

                // twitch sometimes returns 0 months for new subs
                if (cumulativeMonths == 0) {
                    cumulativeMonths = 1;
                }

                // check user's sub streak
                // Twitch API specifies that 0 is returned if the user chooses not to share their streak
                Integer streak = event.getTags().containsKey("msg-param-streak-months") ? Integer.parseInt(event.getTags().get("msg-param-streak-months")) : 0;

                // unofficial: multi month tags
                Integer multiMonthDuration = Math.max(Integer.parseInt(event.getTags().getOrDefault("msg-param-multimonth-duration", "1")), 1);
                Integer multiMonthTenure = Integer.parseInt(event.getTags().getOrDefault("msg-param-multimonth-tenure", "0"));

                // Dispatch Event
                eventManager.publish(new SubscriptionEvent(event, channel, user, subPlan, event.getMessage(), cumulativeMonths, false, null, streak, null, multiMonthDuration, multiMonthTenure, event.getFlags()));
            }
            // Receive Gifted Sub
            else if (msgId.equalsIgnoreCase("subgift") || msgId.equalsIgnoreCase("anonsubgift")) {
                // Load Info
                EventUser user = new EventUser(event.getTagValue("msg-param-recipient-id").get(), event.getTagValue("msg-param-recipient-user-name").get());
                EventUser giftedBy = event.getUser();
                String subPlan = event.getTagValue("msg-param-sub-plan").get();
                int subStreak = event.getTags().containsKey("msg-param-months") ? Integer.parseInt(event.getTags().get("msg-param-months")) : 1;

                // twitch sometimes returns 0 months for new subs
                if (subStreak == 0) {
                    subStreak = 1;
                }

                // Handle multi-month gifts
                String giftMonthsParam = event.getTags().get("msg-param-gift-months");
                int giftMonths = giftMonthsParam != null ? Integer.parseInt(giftMonthsParam) : 1;
                // unofficial: plausible, but hasn't been observed in the wild for this msg-id
                String multiTenureParam = event.getTags().get("msg-param-multimonth-tenure");
                Integer multiMonthTenure = StringUtils.isEmpty(multiTenureParam) ? null : Integer.parseInt(multiTenureParam);

                // Dispatch Event
                eventManager.publish(new SubscriptionEvent(event, channel, user, subPlan, event.getMessage(), subStreak, true, giftedBy != null ? giftedBy : ANONYMOUS_GIFTER, 0, giftMonths, giftMonths, multiMonthTenure, event.getFlags()));
            }
            // Gift X Subs
            else if (msgId.equalsIgnoreCase("submysterygift") || msgId.equalsIgnoreCase("anonsubmysterygift")) {
                // Load Info
                EventUser user = event.getUser();
                String subPlan = event.getTagValue("msg-param-sub-plan").get();
                Integer subsGifted = (event.getTags().containsKey("msg-param-mass-gift-count")) ? Integer.parseInt(event.getTags().get("msg-param-mass-gift-count")) : 0;
                Integer subsGiftedTotal = (event.getTags().containsKey("msg-param-sender-count")) ? Integer.parseInt(event.getTags().get("msg-param-sender-count")) : 0;

                // Dispatch Event
                eventManager.publish(new GiftSubscriptionsEvent(channel, user != null ? user : ANONYMOUS_GIFTER, subPlan, subsGifted, subsGiftedTotal));
            }
            // Upgrading from a gifted sub
            else if (msgId.equalsIgnoreCase("giftpaidupgrade") || msgId.equalsIgnoreCase("anongiftpaidupgrade")) {
                // Load Info
                EventUser user = event.getUser();
                String promoName = event.getTagValue("msg-param-promo-name").orElse(null);
                String giftTotalParam = event.getTags().get("msg-param-promo-gift-total");
                Integer giftTotal = giftTotalParam != null ? Integer.parseInt(giftTotalParam) : null;
                String senderLogin = event.getTagValue("msg-param-sender-login").orElse(null);
                String senderName = event.getTagValue("msg-param-sender-name").orElse(null);

                // Dispatch Event
                eventManager.publish(new GiftSubUpgradeEvent(channel, user, promoName, giftTotal, senderLogin, senderName));
            }
            // Upgrading from a Prime sub to a normal one
            else if (msgId.equalsIgnoreCase("primepaidupgrade")) {
                // Load Info
                EventUser user = event.getUser();
                SubscriptionPlan subPlan = event.getTagValue("msg-param-sub-plan").map(SubscriptionPlan::fromString).orElse(null);

                // Dispatch Event
                eventManager.publish(new PrimeSubUpgradeEvent(channel, user, subPlan));
            }
            // Extend Subscription
            else if (msgId.equalsIgnoreCase("extendsub")) {
                // Load Info
                EventUser user = event.getUser();
                SubscriptionPlan subPlan = event.getTagValue("msg-param-sub-plan").map(SubscriptionPlan::fromString).orElse(null);

                String cumMonthsParam = event.getTags().get("msg-param-cumulative-months");
                int cumulativeMonths = cumMonthsParam != null ? Math.max(Integer.parseInt(cumMonthsParam), 1) : 1;

                String endMonthParam = event.getTags().get("msg-param-sub-benefit-end-month");
                Month endMonth = endMonthParam != null ? Month.of(Integer.parseInt(endMonthParam)) : null;

                // Dispatch Event
                eventManager.publish(new ExtendSubscriptionEvent(channel, user, subPlan, cumulativeMonths, endMonth));
            }
        }
    }

    /**
     * ChatChannel Prime Community Gift Event Parser: user receives a gift from a prime member
     *
     * @param event the {@link IRCMessageEvent} to be checked
     */
    public void onGiftReceived(IRCMessageEvent event) {
        if ("USERNOTICE".equals(event.getCommandType()) && "primecommunitygiftreceived".equalsIgnoreCase(event.getTags().get("msg-id"))) {
            // Load Info
            EventChannel channel = event.getChannel();
            EventUser user = event.getUser();
            String giftName = event.getTagValue("msg-param-gift-name").orElse(null);
            String recipientName = event.getTagValue("msg-param-recipient").orElse(null);

            // Dispatch Event
            eventManager.publish(new PrimeGiftReceivedEvent(channel, user, giftName, recipientName));
        }
    }

    /**
     * ChatChannel Pay Forward Event Parser: user pays forward a gift they previously received
     *
     * @param event the {@link IRCMessageEvent} to be checked
     */
    public void onPayForward(IRCMessageEvent event) {
        String msgId;
        if ("USERNOTICE".equals(event.getCommandType()) && (msgId = event.getTags().get("msg-id")) != null
            && (msgId.equalsIgnoreCase("standardpayforward") || msgId.equalsIgnoreCase("communitypayforward"))) {
            // Load Info
            EventChannel channel = event.getChannel();
            EventUser user = event.getUser();

            // Present for both standard & community when not anonymous
            String gifterId = event.getTagValue("msg-param-prior-gifter-id").orElse(null);
            String gifterName = gifterId != null
                ? event.getTagValue("msg-param-prior-gifter-user-name").orElseGet(() -> event.getTagValue("msg-param-prior-gifter-display-name").orElse(null))
                : null;
            EventUser gifter = gifterId != null ? new EventUser(gifterId, gifterName) : null;

            // Only present for standard
            String recipientId = msgId.charAt(0) == 's' ? event.getTagValue("msg-param-recipient-id").orElse(null) : null;
            String recipientName = recipientId != null
                ? event.getTagValue("msg-param-recipient-user-name").orElseGet(() -> event.getTagValue("msg-param-recipient-display-name").orElse(null))
                : null;
            EventUser recipient = recipientId != null ? new EventUser(recipientId, recipientName) : null;

            // Dispatch Event
            eventManager.publish(new PayForwardEvent(channel, user, gifter, recipient));
        }
    }

    /**
     * ChatChannel Raid Event (receiving)
     * @param event IRCMessageEvent
     */
    public void onRaid(IRCMessageEvent event) {
        if (event.getCommandType().equals("USERNOTICE") && event.getTags().containsKey("msg-id") && event.getTags().get("msg-id").equalsIgnoreCase("raid")) {
            EventChannel channel = event.getChannel();
            EventUser raider = event.getUser();
            Integer viewers;
            try {
                viewers = Integer.parseInt(event.getTags().get("msg-param-viewerCount"));
            }
            catch(NumberFormatException ex) {
                viewers = 0;
            }
            eventManager.publish(new RaidEvent(channel, raider, viewers));
        }
    }

    /**
     * ChatChannel Unraid Parser: raid cancellation
     *
     * @param event the {@link IRCMessageEvent} to be checked
     */
    public void onUnraid(IRCMessageEvent event) {
        if ("USERNOTICE".equals(event.getCommandType()) && "unraid".equalsIgnoreCase(event.getTags().get("msg-id"))) {
            eventManager.publish(new RaidCancellationEvent(event.getChannel()));
        }
    }

    /**
     * ChatChannel Reward Gift Event Parser: monetary event triggered emotes to be shared
     *
     * @param event the {@link IRCMessageEvent} to be checked
     */
    public void onRewardGift(IRCMessageEvent event) {
        if ("USERNOTICE".equals(event.getCommandType()) && "rewardgift".equalsIgnoreCase(event.getTags().get("msg-id"))) {
            // Load Info
            EventChannel channel = event.getChannel();
            EventUser user = event.getUser();
            String domain = event.getTagValue("msg-param-domain").orElse(null);
            String triggerType = event.getTagValue("msg-param-trigger-type").orElse(null);

            String selectedCountParam = event.getTags().get("msg-param-selected-count");
            Integer selectedCount = selectedCountParam != null ? Integer.parseInt(selectedCountParam) : null;

            String totalRewardCountParam = event.getTags().get("msg-param-total-reward-count");
            Integer totalRewardCount = totalRewardCountParam != null ? Integer.parseInt(totalRewardCountParam) : null;

            String triggerAmountParam = event.getTags().get("msg-param-trigger-amount");
            Integer triggerAmount = triggerAmountParam != null ? Integer.parseInt(triggerAmountParam) : null;

            // Dispatch Event
            eventManager.publish(new RewardGiftEvent(channel, user, domain, triggerType, selectedCount, totalRewardCount, triggerAmount));
        }
    }

    /**
     * ChatChannel Ritual Event Parser: celebration of a shared viewer milestone
     *
     * @param event the {@link IRCMessageEvent} to be checked
     */
    public void onRitual(IRCMessageEvent event) {
        if ("USERNOTICE".equals(event.getCommandType()) && "ritual".equalsIgnoreCase(event.getTags().get("msg-id"))) {
            // Load Info
            EventChannel channel = event.getChannel();
            EventUser user = event.getUser();
            String ritualName = event.getTagValue("msg-param-ritual-name").orElse(null);

            // Dispatch Event
            eventManager.publish(new RitualEvent(channel, user, ritualName));
        }
    }

    /**
     * ChatChannel clearing chat, timeouting or banning user Event
     * @param event IRCMessageEvent
     */
    public void onClearChat(IRCMessageEvent event) {
        if (event.getCommandType().equals("CLEARCHAT")) {
            EventChannel channel = event.getChannel();
            if (event.getTags().containsKey("target-user-id")) { // ban or timeout
                if (event.getTags().containsKey("ban-duration")) { // timeout
                    // Load Info
                    EventUser user = event.getTargetUser();
                    Integer duration = Integer.parseInt(event.getTagValue("ban-duration").get());
                    String banReason = event.getTags().get("ban-reason") != null ? event.getTags().get("ban-reason").toString() : "";
                    banReason = banReason.replaceAll("\\\\s", " ");
                    UserTimeoutEvent timeoutEvent = new UserTimeoutEvent(channel, user, duration, banReason);

                    // Dispatch Event
                    eventManager.publish(timeoutEvent);
                } else { // ban
                    // Load Info
                    EventUser user = event.getTargetUser();
                    String banReason = event.getTagValue("ban-reason").orElse("");
                    banReason = banReason.replaceAll("\\\\s", " ");
                    UserBanEvent banEvent = new UserBanEvent(channel, user, banReason);

                    // Dispatch Event
                    eventManager.publish(banEvent);
                }
            } else { // Clear chat event
                eventManager.publish(new ClearChatEvent(channel));
            }
        }
    }

    /**
     * User Joins ChatChannel Event
     * @param event IRCMessageEvent
     */
    public void onChannnelClientJoinEvent(IRCMessageEvent event) {
        if(event.getCommandType().equals("JOIN") && event.getChannelName().isPresent() && event.getClientName().isPresent()) {
            // Load Info
            EventChannel channel = event.getChannel();
            EventUser user = event.getUser();

            // Dispatch Event
            if (channel != null && user != null) {
                eventManager.publish(new ChannelJoinEvent(channel, user));
            }
        }
    }

    /**
     * User Leaves ChatChannel Event
     * @param event IRCMessageEvent
     */
    public void onChannnelClientLeaveEvent(IRCMessageEvent event) {
        if(event.getCommandType().equals("PART") && event.getChannelName().isPresent() && event.getClientName().isPresent()) {
            // Load Info
            EventChannel channel = event.getChannel();
            EventUser user = event.getUser();

            // Dispatch Event
            if (channel != null && user != null) {
                eventManager.publish(new ChannelLeaveEvent(channel, user));
            }
        }
    }

    /**
     * Mod Status Change Event
     * @param event IRCMessageEvent
     */
    public void onChannelModChange(IRCMessageEvent event) {
        if(event.getCommandType().equals("MODE") && event.getPayload().isPresent()) {
            // Receiving Mod Status
            if(event.getPayload().get().substring(1).startsWith("o")) {
                // Load Info
                EventChannel channel = event.getChannel();
                EventUser user = new EventUser(null, event.getPayload().get().substring(3));

                // Dispatch Event
                eventManager.publish(new ChannelModEvent(channel, user, event.getPayload().get().startsWith("+")));
            }
        }
    }

    public void onNoticeEvent(IRCMessageEvent event) {
        if (event.getCommandType().equals("NOTICE")) {
            EventChannel channel = event.getChannel();
            String messageId = event.getTagValue("msg-id").get();
            String message = event.getMessage().orElse(null); // can be null, ie. bad_delete_message_error

            eventManager.publish(new ChannelNoticeEvent(channel, messageId, message));
        }
    }

    public void onHostOnEvent(IRCMessageEvent event) {
        if (event.getCommandType().equals("NOTICE")) {
            EventChannel channel = event.getChannel();
            String messageId = event.getTagValue("msg-id").get();

            if(messageId.equals("host_on")) {
                String message = event.getMessage().get();
                String targetChannelName = message.substring(12, message.length() - 1);
                EventChannel targetChannel = new EventChannel(null, targetChannelName);
                eventManager.publish(new HostOnEvent(channel, targetChannel));
            }
        }
    }

    public void onHostOffEvent(IRCMessageEvent event) {
        if (event.getCommandType().equals("NOTICE")) {
            EventChannel channel = event.getChannel();
            String messageId = event.getTagValue("msg-id").get();

            if(messageId.equals("host_off")) {
                eventManager.publish(new HostOffEvent(channel));
            }
        }
    }

    public void onInboundHostEvent(IRCMessageEvent event) {
        if ("PRIVMSG".equals(event.getCommandType()) && "jtv".equals(event.getClientName().orElse(null)) && event.getChannelName().isPresent() && event.getRawTags().isEmpty()) {
            final String hostMessageSuffix = " is now hosting you.";
            event.getMessage()
                .map(String::trim)
                .filter(msg -> msg.endsWith(hostMessageSuffix))
                .map(msg -> msg.substring(0, msg.length() - hostMessageSuffix.length()))
                .ifPresent(hostName -> eventManager.publish(new InboundHostEvent(event.getChannelName().get(), hostName)));
        }
    }

    public void onListModsEvent(IRCMessageEvent event) {
        if ("NOTICE".equals(event.getCommandType()) && event.getTagValue("msg-id").filter(s -> s.equals("room_mods") || s.equals("no_mods")).isPresent()) {
            List<String> names = extractItemsFromDelimitedList(event.getMessage(), "The moderators of this channel are: ", ", ");
            eventManager.publish(new ListModsEvent(event.getChannel(), names));
        }
    }

    public void onListVipsEvent(IRCMessageEvent event) {
        if ("NOTICE".equals(event.getCommandType()) && event.getTagValue("msg-id").filter(s -> s.equals("vips_success") || s.equals("no_vips")).isPresent()) {
            List<String> names = extractItemsFromDelimitedList(event.getMessage(), "The VIPs of this channel are: ", ", ");
            eventManager.publish(new ListVipsEvent(event.getChannel(), names));
        }
    }

    public void onChannelState(IRCMessageEvent event) {
        if (event.getCommandType().equals("ROOMSTATE")) {
            // getting Status on channel
            EventChannel channel = event.getChannel();
            Map<ChannelStateEvent.ChannelState, Object> states = new HashMap<>();
            if (event.getTags().size() > 2) {
                event.getTags().forEach((k, v) -> {
                    switch (k) {
                        case "broadcaster-lang":
                            Locale locale = v != null ? Locale.forLanguageTag(v) : null;
                            states.put(ChannelStateEvent.ChannelState.BROADCAST_LANG, locale);
                            eventManager.publish(new BroadcasterLanguageEvent(channel, locale));
                            break;
                        case "emote-only":
                            boolean eoActive = "1".equals(v);
                            states.put(ChannelStateEvent.ChannelState.EMOTE, eoActive);
                            eventManager.publish(new EmoteOnlyEvent(channel, eoActive));
                            break;
                        case "followers-only":
                            long followDelay = Long.parseLong(v);
                            states.put(ChannelStateEvent.ChannelState.FOLLOWERS, followDelay);
                            eventManager.publish(new FollowersOnlyEvent(channel, followDelay));
                            break;
                        case "r9k":
                            boolean uniqActive = "1".equals(v);
                            states.put(ChannelStateEvent.ChannelState.R9K, uniqActive);
                            eventManager.publish(new Robot9000Event(channel, uniqActive));
                            break;
                        case "slow":
                            long slowDelay = Long.parseLong(v);
                            states.put(ChannelStateEvent.ChannelState.SLOW, slowDelay);
                            eventManager.publish(new SlowModeEvent(channel, slowDelay));
                            break;
                        case "subs-only":
                            boolean subActive = "1".equals(v);
                            states.put(ChannelStateEvent.ChannelState.SUBSCRIBERS, subActive);
                            eventManager.publish(new SubscribersOnlyEvent(channel, subActive));
                            break;
                        default:
                            break;
                    }
                });
            }
            eventManager.publish(new ChannelStateEvent(channel, states));
        }
    }

    public void onMessageDeleteResponse(IRCMessageEvent event) {
        if (event.getCommandType().equals("NOTICE")) {
            EventChannel channel = event.getChannel();
            String messageId = event.getTagValue("msg-id").get();

            if (messageId.equals("delete_message_success")) {
                eventManager.publish(new MessageDeleteSuccess(channel));
            } else if (messageId.equals("bad_delete_message_error")) {
                eventManager.publish(new MessageDeleteError(channel));
                log.warn("Failed to delete a message in {}!", channel.getName());
            }
        }
    }

    public void onUserState(IRCMessageEvent event) {
        if (event.getCommandType().equals("USERSTATE")) {
            eventManager.publish(new UserStateEvent(event));
        }
    }

    public void onGlobalUserState(IRCMessageEvent event) {
        if ("GLOBALUSERSTATE".equals(event.getCommandType())) {
            eventManager.publish(new GlobalUserStateEvent(event));
        }
    }

    @NonNull
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static List<String> extractItemsFromDelimitedList(@NonNull Optional<String> message, @NonNull String prefix, @NonNull String delim) {
        return message.filter(s -> s.startsWith(prefix))
            .map(s -> s.substring(prefix.length()))
            .map(s -> s.charAt(s.length() - 1) == '.' ? s.substring(0, s.length() - 1) : s) // remove trailing period if present
            .map(s -> StringUtils.split(s, delim))
            .map(Arrays::asList)
            .map(Collections::unmodifiableList)
            .orElse(Collections.emptyList());
    }

}
