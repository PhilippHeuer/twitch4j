package com.github.twitch4j.chat.events.channel;

import com.github.twitch4j.chat.ITwitchChat;
import com.github.twitch4j.chat.events.AbstractChannelEvent;
import com.github.twitch4j.chat.flag.AutoModFlag;
import com.github.twitch4j.common.annotation.Unofficial;
import com.github.twitch4j.common.enums.SubscriptionPlan;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * This event gets called when a user gets a new subscriber or a user resubscribes.
 * <p>
 * This event will be called simultaneously with the chat announcement,
 * not necessary when the user presses the subscription button.
 */
@Value
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SubscriptionEvent extends AbstractChannelEvent implements ReplyableEvent {

    /**
     * Raw IRC Message Event
     */
    IRCMessageEvent messageEvent;

    /**
     * Event Target User
     */
    EventUser user;

    /**
     * Subscription Plan
     */
    String subscriptionPlan;

    /**
     * Subscription Plan, in enum form
     */
    @Getter(lazy = true)
    SubscriptionPlan subPlan = SubscriptionPlan.fromString(subscriptionPlan);

    /**
     * Subscription Message
     */
    Optional<String> message;

    /**
     * Cumulative months subscribed
     */
    Integer months;

    /**
     * Was this sub gifted?
     */
    Boolean gifted;

    /**
     * User that gifted the sub
     */
    EventUser giftedBy;

    /**
     * Consecutive months subscribed
     */
    Integer subStreak;

    /**
     * The number of months gifted as part of a single, multi-month gift
     */
    Integer giftMonths;

    /**
     * The number of subscription months just purchased. (Unofficial)
     */
    @Unofficial
    Integer multiMonthDuration;

    /**
     * The length of multi-month subscription tenure that has already been served. Can be null for gifts. (Unofficial)
     */
    @Nullable
    @Unofficial
    Integer multiMonthTenure;

    /**
     * The regions of {@link #getMessage()} that were flagged by AutoMod (Unofficial)
     */
    @Unofficial
    List<AutoModFlag> flags;

    /**
     * Event Constructor
     *
     * @param event              The raw message event.
     * @param channel            ChatChannel the user subscribed to
     * @param user               User that subscribed
     * @param subPlan            Sub Plan
     * @param message            Sub Message
     * @param months             Cumulative number of months user has been subscribed (not consecutive)
     * @param gifted             Is gifted?
     * @param giftedBy           User that gifted the sub
     * @param subStreak          Consecutive number of months user has been subscribed (not cumulative); 0 if no streak or user chooses not to share their streak
     * @param giftMonths         The number of months gifted as part of a single, multi-month gift
     * @param multiMonthDuration The number of subscription months just purchased
     * @param multiMonthTenure   The length of multi-month subscription tenure that has already been served
     * @param flags              The regions of the message that were flagged by AutoMod.
     */
    public SubscriptionEvent(IRCMessageEvent event, EventChannel channel, EventUser user, String subPlan, Optional<String> message, Integer months, Boolean gifted, EventUser giftedBy, Integer subStreak, Integer giftMonths, Integer multiMonthDuration, Integer multiMonthTenure, List<AutoModFlag> flags) {
        super(channel);
        this.messageEvent = event;
        this.user = user;
        this.subscriptionPlan = subPlan;
        this.message = message;
        this.months = months;
        this.gifted = gifted;
        this.giftedBy = giftedBy;
        this.subStreak = subStreak;
        this.giftMonths = giftMonths;
        this.multiMonthDuration = multiMonthDuration;
        this.multiMonthTenure = multiMonthTenure;
        this.flags = flags;
    }

    /**
     * Replies to this sub event, <i>if</i> a message was included when the subscription was shared.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void reply(ITwitchChat chat, String message) {
        getMessage().ifPresent(m -> ReplyableEvent.super.reply(chat, message));
    }

    /**
     * Gets the Subscription Plan
     *
     * @return SubscriptionPlan
     * @deprecated will be removed in favor of .getSubPlan()
     */
    @Deprecated
    public com.github.twitch4j.chat.enums.SubscriptionPlan getSubscriptionPlanName() {
        return com.github.twitch4j.chat.enums.SubscriptionPlan.fromString(this.subscriptionPlan);
    }

}
