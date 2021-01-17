package com.github.twitch4j.eventsub.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Contribution {

    /**
     * The ID of the user.
     */
    private String userId;

    /**
     * The display name of the user.
     */
    private String userName;

    /**
     * The login name of the user.
     */
    private String userLogin;

    /**
     * Type of contribution.
     */
    private Type type;

    /**
     * The total contributed.
     */
    private Integer total;

    public enum Type {
        @JsonProperty("bits")
        BITS,
        @JsonProperty("subscription")
        SUBSCRIPTION;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }

}
