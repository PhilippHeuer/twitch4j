package com.github.twitch4j.eventsub.condition;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Data
@Setter(AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@Jacksonized
public class CampaignEventSubCondition extends EventSubCondition {

    /**
     * The organization ID of the organization that owns the game on the developer portal.
     */
    private String organizationId;

    /**
     * The category (or game) ID of the game for which entitlement notifications will be received.
     */
    private String categoryId;

    /**
     * The campaign ID for a specific campaign for which entitlement notifications will be received.
     */
    private String campaignId;

}
