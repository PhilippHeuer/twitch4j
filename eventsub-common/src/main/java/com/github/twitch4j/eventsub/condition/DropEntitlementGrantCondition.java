package com.github.twitch4j.eventsub.condition;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Jacksonized
public class DropEntitlementGrantCondition extends CampaignEventSubCondition {}
