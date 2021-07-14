package com.github.twitch4j.helix.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.PRIVATE)
public class StreamScheduleResponse {

    @JsonProperty("data")
    private StreamSchedule schedule;

    private HelixPagination pagination;

}
