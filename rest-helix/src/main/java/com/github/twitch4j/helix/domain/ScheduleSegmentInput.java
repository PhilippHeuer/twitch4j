package com.github.twitch4j.helix.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@With
@Data
@Setter(AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduleSegmentInput {

    /**
     * Start time for the scheduled broadcast specified in RFC3339 format.
     */
    private Instant startTime;

    /**
     * The timezone of the application creating the scheduled broadcast using the IANA time zone database format.
     *
     * @see <a href="https://www.iana.org/time-zones">IANA Time Zone Database</a>
     */
    private String timezone;

    /**
     * Indicates if the scheduled broadcast is recurring weekly.
     */
    @JsonProperty("is_recurring")
    private Boolean recurring;

    /**
     * Indicated if the scheduled broadcast is canceled.
     */
    @JsonProperty("is_canceled")
    private Boolean canceled;

    /**
     * Duration of the scheduled broadcast in minutes from the start_time.
     * Default: 240.
     */
    @JsonProperty("duration")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Integer durationMinutes;

    /**
     * Game/Category ID for the scheduled broadcast.
     */
    private String categoryId;

    /**
     * Title for the scheduled broadcast.
     * Maximum: 140 characters.
     */
    private String title;

}
