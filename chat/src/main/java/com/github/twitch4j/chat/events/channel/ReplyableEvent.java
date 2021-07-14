package com.github.twitch4j.chat.events.channel;

import com.github.twitch4j.chat.ITwitchChat;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.util.CryptoUtils;

@FunctionalInterface
public interface ReplyableEvent {

    IRCMessageEvent getMessageEvent();

    default EventChannel getChannel() {
        return getMessageEvent().getChannel();
    }

    /**
     * Sends a reply to this chat message.
     *
     * @param chat    the {@link ITwitchChat} instance to send the message from.
     * @param message the message to be sent.
     */
    default void reply(ITwitchChat chat, String message) {
        chat.sendMessage(getChannel().getName(), message, CryptoUtils.generateNonce(32), getMessageEvent().getMessageId().orElse(null));
    }

}
