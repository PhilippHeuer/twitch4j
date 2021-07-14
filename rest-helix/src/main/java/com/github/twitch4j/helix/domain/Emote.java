package com.github.twitch4j.helix.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.twitch4j.common.annotation.Unofficial;
import com.github.twitch4j.common.enums.SubscriptionPlan;
import com.github.twitch4j.helix.TwitchHelix;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Data
@Setter(AccessLevel.PRIVATE)
public class Emote {

    /**
     * ID of the emote.
     */
    private String id;

    /**
     * Name of the emote a viewer types into Twitch chat for the image to appear.
     */
    private String name;

    /**
     * The image URLs for the emote.
     */
    private Images images;

    /**
     * ID of the emote set the emote belongs to.
     * <p>
     * This is <i>not</i> present for {@link TwitchHelix#getGlobalEmotes(String)}.
     * This <i>is</i> present for {@link TwitchHelix#getChannelEmotes(String, String)} and {@link TwitchHelix#getEmoteSets(String, Collection)}.
     */
    @Nullable
    private String emoteSetId;

    /**
     * User ID of the broadcaster who owns the emote.
     * <p>
     * This is <i>only</i> present for {@link TwitchHelix#getEmoteSets(String, Collection)}.
     */
    @Nullable
    private String ownerId;

    /**
     * The type of emote.
     * <p>
     * This is <i>only</i> present for {@link TwitchHelix#getChannelEmotes(String, String)} and {@link TwitchHelix#getEmoteSets(String, Collection)}.
     */
    @Nullable
    private String emoteType;

    /**
     * The subscriber tier at which the emote is unlocked.
     * <p>
     * This is only present when {@link #getEmoteType()} equals {@link Type#SUBSCRIPTIONS}.
     * Also, this can only be present when using {@link TwitchHelix#getChannelEmotes(String, String)}.
     */
    @Nullable
    private SubscriptionPlan tier;

    /**
     * Attempts to parse this emoteType to a known {@link Type}.
     *
     * @return the parsed {@link Type}.
     * @see #getEmoteType()
     */
    @NonNull
    public Type getParsedEmoteType() {
        return Type.parseEmoteType(emoteType != null ? emoteType : "");
    }

    @Data
    @Setter(AccessLevel.PRIVATE)
    public static class Images {

        /**
         * Small emote image URL.
         */
        @JsonProperty("url_1x")
        private String smallImageUrl;

        /**
         * Medium emote image URL.
         */
        @JsonProperty("url_2x")
        private String mediumImageUrl;

        /**
         * Large emote image URL.
         */
        @JsonProperty("url_4x")
        private String largeImageUrl;

    }

    public enum Type {

        /**
         * Indicates a library-archived emote.
         */
        @Unofficial
        ARCHIVE,

        /**
         * Indicates a custom Bits tier emote.
         */
        BITS_TIER,

        /**
         * Indicates a channel points reward emote.
         */
        @Unofficial
        CHANNEL_POINTS,

        /**
         * Indicates a custom follower emote.
         */
        FOLLOWER,

        /**
         * Indicates a global emote.
         */
        @Unofficial
        GLOBALS,

        /**
         * Indicates a hype train emote.
         */
        @Unofficial
        HYPE_TRAIN,

        /**
         * Indicates a limited time emote.
         */
        @Unofficial
        LIMITED_TIME("limitedtime", "owl2019"),

        /**
         * Indicates a prime or turbo emote.
         */
        @Unofficial
        PRIME("prime", "turbo"),

        /**
         * Indicates a rewards emote.
         */
        @Unofficial
        REWARDS("rewards", "megacommerce", "megacheer"),

        /**
         * Indicates a smiley emote.
         */
        @Unofficial
        SMILIES,

        /**
         * Indicates a custom subscriber emote.
         */
        SUBSCRIPTIONS,

        /**
         * Indicates a two-factor emote.
         */
        @Unofficial
        TWO_FACTOR,

        /**
         * The channel emote type was unknown; please report on our Discord or Github!
         */
        OTHER;

        @NonNull
        private final String[] twitchStrings;

        Type() {
            this.twitchStrings = new String[] { this.name().toLowerCase().replace("_", "") };
        }

        Type(String... names) {
            this.twitchStrings = names;
        }

        private static final Map<String, Type> MAPPINGS;

        @NonNull
        public static Type parseEmoteType(String emoteType) {
            return MAPPINGS.getOrDefault(emoteType, OTHER);
        }

        static {
            final Map<String, Type> map = new HashMap<>();

            for (Type emoteType : values()) {
                for (String twitchString : emoteType.twitchStrings) {
                    map.put(twitchString, emoteType);
                }
            }

            MAPPINGS = Collections.unmodifiableMap(map);
        }

    }

}
