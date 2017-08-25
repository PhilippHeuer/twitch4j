package me.philippheuer.twitch4j.events.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import me.philippheuer.twitch4j.model.Channel;
import me.philippheuer.twitch4j.model.User;

/**
 * This event gets called when a user receives bits.
 *
 * @author Philipp Heuer [https://github.com/PhilippHeuer]
 * @version %I%, %G%
 * @since 1.0
 */
@Data
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class CheerEvent extends AbstractChannelEvent {

	/**
	 * Event Target User
	 */
	private final User user;

	/**
	 * Message
	 */
	private final String message;

	/**
	 * Amount of Bits
	 */
	private final Integer bits;


	/**
	 * Event Constructor
	 *
	 * @param channel The channel that this event originates from.
	 * @param user The donating user.
	 * @param message The donation message.
	 * @param bits The amount of bits.
	 */
	public CheerEvent(Channel channel, User user, String message, Integer bits) {
		super(channel);
		this.user = user;
		this.message = message;
		this.bits = bits;
	}
}
