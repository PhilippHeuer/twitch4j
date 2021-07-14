package com.github.twitch4j.eventsub.subscriptions;

import com.github.twitch4j.eventsub.condition.DropEntitlementGrantCondition;
import com.github.twitch4j.eventsub.events.batched.BatchedDropEntitlementGrantEvents;

/**
 * This subscription type sends a notification when an entitlement for a Drop is granted to a user.
 * <p>
 * The client ID associated with the app access token must be owned by a user who is part of the specified organization.
 */
public class DropEntitlementGrantType implements SubscriptionType<DropEntitlementGrantCondition, DropEntitlementGrantCondition.DropEntitlementGrantConditionBuilder<?, ?>, BatchedDropEntitlementGrantEvents> {

    @Override
    public String getName() {
        return "drop.entitlement.grant";
    }

    @Override
    public String getVersion() {
        return "1";
    }

    @Override
    public DropEntitlementGrantCondition.DropEntitlementGrantConditionBuilder<?, ?> getConditionBuilder() {
        return DropEntitlementGrantCondition.builder();
    }

    @Override
    public Class<BatchedDropEntitlementGrantEvents> getEventClass() {
        return BatchedDropEntitlementGrantEvents.class;
    }

    @Override
    public boolean isBatchingEnabled() {
        return true;
    }

}
