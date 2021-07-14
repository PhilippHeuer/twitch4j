package com.github.twitch4j.eventsub.events;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Data
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DropEntitlementGrantEvent extends EventSubUserEvent {

    /**
     * The ID of the organization that owns the game that has Drops enabled.
     */
    private String organizationId;

    /**
     * Twitch category ID of the game that was being played when this benefit was entitled.
     */
    private String categoryId;

    /**
     * The category name.
     */
    private String categoryName;

    /**
     * The campaign this entitlement is associated with.
     */
    private String campaignId;

    /**
     * Unique identifier of the entitlement. Use this to de-duplicate entitlements.
     */
    private String entitlementId;

    /**
     * Identifier of the Benefit.
     */
    private String benefitId;

    /**
     * UTC timestamp in ISO format when this entitlement was granted on Twitch.
     */
    private Instant createdAt;

}
