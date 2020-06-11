package com.github.twitch4j.pubsub.events;

import com.github.twitch4j.pubsub.domain.Leaderboard;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SubLeaderboardEvent extends LeaderboardEvent {
    public SubLeaderboardEvent(Leaderboard data) {
        super(data);
    }
}
