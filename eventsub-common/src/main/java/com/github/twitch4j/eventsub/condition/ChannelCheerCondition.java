package com.github.twitch4j.eventsub.condition;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ChannelCheerCondition extends ChannelEventSubCondition {}
