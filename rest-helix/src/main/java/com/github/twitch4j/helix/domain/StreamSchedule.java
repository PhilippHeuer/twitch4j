package com.github.twitch4j.helix.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@Setter(AccessLevel.PRIVATE)
public class StreamSchedule {

    /**
     * Scheduled broadcasts for this stream schedule.
     */
    private List<ScheduledSegment> segments;

    /**
     * User ID of the broadcaster.
     */
    private String broadcasterId;

    /**
     * Display name of the broadcaster.
     */
    private String broadcasterName;

    /**
     * Login name of the broadcaster.
     */
    private String broadcasterLogin;

    /**
     * If Vacation Mode is enabled, this includes start and end dates for the vacation.
     * If Vacation Mode is disabled, this value is set to null.
     */
    @Nullable
    private ScheduledVacation vacation;

}
