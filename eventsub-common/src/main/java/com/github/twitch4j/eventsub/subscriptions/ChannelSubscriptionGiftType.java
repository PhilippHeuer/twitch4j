package com.github.twitch4j.eventsub.subscriptions;

import com.github.twitch4j.eventsub.condition.ChannelSubscriptionGiftCondition;
import com.github.twitch4j.eventsub.events.ChannelSubscriptionGiftEvent;

/**
 * The channel.subscription.gift subscription type sends a notification when a user gives one or more gifted subscriptions in a channel.
 * <p>
 * Must have channel:read:subscriptions scope.
 */
public class ChannelSubscriptionGiftType implements SubscriptionType<ChannelSubscriptionGiftCondition, ChannelSubscriptionGiftCondition.ChannelSubscriptionGiftConditionBuilder<?, ?>, ChannelSubscriptionGiftEvent> {

    @Override
    public String getName() {
        return "channel.subscription.gift";
    }

    @Override
    public String getVersion() {
        return "1";
    }

    @Override
    public ChannelSubscriptionGiftCondition.ChannelSubscriptionGiftConditionBuilder<?, ?> getConditionBuilder() {
        return ChannelSubscriptionGiftCondition.builder();
    }

    @Override
    public Class<ChannelSubscriptionGiftEvent> getEventClass() {
        return ChannelSubscriptionGiftEvent.class;
    }

}
