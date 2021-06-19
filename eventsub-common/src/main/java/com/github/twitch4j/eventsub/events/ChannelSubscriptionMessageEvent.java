package com.github.twitch4j.eventsub.events;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.github.twitch4j.common.enums.SubscriptionPlan;
import com.github.twitch4j.eventsub.domain.Message;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

@Data
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ChannelSubscriptionMessageEvent extends EventSubUserChannelEvent {

    /**
     * The tier of the user’s subscription.
     */
    private SubscriptionPlan tier;

    /**
     * An object that contains the re-subscription message and emote information needed to recreate the message.
     */
    private Message message;

    /**
     * The total number of months the user has been subscribed to the channel.
     */
    @JsonAlias("cumulative_total") // https://github.com/twitchdev/issues/issues/415
    private Integer cumulativeMonths;

    /**
     * The number of consecutive months the user’s current subscription has been active.
     * This value is null if the user has opted out of sharing this information.
     */
    @Nullable
    private Integer streakMonths;

    /**
     * The month duration of the subscription.
     */
    private Integer durationMonths;

}
