package com.github.twitch4j.eventsub.subscriptions;

import com.github.twitch4j.common.annotation.Unofficial;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class SubscriptionTypes {
    private final Map<String, SubscriptionType<?, ?, ?>> SUBSCRIPTION_TYPES;
    public final ChannelBanType CHANNEL_BAN;
    public final ChannelCheerType CHANNEL_CHEER;
    public final ChannelFollowType CHANNEL_FOLLOW;
    public final ChannelModeratorAddType CHANNEL_MODERATOR_ADD;
    public final ChannelModeratorRemoveType CHANNEL_MODERATOR_REMOVE;
    public final ChannelPointsCustomRewardAddType CHANNEL_POINTS_CUSTOM_REWARD_ADD;
    public final ChannelPointsCustomRewardRedemptionAddType CHANNEL_POINTS_CUSTOM_REWARD_REDEMPTION_ADD;
    public final ChannelPointsCustomRewardRedemptionUpdateType CHANNEL_POINTS_CUSTOM_REWARD_REDEMPTION_UPDATE;
    public final ChannelPointsCustomRewardRemoveType CHANNEL_POINTS_CUSTOM_REWARD_REMOVE;
    public final ChannelPointsCustomRewardUpdateType CHANNEL_POINTS_CUSTOM_REWARD_UPDATE;
    public final ChannelRaidType CHANNEL_RAID;
    public final ChannelSubscribeType CHANNEL_SUBSCRIBE;
    public final ChannelSubscriptionEndType CHANNEL_SUBSCRIPTION_END;
    public final ChannelSubscriptionGiftType CHANNEL_SUBSCRIPTION_GIFT;
    public final ChannelSubscriptionMessageType CHANNEL_SUBSCRIPTION_MESSAGE;
    public final ChannelUnbanType CHANNEL_UNBAN;
    public final ChannelUpdateType CHANNEL_UPDATE;
    @Unofficial public final BetaExtensionBitsTransactionCreateType BETA_EXTENSION_BITS_TRANSACTION_CREATE;
    public final HypeTrainBeginType HYPE_TRAIN_BEGIN;
    public final HypeTrainEndType HYPE_TRAIN_END;
    public final HypeTrainProgressType HYPE_TRAIN_PROGRESS;
    public final PollBeginType POLL_BEGIN;
    public final PollProgressType POLL_PROGRESS;
    public final PollEndType POLL_END;
    public final PredictionBeginType PREDICTION_BEGIN;
    public final PredictionProgressType PREDICTION_PROGRESS;
    public final PredictionLockType PREDICTION_LOCK;
    public final PredictionEndType PREDICTION_END;
    public final StreamOfflineType STREAM_OFFLINE;
    public final StreamOnlineType STREAM_ONLINE;
    public final UserAuthorizationRevokeType USER_AUTHORIZATION_REVOKE;
    public final UserUpdateType USER_UPDATE;

    public SubscriptionType<?, ?, ?> getSubscriptionType(String subTypeName, String subTypeVersion) {
        return SUBSCRIPTION_TYPES.get(subTypeName + ':' + subTypeVersion);
    }

    static {
        SUBSCRIPTION_TYPES = Collections.unmodifiableMap(
            Stream.of(
                CHANNEL_BAN = new ChannelBanType(),
                CHANNEL_CHEER = new ChannelCheerType(),
                CHANNEL_FOLLOW = new ChannelFollowType(),
                CHANNEL_MODERATOR_ADD = new ChannelModeratorAddType(),
                CHANNEL_MODERATOR_REMOVE = new ChannelModeratorRemoveType(),
                CHANNEL_POINTS_CUSTOM_REWARD_ADD = new ChannelPointsCustomRewardAddType(),
                CHANNEL_POINTS_CUSTOM_REWARD_REDEMPTION_ADD = new ChannelPointsCustomRewardRedemptionAddType(),
                CHANNEL_POINTS_CUSTOM_REWARD_REDEMPTION_UPDATE = new ChannelPointsCustomRewardRedemptionUpdateType(),
                CHANNEL_POINTS_CUSTOM_REWARD_REMOVE = new ChannelPointsCustomRewardRemoveType(),
                CHANNEL_POINTS_CUSTOM_REWARD_UPDATE = new ChannelPointsCustomRewardUpdateType(),
                CHANNEL_RAID = new ChannelRaidType(),
                CHANNEL_SUBSCRIBE = new ChannelSubscribeType(),
                CHANNEL_SUBSCRIPTION_END = new ChannelSubscriptionEndType(),
                CHANNEL_SUBSCRIPTION_GIFT = new ChannelSubscriptionGiftType(),
                CHANNEL_SUBSCRIPTION_MESSAGE = new ChannelSubscriptionMessageType(),
                CHANNEL_UNBAN = new ChannelUnbanType(),
                CHANNEL_UPDATE = new ChannelUpdateType(),
                BETA_EXTENSION_BITS_TRANSACTION_CREATE = new BetaExtensionBitsTransactionCreateType(),
                HYPE_TRAIN_BEGIN = new HypeTrainBeginType(),
                HYPE_TRAIN_END = new HypeTrainEndType(),
                HYPE_TRAIN_PROGRESS = new HypeTrainProgressType(),
                POLL_BEGIN = new PollBeginType(),
                POLL_PROGRESS = new PollProgressType(),
                POLL_END = new PollEndType(),
                PREDICTION_BEGIN = new PredictionBeginType(),
                PREDICTION_PROGRESS = new PredictionProgressType(),
                PREDICTION_LOCK = new PredictionLockType(),
                PREDICTION_END = new PredictionEndType(),
                STREAM_OFFLINE = new StreamOfflineType(),
                STREAM_ONLINE = new StreamOnlineType(),
                USER_AUTHORIZATION_REVOKE = new UserAuthorizationRevokeType(),
                USER_UPDATE = new UserUpdateType()
            ).collect(Collectors.toMap(type -> type.getName() + ':' + type.getVersion(), Function.identity()))
        );
    }
}
