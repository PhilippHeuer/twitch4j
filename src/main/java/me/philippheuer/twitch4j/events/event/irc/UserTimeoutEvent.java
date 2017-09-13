package me.philippheuer.twitch4j.events.event.irc;

import lombok.*;
import me.philippheuer.twitch4j.events.event.AbstractChannelEvent;
import me.philippheuer.twitch4j.model.Channel;
import me.philippheuer.twitch4j.model.User;

/**
 * This event gets called when a user get a timeout.
 *
 * @author Philipp Heuer [https://github.com/PhilippHeuer]
 * @version %I%, %G%
 * @since 1.0
 */
@Data
@Getter
@Setter(AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class UserTimeoutEvent extends AbstractChannelEvent {

	/**
	 * Event Target User
	 */
	private final User user;

	/**
	 * Duration in Minutes
	 */
	private final Integer duration;

	/**
	 * Reason for Punishment
	 */
	private final String reason;

	/**
	 * Event Constructor
	 *
	 * @param channel  The channel that this event originates from.
	 * @param user     The user who triggered the event.
	 * @param duration Timeout Duration in Minutes.
	 * @param reason   Reason for Ban.
	 */
	public UserTimeoutEvent(Channel channel, User user, Integer duration, String reason) {
		super(channel);
		this.user = user;
		this.duration = duration;
		this.reason = reason;
	}
}
