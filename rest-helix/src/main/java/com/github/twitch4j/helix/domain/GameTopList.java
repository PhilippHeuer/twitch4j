package com.github.twitch4j.helix.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Game Top List
 */
@Data
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
public class GameTopList {

    @JsonProperty("data")
    private List<Game> games;

    @JsonProperty("pagination")
    private HelixPagination pagination;

}
