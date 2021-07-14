package com.github.twitch4j.eventsub.events.batched;

import com.github.twitch4j.eventsub.events.EnclosedDataEvent;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class BatchedDataEvents<T> extends BatchedEventSubEvents<EnclosedDataEvent<T>> {

    public List<T> getData() {
        return getEvents().stream().map(EnclosedDataEvent::getData).collect(Collectors.toList());
    }

}
