package com.github.twitch4j.pubsub.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatModerationAction {

    private String type;
    private String moderationAction;
    private List<String> args;
    private String createdBy;
    private String createdByUserId;
    private String msgId;
    private String targetUserId;
    private String targetUserLogin;
    private Boolean fromAutomod;

    /**
     * @return the specific moderation type that took place, in a convenient enum form
     */
    public ModerationType getModType() {
        return ModerationType.parse(type);
    }

    /**
     * @return the specific moderation action that took place, in a convenient enum form
     */
    public ModerationAction getModAction() {
        return ModerationAction.MAPPINGS.get(moderationAction);
    }

    /**
     * @return optional wrapper around the username that was specified in a ban, unban, timeout, untimeout, vip,
     * unvip, mod, unmod, host, or delete message command
     */
    public Optional<String> getTargetedUserName() {
        final ModerationAction action = getModAction();
        if (args != null && args.size() > 0 && (action == ModerationAction.BAN || action == ModerationAction.UNBAN
            || action == ModerationAction.TIMEOUT || action == ModerationAction.UNTIMEOUT
            || action == ModerationAction.DELETE || action == ModerationAction.HOST
            || action == ModerationAction.VIP || action == ModerationAction.UNVIP
            || action == ModerationAction.MOD || action == ModerationAction.UNMOD))
            return Optional.of(args.get(0));

        return Optional.ofNullable(this.targetUserLogin).filter(s -> !s.isEmpty());
    }

    /**
     * @return the reason associated with the ban or timeout (or an empty string if none was specified)
     */
    public Optional<String> getReason() {
        switch (getModAction()) {
            case BAN:
                if (args != null && args.size() > 1)
                    return Optional.of(args.get(1));

            case TIMEOUT:
                if (args != null && args.size() > 2)
                    return Optional.of(args.get(2));

            default:
                return Optional.empty();
        }
    }

    /**
     * @return optional wrapper around the message that was deleted
     */
    public Optional<String> getDeletedMessage() {
        if (getModAction() == ModerationAction.DELETE && args != null && args.size() > 1)
            return Optional.of(args.get(1));

        return Optional.empty();
    }

    /**
     * @return optional wrapper around the duration the user was timed out for, in seconds
     */
    public OptionalInt getTimeoutDuration() {
        if (getModAction() == ModerationAction.TIMEOUT && args != null && args.size() > 1)
            return OptionalInt.of(Integer.parseInt(args.get(1)));

        return OptionalInt.empty();
    }

    /**
     * @return optional wrapper around the slow mode delay, in seconds
     */
    public OptionalInt getSlowDuration() {
        if (getModAction() == ModerationAction.SLOW && args != null && args.size() > 0)
            return OptionalInt.of(Integer.parseInt(args.get(0)));

        return OptionalInt.empty();
    }

    /**
     * @return optional wrapper around the duration followers only mode was set to, in minutes
     */
    public OptionalInt getFollowersDuration() {
        if (getModAction() == ModerationAction.FOLLOWERS && args != null && args.size() > 0)
            return OptionalInt.of(Integer.parseInt(args.get(0)));

        return OptionalInt.empty();
    }

    /*
     * Convenience Enums
     */

    public enum ModerationType {
        CHANNEL,
        LOGIN;

        private static ModerationType parse(String twitchString) {
            if ("chat_channel_moderation".equals(twitchString))
                return CHANNEL;

            if ("chat_login_moderation".equals(twitchString))
                return LOGIN;

            return null; // should not occur
        }
    }

    public enum ModerationAction {
        /**
         * User was banned
         */
        BAN,
        /**
         * User was unbanned
         */
        UNBAN,
        /**
         * User was timed out
         */
        TIMEOUT,
        /**
         * User time out was removed
         */
        UNTIMEOUT,
        /**
         * Chat message was deleted
         */
        DELETE,
        /**
         * Chat slow mode was enabled
         */
        SLOW,
        /**
         * Chat slow mode was disabled
         */
        SLOW_OFF,
        /**
         * Chat followers only mode was enabled
         */
        FOLLOWERS,
        /**
         * Chat followers only mode was disabled
         */
        FOLLOWERS_OFF,
        /**
         * Unique chat mode was enabled
         */
        R9K_BETA,
        /**
         * Unique chat mode was disabled
         */
        R9K_BETA_OFF,
        /**
         * Emote only chat was enabled
         */
        EMOTE_ONLY,
        /**
         * Emote only chat was disabled
         */
        EMOTE_ONLY_OFF,
        /**
         * Subscribers-only chat was enabled
         */
        SUBSCRIBERS,
        /**
         * Subscribers-only chat was disabled
         */
        SUBSCRIBERS_OFF,
        /**
         * User was given VIP status
         */
        VIP,
        /**
         * User VIP status was removed
         */
        UNVIP,
        /**
         * User was modded
         */
        MOD,
        /**
         * User was unmodded
         */
        UNMOD,
        /**
         * Another channel was hosted
         */
        HOST,
        /**
         * Channel exited host mode
         */
        UNHOST;

        @Getter
        private final String twitchString;

        ModerationAction() {
            this.twitchString = this.name().toLowerCase().replace("_", "");
        }

        private static final Map<String, ModerationAction> MAPPINGS = Arrays.stream(ModerationAction.values())
            .collect(Collectors.toMap(ModerationAction::getTwitchString, Function.identity()));
    }
}
