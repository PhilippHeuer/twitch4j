package com.github.twitch4j.eventsub;

import com.github.twitch4j.common.util.TypeConvert;
import com.github.twitch4j.eventsub.condition.ChannelFollowCondition;
import com.github.twitch4j.eventsub.condition.DropEntitlementGrantCondition;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.events.DropEntitlementGrantEvent;
import com.github.twitch4j.eventsub.events.batched.BatchedDropEntitlementGrantEvents;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unittest")
public class EventSubNotificationTest {

    @Test
    @DisplayName("Deserialize Challenge")
    public void deserializeChallenge() {
        String json = "{\"subscription\":{\"id\":\"b9ee086b-4ff1-4f11-8be8-9c93e3cd9515\",\"status\":\"webhook_callback_verification_pending\",\"type\":\"channel.follow\",\"version\":\"1\",\"condition\":{\"broadcaster_user_id\":\"207813352\"}," +
            "\"transport\":{\"method\":\"webhook\",\"callback\":\"https://twitch4j.ngrok.io\"},\"created_at\":\"2021-01-20T19:41:07.513199878Z\"},\"challenge\":\"sample-challenge\"}";

        EventSubNotification notif = TypeConvert.jsonToObject(json, EventSubNotification.class);
        assertEquals("sample-challenge", notif.getChallenge());
    }

    @Test
    @DisplayName("Deserialize Follow Notification")
    public void deserializeFollowNotification() {
        EventSubNotification notif = TypeConvert.jsonToObject(
            "{\"subscription\":{\"id\":\"f1c2a387-161a-49f9-a165-0f21d7a4e1c4\",\"type\":\"channel.follow\",\"version\":\"1\",\"status\":\"enabled\",\"condition\":{\"broadcaster_user_id\":\"1337\"},\"transport\":{\"method\":\"webhook\"," +
                "\"callback\":\"https://example.com/webhooks/callback\"},\"created_at\":\"2019-11-16T10:11:12.123Z\"},\"event\":{\"user_id\":\"1234\",\"user_login\":\"cool_user\",\"user_name\":\"Cool_User\",\"broadcaster_user_id\":\"1337\"," +
                "\"broadcaster_user_login\":\"cooler_user\",\"broadcaster_user_name\":\"Cooler_User\",\"followed_at\":\"2020-07-15T18:16:11.17106713Z\"}}",
            EventSubNotification.class
        );

        assertNotNull(notif);
        assertNotNull(notif.getSubscription());
        assertEquals("f1c2a387-161a-49f9-a165-0f21d7a4e1c4", notif.getSubscription().getId());
        assertEquals(EventSubSubscriptionStatus.ENABLED, notif.getSubscription().getStatus());
        assertEquals(SubscriptionTypes.CHANNEL_FOLLOW, notif.getSubscription().getType());
        assertEquals(ChannelFollowCondition.builder().broadcasterUserId("1337").build(), notif.getSubscription().getCondition());
        assertNotNull(notif.getSubscription().getTransport());
        assertEquals(EventSubTransportMethod.WEBHOOK, notif.getSubscription().getTransport().getMethod());
        assertEquals("https://example.com/webhooks/callback", notif.getSubscription().getTransport().getCallback());
        assertEquals(Instant.parse("2019-11-16T10:11:12.123Z"), notif.getSubscription().getCreatedAt());
        assertTrue(notif.getEvent() instanceof ChannelFollowEvent);
        ChannelFollowEvent event = (ChannelFollowEvent) notif.getEvent();
        assertEquals("1234", event.getUserId());
        assertEquals("cool_user", event.getUserLogin());
        assertEquals("Cool_User", event.getUserName());
        assertEquals("1337", event.getBroadcasterUserId());
        assertEquals("cooler_user", event.getBroadcasterUserLogin());
        assertEquals("Cooler_User", event.getBroadcasterUserName());
        assertEquals(Instant.parse("2020-07-15T18:16:11.17106713Z"), event.getFollowedAt());
    }

    @Test
    @DisplayName("Deserialize Batched Drop Entitlement Grant Notification")
    public void deserializeBatchedEntitlementEvent() {
        EventSubNotification notif = TypeConvert.jsonToObject(
            "{\"subscription\":{\"id\":\"f1c2a387-161a-49f9-a165-0f21d7a4e1c4\",\"type\":\"drop.entitlement.grant\",\"version\":\"1\",\"status\":\"enabled\",\"condition\":{\"organization_id\":\"9001\",\"category_id\":\"9002\",\"campaign_id\":\"9003\"}," +
                "\"transport\":{\"method\":\"webhook\",\"callback\":\"https://example.com/webhooks/callback\"},\"created_at\":\"2019-11-16T10:11:12.123Z\"},\"events\":[{\"id\":\"bf7c8577-e3e3-4881-a78a-e9446641d45d\"," +
                "\"data\":{\"organization_id\":\"9001\",\"category_id\":\"9002\",\"category_name\":\"Fortnite\",\"campaign_id\":\"9003\",\"user_id\":\"1234\",\"user_name\":\"Cool_User\",\"user_login\":\"cool_user\"," +
                "\"entitlement_id\":\"fb78259e-fb81-4d1b-8333-34a06ffc24c0\",\"benefit_id\":\"74c52265-e214-48a6-91b9-23b6014e8041\",\"created_at\":\"2019-01-28T04:17:53.325Z\"}},{\"id\":\"bf7c8577-e3e3-4881-a78a-e9446641d45c\"," +
                "\"data\":{\"organization_id\":\"9001\",\"category_id\":\"9002\",\"category_name\":\"Fortnite\",\"campaign_id\":\"9003\",\"user_id\":\"12345\",\"user_name\":\"Cooler_User\",\"user_login\":\"cooler_user\"," +
                "\"entitlement_id\":\"fb78259e-fb81-4d1b-8333-34a06ffc24c0\",\"benefit_id\":\"74c52265-e214-48a6-91b9-23b6014e8041\",\"created_at\":\"2019-01-28T04:17:53.325Z\"}}]}",
            EventSubNotification.class
        );

        assertNotNull(notif);
        assertNotNull(notif.getSubscription());
        assertEquals("f1c2a387-161a-49f9-a165-0f21d7a4e1c4", notif.getSubscription().getId());
        assertEquals(EventSubSubscriptionStatus.ENABLED, notif.getSubscription().getStatus());
        assertEquals(SubscriptionTypes.DROP_ENTITLEMENT_GRANT, notif.getSubscription().getType());
        assertEquals(DropEntitlementGrantCondition.builder().organizationId("9001").categoryId("9002").campaignId("9003").build(), notif.getSubscription().getCondition());
        assertNotNull(notif.getSubscription().getTransport());
        assertEquals(EventSubTransportMethod.WEBHOOK, notif.getSubscription().getTransport().getMethod());
        assertEquals("https://example.com/webhooks/callback", notif.getSubscription().getTransport().getCallback());
        assertEquals(Instant.parse("2019-11-16T10:11:12.123Z"), notif.getSubscription().getCreatedAt());
        assertTrue(notif.getEvent() instanceof BatchedDropEntitlementGrantEvents);
        BatchedDropEntitlementGrantEvents event = (BatchedDropEntitlementGrantEvents) notif.getEvent();
        assertEquals(2, event.getEvents().size());
        assertEquals("bf7c8577-e3e3-4881-a78a-e9446641d45d", event.getEvents().get(0).getId());
        assertEquals("bf7c8577-e3e3-4881-a78a-e9446641d45c", event.getEvents().get(1).getId());
        List<DropEntitlementGrantEvent> data = event.getData();
        assertNotNull(data);
        assertEquals(2, data.size());
        assertNotNull(data.get(0));
        assertEquals("9001", data.get(0).getOrganizationId());
        assertEquals("9003", data.get(0).getCampaignId());
        assertEquals("fb78259e-fb81-4d1b-8333-34a06ffc24c0", data.get(0).getEntitlementId());
        assertEquals("74c52265-e214-48a6-91b9-23b6014e8041", data.get(0).getBenefitId());
        assertEquals(Instant.parse("2019-01-28T04:17:53.325Z"), data.get(0).getCreatedAt());
        assertNotNull(data.get(1));
        assertEquals("12345", data.get(1).getUserId());
        assertEquals("Cooler_User", data.get(1).getUserName());
        assertEquals("cooler_user", data.get(1).getUserLogin());
    }

}
