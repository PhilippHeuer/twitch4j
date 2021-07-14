package com.github.twitch4j.common.util;

import lombok.NonNull;
import lombok.Value;

import java.util.Map;

import static com.github.twitch4j.common.util.EscapeUtils.unescapeTagValue;

/**
 * Meta-info regarding the <i>parent</i> message being replied to.
 */
@Value
public class ChatReply {

    public static final String REPLY_MSG_ID_TAG_NAME = "reply-parent-msg-id";

    /**
     * The msgId of the original message being replied to.
     */
    @NonNull
    String messageId;

    /**
     * The text of the original message being replied to.
     */
    String messageBody;

    /**
     * The id of the user who originally sent the message being replied to.
     */
    String userId;

    /**
     * The login name of the user who originally sent the message being replied to.
     */
    String userLogin;

    /**
     * The display name of the user who originally sent the message being replied to.
     */
    String displayName;

    /**
     * Attempts to parse a {@link ChatReply} instance from chat tags.
     *
     * @param tags the message tags associated with a reply.
     * @return the parsed {@link ChatReply}, or null if unsuccessful
     */
    public static ChatReply parse(final Map<String, String> tags) {
        final String msgId;
        if (tags == null || (msgId = tags.get(REPLY_MSG_ID_TAG_NAME)) == null || msgId.isEmpty())
            return null;

        return new ChatReply(
            msgId,
            unescapeTagValue(tags.get("reply-parent-msg-body")),
            tags.get("reply-parent-user-id"),
            tags.get("reply-parent-user-login"),
            unescapeTagValue(tags.get("reply-parent-display-name"))
        );
    }
}
