package com.github.twitch4j.pubsub.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@Setter(AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RadioTrack {

    private String asin;

    private Integer duration; // in seconds

    private String title;

    private List<Artist> artists;

    private AlbumInfo album;

    @Data
    @Setter(AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Artist {
        private String asin;
        private String name;
    }

    @Data
    @Setter(AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AlbumInfo {
        private String asin;
        private String name;
        @JsonProperty("imageURL")
        private String imageUrl;
    }

}
