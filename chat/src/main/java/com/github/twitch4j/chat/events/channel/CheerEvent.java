package com.github.twitch4j.chat.events.channel;

import com.github.twitch4j.chat.events.AbstractChannelEvent;
import com.github.twitch4j.chat.flag.AutoModFlag;
import com.github.twitch4j.common.annotation.Unofficial;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import java.util.List;

/**
 * This event gets called when a user receives bits.
 */
@Value
@Getter
@EqualsAndHashCode(callSuper = false)
public class CheerEvent extends AbstractChannelEvent implements ReplyableEvent {

    /**
     * Raw IRC Message Event
     */
    IRCMessageEvent messageEvent;

	/**
	 * Event Target User
	 */
	private EventUser user;

	/**
	 * Message
	 */
	private String message;

	/**
	 * Amount of Bits
	 */
	private Integer bits;

    /**
     * The exact number of months the user has been a subscriber, or zero if not subscribed
     */
    private int subscriberMonths;

    /**
     * The tier at which the user is subscribed (prime is treated as 1), or zero if not subscribed
     */
    private int subscriptionTier;

    /**
     * Regions of {@link #getMessage()} that were flagged by AutoMod (Unofficial)
     */
    @Unofficial
    private List<AutoModFlag> flags;

    /**
     * Event Constructor
     *
     * @param event            The raw message event.
     * @param channel          The channel that this event originates from.
     * @param user             The donating user.
     * @param message          The donation message.
     * @param bits             The amount of bits.
     * @param subscriberMonths The exact number of months the user has been a subscriber.
     * @param subscriptionTier The tier at which the user is subscribed.
     * @param flags            The regions of the message that were flagged by AutoMod.
     */
	public CheerEvent(IRCMessageEvent event, EventChannel channel, EventUser user, String message, Integer bits, int subscriberMonths, int subscriptionTier, List<AutoModFlag> flags) {
		super(channel);
		this.messageEvent = event;
		this.user = user;
		this.message = message;
		this.bits = bits;
        this.subscriberMonths = subscriberMonths;
        this.subscriptionTier = subscriptionTier;
        this.flags = flags;
	}
}
