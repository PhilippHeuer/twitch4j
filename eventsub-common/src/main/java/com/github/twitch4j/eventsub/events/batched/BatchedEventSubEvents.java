package com.github.twitch4j.eventsub.events.batched;

import com.github.twitch4j.eventsub.events.EventSubEvent;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract class BatchedEventSubEvents<E extends EventSubEvent> extends EventSubEvent {

    private List<E> events; // do not rename without understanding NotificationDeserializer

}
