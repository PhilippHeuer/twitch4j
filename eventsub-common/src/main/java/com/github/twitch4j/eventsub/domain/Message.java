package com.github.twitch4j.eventsub.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
public class Message {

    /**
     * The text of the re-subscription chat message.
     */
    private String text;

    /**
     * A collection that includes the emote ID and start and end positions for where the emote appears in the text.
     */
    private List<Emote> emotes;

    @Data
    @Setter(AccessLevel.PRIVATE)
    @NoArgsConstructor
    public static class Emote {

        /**
         * The index of where the Emote starts in the text.
         */
        private Integer begin;

        /**
         * The index of where the Emote ends in the text.
         */
        private Integer end;

        /**
         * The emote ID.
         */
        private String id;

    }

}
