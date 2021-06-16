package com.github.twitch4j.eventsub.subscriptions;

import com.github.twitch4j.eventsub.condition.ExtensionBitsTransactionCreateCondition;
import com.github.twitch4j.eventsub.events.ExtensionBitsTransactionCreateEvent;

/**
 * This subscription type sends a notification when a new transaction is created for a Twitch Extension.
 * <p>
 * Authorization: The OAuth token client ID must match the Extension client ID.
 */
public class ExtensionBitsTransactionCreateType implements SubscriptionType<ExtensionBitsTransactionCreateCondition, ExtensionBitsTransactionCreateCondition.ExtensionBitsTransactionCreateConditionBuilder<?, ?>, ExtensionBitsTransactionCreateEvent> {

    @Override
    public String getName() {
        return "extension.bits_transaction.create";
    }

    @Override
    public String getVersion() {
        return "1";
    }

    @Override
    public ExtensionBitsTransactionCreateCondition.ExtensionBitsTransactionCreateConditionBuilder<?, ?> getConditionBuilder() {
        return ExtensionBitsTransactionCreateCondition.builder();
    }

    @Override
    public Class<ExtensionBitsTransactionCreateEvent> getEventClass() {
        return ExtensionBitsTransactionCreateEvent.class;
    }

}
