package com.github.twitch4j.helix.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

@Data
@Setter(AccessLevel.PRIVATE)
public class ScheduledSegment {

    /**
     * The ID for the scheduled broadcast.
     */
    private String id;

    /**
     * Scheduled start time for the scheduled broadcast in RFC3339 format.
     */
    private Instant startTime;

    /**
     * Scheduled end time for the scheduled broadcast in RFC3339 format.
     */
    private Instant endTime;

    /**
     * Title for the scheduled broadcast.
     */
    private String title;

    /**
     * Used with recurring scheduled broadcasts.
     * Specifies the date of the next recurring broadcast in RFC3339 format if one or more specific broadcasts have been deleted in the series.
     * Set to null otherwise.
     */
    @Nullable
    private Instant canceledUntil;

    /**
     * The category for the scheduled broadcast.
     * Set to null if no category has been specified.
     */
    @Nullable
    private Category category;

    /**
     * Indicates whether the scheduled broadcast is recurring weekly.
     */
    @Accessors(fluent = true)
    @JsonProperty("is_recurring")
    private Boolean isRecurring;

    @Data
    @Setter(AccessLevel.PRIVATE)
    public static class Category {

        /**
         * Game/category ID.
         */
        private String id;

        /**
         * Game/category name.
         */
        private String name;

    }

}
