package me.philippheuer.twitch4j.events.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import me.philippheuer.twitch4j.events.Event;
import me.philippheuer.twitch4j.model.Channel;
import me.philippheuer.twitch4j.model.Subscription;
import me.philippheuer.twitch4j.model.User;

/**
 * This event gets called when a user gets a new subscriber or a user resubscribes.
 * <p>
 * This event will be called simultaneously with the chat announcement,
 * not when the user presses his subscription button.
 *
 * @author Philipp Heuer [https://github.com/PhilippHeuer]
 * @version %I%, %G%
 * @since 1.0
 */
@Data
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class SubscriptionEvent extends Event {

	/**
	 * Event Channel
	 */
	private final Channel channel;

	/**
	 * Event Target User
	 */
	private final User user;

	/**
	 * The Subscription
	 */
	private final Subscription subscription;

	/**
	 * Constructor
	 *
	 * @param channel      Channel
	 * @param subscription Subscription
	 */
	public SubscriptionEvent(Channel channel, Subscription subscription) {
		this.channel = channel;
		this.user = subscription.getUser();
		this.subscription = subscription;
	}

}
