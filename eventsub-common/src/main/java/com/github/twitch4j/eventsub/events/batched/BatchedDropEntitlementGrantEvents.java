package com.github.twitch4j.eventsub.events.batched;

import com.github.twitch4j.eventsub.events.DropEntitlementGrantEvent;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BatchedDropEntitlementGrantEvents extends BatchedDataEvents<DropEntitlementGrantEvent> {}
