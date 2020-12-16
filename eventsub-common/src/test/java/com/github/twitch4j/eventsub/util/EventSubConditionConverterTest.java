package com.github.twitch4j.eventsub.util;

import com.github.twitch4j.common.util.CryptoUtils;
import com.github.twitch4j.eventsub.condition.ChannelBanCondition;
import com.github.twitch4j.eventsub.condition.ChannelPointsCustomRewardRedemptionUpdateCondition;
import com.github.twitch4j.eventsub.condition.EventSubCondition;
import com.github.twitch4j.eventsub.condition.UserAuthorizationRevokeCondition;
import com.github.twitch4j.eventsub.condition.UserUpdateCondition;
import com.github.twitch4j.eventsub.subscriptions.ChannelBanType;
import com.github.twitch4j.eventsub.subscriptions.ChannelPointsCustomRewardRedemptionUpdateType;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionType;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes;
import com.github.twitch4j.eventsub.subscriptions.UserAuthorizationRevokeType;
import com.github.twitch4j.eventsub.subscriptions.UserUpdateType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

@Tag("integration")
public class EventSubConditionConverterTest {

    @Test
    @DisplayName("Convert map to app condition")
    public void convertAppCondition() {
        UserAuthorizationRevokeType type = SubscriptionTypes.USER_AUTHORIZATION_REVOKE;
        UserAuthorizationRevokeCondition condition = type.getConditionBuilder().clientId(CryptoUtils.generateNonce(30)).build();
        test(type, condition);
    }

    @Test
    @DisplayName("Convert map to channel condition")
    public void convertChannelCondition() {
        ChannelBanType type = SubscriptionTypes.CHANNEL_BAN;
        ChannelBanCondition condition = type.getConditionBuilder().broadcasterUserId("42069").build();
        test(type, condition);
    }

    @Test
    @DisplayName("Convert map to reward condition")
    public void convertRedemptionCondition() {
        ChannelPointsCustomRewardRedemptionUpdateType type = SubscriptionTypes.CHANNEL_POINTS_CUSTOM_REWARD_REDEMPTION_UPDATE;
        ChannelPointsCustomRewardRedemptionUpdateCondition condition = type.getConditionBuilder()
            .broadcasterUserId("42069")
            .rewardId(UUID.randomUUID().toString())
            .build();
        test(type, condition);
    }

    @Test
    @DisplayName("Convert map to user condition")
    public void convertUserCondition() {
        UserUpdateType type = SubscriptionTypes.USER_UPDATE;
        UserUpdateCondition condition = type.getConditionBuilder().userId("1337").build();
        test(type, condition);
    }

    private static void test(SubscriptionType<?, ?> type, EventSubCondition condition) {
        Assertions.assertEquals(condition, EventSubConditionConverter.getCondition(type, condition.toMap()));
    }

}
