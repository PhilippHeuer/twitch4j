package me.philippheuer.twitch4j.events.event.irc;

import lombok.*;
import me.philippheuer.twitch4j.events.event.AbstractChannelEvent;
import me.philippheuer.twitch4j.message.commands.CommandPermission;
import me.philippheuer.twitch4j.model.Channel;
import me.philippheuer.twitch4j.model.User;

import java.util.Set;

/**
 * This event gets called when a message is received in a channel.
 *
 * @author Philipp Heuer [https://github.com/PhilippHeuer]
 * @version %I%, %G%
 * @since 1.0
 */
@Data
@Getter
@Setter(AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class ChannelMessageEvent extends AbstractChannelEvent {

	/**
	 * User
	 */
	private final User user;

	/**
	 * Message
	 */
	private final String message;

	/**
	 * Permissions of the user
	 */
	private final Set<CommandPermission> permissions;

	/**
	 * Event Constructor
	 *
	 * @param channel     The channel that this event originates from.
	 * @param user        The user who triggered the event.
	 * @param message     The plain text of the message.
	 * @param permissions The permissions of the triggering user.
	 */
	public ChannelMessageEvent(Channel channel, User user, String message, Set<CommandPermission> permissions) {
		super(channel);
		this.user = user;
		this.message = message;
		this.permissions = permissions;
	}
}
