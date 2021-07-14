package com.github.twitch4j.eventsub.events;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EnclosedDataEvent<T> extends EventSubEvent {

    /**
     * Individual event ID, as assigned by EventSub. Use this for de-duplicating messages.
     */
    private String id;

    /**
     * The object that contains the event data.
     */
    private T data;

}
