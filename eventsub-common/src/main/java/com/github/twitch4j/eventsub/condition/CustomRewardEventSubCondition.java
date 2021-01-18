package com.github.twitch4j.eventsub.condition;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Data
@Setter(AccessLevel.PRIVATE)
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Jacksonized
public class CustomRewardEventSubCondition extends ChannelEventSubCondition {

    /**
     * Specify a reward id to only receive notifications for a specific reward.
     */
    private String rewardId;

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("broadcaster_user_id", getBroadcasterUserId());
        if (rewardId != null)
            map.put("reward_id", this.rewardId);
        return Collections.unmodifiableMap(map);
    }

}
