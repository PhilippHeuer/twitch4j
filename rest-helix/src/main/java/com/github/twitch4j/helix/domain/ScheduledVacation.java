package com.github.twitch4j.helix.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.Instant;

@Data
@Setter(AccessLevel.PRIVATE)
public class ScheduledVacation {

    /**
     * Start time for vacation specified in RFC3339 format.
     */
    private Instant startTime;

    /**
     * End time for vacation specified in RFC3339 format.
     */
    private Instant endTime;

}
