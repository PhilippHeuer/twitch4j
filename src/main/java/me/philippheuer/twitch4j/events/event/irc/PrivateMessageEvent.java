package me.philippheuer.twitch4j.events.event.irc;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import me.philippheuer.twitch4j.events.Event;
import me.philippheuer.twitch4j.message.commands.CommandPermission;
import me.philippheuer.twitch4j.model.User;

import java.util.Set;

@Getter
public class PrivateMessageEvent extends Event {

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
	 * @param user        The user who triggered the event.
	 * @param message     The plain text of the message.
	 * @param permissions The permissions of the triggering user.
	 */
	public PrivateMessageEvent(User user, String message, Set<CommandPermission> permissions) {
		this.user = user;
		this.message = message;
		this.permissions = permissions;
	}
}
