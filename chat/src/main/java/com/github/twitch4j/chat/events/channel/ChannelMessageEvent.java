package com.github.twitch4j.chat.events.channel;

import com.github.twitch4j.chat.events.AbstractChannelEvent;
import com.github.twitch4j.chat.flag.AutoModFlag;
import com.github.twitch4j.common.annotation.Unofficial;
import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import java.util.List;
import java.util.Set;

/**
 * This event gets called when a message is received in a channel.
 */
@Value
@Getter
@EqualsAndHashCode(callSuper = false)
public class ChannelMessageEvent extends AbstractChannelEvent {

    /**
     * RAW Message Event
     */
    private final IRCMessageEvent messageEvent;

	/**
	 * User
	 */
	private EventUser user;

	/**
	 * Message
	 */
	private String message;

	/**
	 * Permissions of the user
	 */
	private Set<CommandPermission> permissions;

	/**
	 * Event Constructor
	 *
	 * @param channel     The channel that this event originates from.
     * @param messageEvent The raw message event
	 * @param user        The user who triggered the event.
	 * @param message     The plain text of the message.
	 * @param permissions The permissions of the triggering user.
	 */
	public ChannelMessageEvent(EventChannel channel, IRCMessageEvent messageEvent, EventUser user, String message, Set<CommandPermission> permissions) {
		super(channel);
        this.messageEvent = messageEvent;
		this.user = user;
		this.message = message;
		this.permissions = permissions;
	}

    /**
     * @return the regions of the message that were flagged by AutoMod.
     */
    @Unofficial
    public List<AutoModFlag> getFlags() {
        return this.messageEvent.getFlags();
    }
}
