package com.github.twitch4j.eventsub.subscriptions;

import com.github.twitch4j.eventsub.condition.UserAuthorizationGrantCondition;
import com.github.twitch4j.eventsub.events.UserAuthorizationGrantEvent;

/**
 * A user has granted authorization for your client id.
 * <p>
 * Provided client_id must match the client id in the application access token.
 * <p>
 * Unless otherwise noted, EventSub subscriptions that were released as a public beta will be available for 30 days after their v1 version is released. Subscriptions should be updated to v1 during this timeframe to continue its functionality.
 * Any active beta subscriptions beyond 30 days will be automatically deleted.
 */
public class BetaUserAuthorizationGrantType implements SubscriptionType<UserAuthorizationGrantCondition, UserAuthorizationGrantCondition.UserAuthorizationGrantConditionBuilder<?, ?>, UserAuthorizationGrantEvent> {

    @Override
    public String getName() {
        return "user.authorization.grant";
    }

    @Override
    public String getVersion() {
        return "beta";
    }

    @Override
    public UserAuthorizationGrantCondition.UserAuthorizationGrantConditionBuilder<?, ?> getConditionBuilder() {
        return UserAuthorizationGrantCondition.builder();
    }

    @Override
    public Class<UserAuthorizationGrantEvent> getEventClass() {
        return UserAuthorizationGrantEvent.class;
    }

}
