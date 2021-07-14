package com.github.twitch4j.chat;

import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.common.annotation.Unofficial;
import com.github.twitch4j.common.util.ChatReply;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public interface ITwitchChat extends AutoCloseable {

    EventManager getEventManager();

    /**
     * Joins a channel
     *
     * @param channelName The target channel name.
     */
    void joinChannel(String channelName);

    /**
     * Parts from a channel
     *
     * @param channelName The target channel name.
     * @return whether the channel was previously joined
     */
    boolean leaveChannel(String channelName);

    /**
     * Sends a message in a joined channel.
     *
     * @param channel The target channel name.
     * @param message The message to be sent.
     * @return whether the message was added to the queue
     */
    default boolean sendMessage(String channel, String message) {
        return this.sendMessage(channel, message, null);
    }

    /**
     * Sends a message to the channel while including an optional nonce and/or reply parent.
     *
     * @param channel    the name of the channel to send the message to.
     * @param message    the message to be sent.
     * @param nonce      the cryptographic nonce (optional).
     * @param replyMsgId the msgId of the parent message being replied to (optional).
     * @return whether the message was added to the queue
     */
    @SuppressWarnings("UnusedReturnValue")
    default boolean sendMessage(String channel, String message, @Unofficial String nonce, String replyMsgId) {
        final Map<String, Object> tags = new LinkedHashMap<>(); // maintain insertion order
        if (nonce != null) tags.put(IRCMessageEvent.NONCE_TAG_NAME, nonce);
        if (replyMsgId != null) tags.put(ChatReply.REPLY_MSG_ID_TAG_NAME, replyMsgId);
        return this.sendMessage(channel, message, tags);
    }

    /**
     * Sends a message to the channel while including the specified message tags.
     *
     * @param channel the name of the channel to send the message to.
     * @param message the message to be sent.
     * @param tags    the message tags.
     * @return whether the message was added to the queue
     */
    boolean sendMessage(String channel, String message, @Nullable Map<String, Object> tags);

    /**
     * Returns a set of all currently joined channels (without # prefix)
     *
     * @return a set of channel names
     */
    Set<String> getChannels();

    @Override
    void close();

    /**
     * @return cached mappings of channel ids to names
     */
    Map<String, String> getChannelIdToChannelName();

    /**
     * @return cached mappings of channel names to ids
     */
    Map<String, String> getChannelNameToChannelId();

    /**
     * Check if Chat is currently in a channel
     *
     * @param channelName channel to check (without # prefix)
     * @return boolean
     */
    default boolean isChannelJoined(String channelName) {
        return getChannels().contains(channelName.toLowerCase());
    }

    /**
     * Sends an action message (/me) in a joined channel.
     *
     * @param channel The target channel name.
     * @param message The message to be sent.
     * @return whether the message was added to the queue
     */
    default boolean sendActionMessage(String channel, String message) {
        return this.sendMessage(channel, String.format("/me %s", message));
    }

    /**
     * Configures the slow mode setting for a channel.
     * <p>
     * Non-positive values would disable slow mode.
     * Positive values must not be greater than 1800.
     *
     * @param channel The target channel name.
     * @param seconds The slow mode seconds.
     * @return whether the command was added to the queue
     */
    default boolean setSlowMode(String channel, int seconds) {
        if (seconds <= 0)
            return this.sendMessage(channel, "/slowoff");
        if (seconds > 1800)
            return false;
        return this.sendMessage(channel, String.format("/slow %d", seconds));
    }

    /**
     * Configures followers only mode in a channel.
     * <p>
     * Passing null (or a negative duration) for time disables followers only.
     * The amount of time must not exceed 3 months.
     *
     * @param channel The target channel name.
     * @param time    The amount of time users must be followed.
     * @return whether the command was added to the queue
     */
    default boolean setFollowersOnly(String channel, Duration time) {
        if (time == null || time.isNegative())
            return this.sendMessage(channel, "/followersoff");

        return this.sendMessage(channel, String.format("/followers %d", time.getSeconds()));
    }

    /**
     * Configures subscribers only mode in a channel.
     *
     * @param channel The target channel name.
     * @param enable  Whether the setting should be enabled or disabled.
     * @return whether the command was added to the queue
     */
    default boolean setSubscribersOnly(String channel, boolean enable) {
        return this.sendMessage(channel, enable ? "/subscribers" : "/subscribersoff");
    }

    /**
     * Configures unique chat mode (r9k) in a channel.
     *
     * @param channel The target channel name.
     * @param enable  Whether the setting should be enabled or disabled.
     * @return whether the command was added to the queue
     */
    default boolean setUniqueChat(String channel, boolean enable) {
        return this.sendMessage(channel, enable ? "/uniquechat" : "/uniquechatoff");
    }

    /**
     * Configures emote only mode in a channel.
     *
     * @param channel The target channel name.
     * @param enable  Whether the setting should be enabled or disabled.
     * @return whether the command was added to the queue
     */
    default boolean setEmoteOnly(String channel, boolean enable) {
        return this.sendMessage(channel, enable ? "/emoteonly" : "/emoteonlyoff");
    }

    /**
     * Clears the history for first-party chat clients.
     *
     * @param channel The target channel name.
     * @return whether the command was added to the queue
     */
    default boolean clearChat(String channel) {
        return this.sendMessage(channel, "/clear");
    }

    /**
     * Deletes a message.
     *
     * @param channel     the name of the channel to delete the message from.
     * @param targetMsgId the unique id of the message to be deleted.
     * @return whether the command was added to the queue
     * @see IRCMessageEvent#getMessageId()
     */
    default boolean delete(String channel, String targetMsgId) {
        return this.sendMessage(channel, String.format("/delete %s", targetMsgId));
    }

    /**
     * Timeout a user
     *
     * @param channel  channel
     * @param user     username
     * @param duration duration
     * @param reason   reason
     * @return whether the command was added to the queue
     */
    default boolean timeout(String channel, String user, Duration duration, String reason) {
        StringBuilder sb = new StringBuilder(user).append(' ').append(duration.getSeconds());
        if (reason != null) {
            sb.append(' ').append(reason);
        }

        return this.sendMessage(channel, String.format("/timeout %s", sb.toString()));
    }

    /**
     * Ban a user
     *
     * @param channel channel
     * @param user    username
     * @param reason  reason
     * @return whether the command was added to the queue
     */
    default boolean ban(String channel, String user, String reason) {
        StringBuilder sb = new StringBuilder(user);
        if (reason != null) {
            sb.append(' ').append(reason);
        }

        return this.sendMessage(channel, String.format("/ban %s", sb.toString()));
    }

    /**
     * Unban a user
     *
     * @param channel channel
     * @param user    username
     * @return whether the command was added to the queue
     */
    default boolean unban(String channel, String user) {
        return this.sendMessage(channel, String.format("/unban %s", user));
    }

}
