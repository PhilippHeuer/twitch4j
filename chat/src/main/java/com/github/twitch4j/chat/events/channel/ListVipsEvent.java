package com.github.twitch4j.chat.events.channel;

import com.github.twitch4j.chat.events.AbstractChannelEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ListVipsEvent extends AbstractChannelEvent {

    /**
     * The login names of the VIPs of this channel.
     */
    String[] vips;

    /**
     * Event Constructor
     *
     * @param channel The channel that this event originates from.
     * @param vips    The login names of the VIPs of this channel.
     */
    public ListVipsEvent(EventChannel channel, String[] vips) {
        super(channel);
        this.vips = vips;
    }

}
