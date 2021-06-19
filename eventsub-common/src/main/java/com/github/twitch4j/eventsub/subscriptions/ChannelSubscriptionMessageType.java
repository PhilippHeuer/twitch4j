package com.github.twitch4j.eventsub.subscriptions;

import com.github.twitch4j.eventsub.condition.ChannelSubscriptionMessageCondition;
import com.github.twitch4j.eventsub.events.ChannelSubscriptionMessageEvent;

/**
 * The channel.subscription.message subscription type sends a notification when a user sends a re-subscription chat message in a specific channel.
 * <p>
 * Must have channel:read:subscriptions scope.
 */
public class ChannelSubscriptionMessageType implements SubscriptionType<ChannelSubscriptionMessageCondition, ChannelSubscriptionMessageCondition.ChannelSubscriptionMessageConditionBuilder<?, ?>, ChannelSubscriptionMessageEvent> {

    @Override
    public String getName() {
        return "channel.subscription.message";
    }

    @Override
    public String getVersion() {
        return "1";
    }

    @Override
    public ChannelSubscriptionMessageCondition.ChannelSubscriptionMessageConditionBuilder<?, ?> getConditionBuilder() {
        return ChannelSubscriptionMessageCondition.builder();
    }

    @Override
    public Class<ChannelSubscriptionMessageEvent> getEventClass() {
        return ChannelSubscriptionMessageEvent.class;
    }

}
