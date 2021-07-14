package com.github.twitch4j.helix;

import com.github.twitch4j.common.feign.ObjectToJsonExpander;
import com.github.twitch4j.eventsub.EventSubSubscription;
import com.github.twitch4j.eventsub.EventSubSubscriptionStatus;
import com.github.twitch4j.eventsub.domain.RedemptionStatus;
import com.github.twitch4j.helix.domain.*;
import com.github.twitch4j.helix.webhooks.domain.WebhookRequest;
import com.netflix.hystrix.HystrixCommand;
import feign.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Twitch - Helix API
 */
public interface TwitchHelix {

    /**
     * The default baseUrl to pass to {@link #getIngestServers(URI)}.
     */
    URI INGESTS_BASE_URL = ((Supplier<URI>) () -> {
        // Not pretty but needed for "Overriding the Request Line" - see: https://github.com/OpenFeign/feign/blob/master/README.md#interface-annotations
        try {
            return new URI("https://ingest.twitch.tv/");
        } catch (Exception e) {
            return null;
        }
    }).get();

    /**
     * Gets a URL that extension developers can use to download analytics reports (CSV files) for their extensions. The URL is valid for 5 minutes.
     * <p>
     * For detail about analytics and the fields returned, see the Insights and Analytics guide.
     *
     * @param authToken   Auth Token
     * @param after       Cursor for forward pagination: tells the server where to start fetching the next set of results, in a multi-page response.
     * @param limit       Maximum number of objects to return. Maximum: 100. Default: 20.
     * @param extensionId Client ID value assigned to the extension when it is created. If this is specified, the returned URL points to an analytics report for just the specified extension.
     * @param type        Type of analytics report that is returned. If this is specified, the response includes one URL, for the specified report type. If this is not specified, the response includes multiple URLs (paginated), one for each report type available for the authenticated user’s Extensions. Limit: 1. Valid values: "overview_v1", "overview_v2".
     * @param startedAt   Starting date/time for returned reports, in RFC3339 format with the hours, minutes, and seconds zeroed out and the UTC timezone: YYYY-MM-DDT00:00:00Z.
     * @param endedAt     Ending date/time for returned reports, in RFC3339 format with the hours, minutes, and seconds zeroed out and the UTC timezone: YYYY-MM-DDT00:00:00Z.
     * @return ExtensionAnalyticsList
     */
    @RequestLine("GET /analytics/extensions?after={after}&ended_at={ended_at}&first={first}&extension_id={extension_id}&started_at={started_at}&type={type}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<ExtensionAnalyticsList> getExtensionAnalyticUrl(
        @Param("token") String authToken,
        @Param("after") String after,
        @Param("first") Integer limit,
        @Param("extension_id") String extensionId,
        @Param("type") String type,
        @Param("started_at") String startedAt,
        @Param("ended_at") String endedAt
    );

    /**
     * Gets a URL that game developers can use to download analytics reports (CSV files) for their games. The URL is valid for 5 minutes.
     * <p>
     * For detail about analytics and the fields returned, see the Insights and Analytics guide.
     *
     * @param authToken Auth Token
     * @param after     Cursor for forward pagination: tells the server where to start fetching the next set of results, in a multi-page response.
     * @param limit     Maximum number of objects to return. Maximum: 100. Default: 20.
     * @param gameId    Game ID. If this is specified, the returned URL points to an analytics report for just the specified game.
     * @param type      Type of analytics report that is returned. If this is specified, the response includes one URL, for the specified report type. If this is not specified, the response includes multiple URLs (paginated), one for each report type available for the authenticated user’s Extensions. Limit: 1. Valid values: "overview_v1", "overview_v2".
     * @param startedAt Starting date/time for returned reports, in RFC3339 format with the hours, minutes, and seconds zeroed out and the UTC timezone: YYYY-MM-DDT00:00:00Z.
     * @param endedAt   Ending date/time for returned reports, in RFC3339 format with the hours, minutes, and seconds zeroed out and the UTC timezone: YYYY-MM-DDT00:00:00Z.
     * @return GameAnalyticsList
     */
    @RequestLine("GET /analytics/games?after={after}&ended_at={ended_at}&first={first}&game_id={game_id}&started_at={started_at}&type={type}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<GameAnalyticsList> getGameAnalyticUrl(
        @Param("token") String authToken,
        @Param("after") String after,
        @Param("first") Integer limit,
        @Param("game_id") String gameId,
        @Param("type") String type,
        @Param("started_at") String startedAt,
        @Param("ended_at") String endedAt
    );

    /**
     * Retrieves the list of available Cheermotes, animated emotes to which viewers can assign Bits, to cheer in chat
     *
     * @param authToken Auth Token
     * @param broadcasterId ID for the broadcaster who might own specialized Cheermotes (optional)
     * @return CheermoteList
     */
    @RequestLine("GET /bits/cheermotes?broadcaster_id={broadcaster_id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<CheermoteList> getCheermotes(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId
    );

    /**
     * Gets a ranked list of Bits leaderboard information for an authorized broadcaster.
     *
     * @param authToken Auth Token
     * @param count     Number of results to be returned. Maximum: 100. Default: 10.
     * @param period    Time period over which data is aggregated (PST time zone). This parameter interacts with started_at. Valid values are given below. Default: "all".
     * @param startedAt Timestamp for the period over which the returned data is aggregated. Must be in RFC 3339 format. If this is not provided, data is aggregated over the current period; e.g., the current day/week/month/year. This value is ignored if period is "all".
     * @param userId    ID of the user whose results are returned; i.e., the person who paid for the Bits.
     * @return BitsLeaderboard
     */
    @RequestLine("GET /bits/leaderboard?count={count}&period={period}&started_at={started_at}&user_id={user_id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<BitsLeaderboard> getBitsLeaderboard(
        @Param("token") String authToken,
        @Param("count") Integer count,
        @Param("period") String period,
        @Param("started_at") String startedAt,
        @Param("user_id") String userId
    );

    /**
     * Gets a ranked list of Bits leaderboard information for an authorized broadcaster.
     *
     * @param authToken Auth Token
     * @param count     Number of results to be returned. Maximum: 100. Default: 10.
     * @param period    Time period over which data is aggregated (PST time zone). This parameter interacts with started_at. Valid values are given below. Default: "all".
     * @param startedAt Timestamp for the period over which the returned data is aggregated. Must be in RFC 3339 format. If this is not provided, data is aggregated over the current period; e.g., the current day/week/month/year. This value is ignored if period is "all".
     * @param userId    ID of the user whose results are returned; i.e., the person who paid for the Bits.
     * @return BitsLeaderboard
     * @deprecated utilize getBitsLeaderboard where count is an Integer
     */
    @Deprecated
    default HystrixCommand<BitsLeaderboard> getBitsLeaderboard(
        String authToken,
        String count,
        String period,
        String startedAt,
        String userId
    ) {
        return getBitsLeaderboard(authToken, count == null || count.isEmpty() ? null : Integer.parseInt(count), period, startedAt, userId);
    }

    /**
     * Creates a Custom Reward on a channel.
     * <p>
     * Query parameter broadcaster_id must match the user_id in the User-Access token.
     *
     * @param authToken     User access token for the broadcaster (scope: channel:manage:redemptions).
     * @param broadcasterId The id of the target channel, which must match the token user id.
     * @param newReward     The Custom Reward to be created.
     * @return CustomRewardList
     */
    @RequestLine("POST /channel_points/custom_rewards?broadcaster_id={broadcaster_id}")
    @Headers({
        "Authorization: Bearer {token}",
        "Content-Type: application/json"
    })
    HystrixCommand<CustomRewardList> createCustomReward(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        CustomReward newReward
    );

    /**
     * Deletes a Custom Reward on a channel.
     * <p>
     * Only rewards created programmatically by the same client_id can be deleted.
     * Any UNFULFILLED Custom Reward Redemptions of the deleted Custom Reward will be updated to the FULFILLED status.
     *
     * @param authToken     User access token for the broadcaster (scope: channel:manage:redemptions).
     * @param broadcasterId The id of the target channel, which must match the token user id.
     * @param rewardId      ID of the Custom Reward to delete, must match a Custom Reward on broadcaster_id’s channel.
     * @return 204 No Content upon a successful deletion
     */
    @RequestLine("DELETE /channel_points/custom_rewards?broadcaster_id={broadcaster_id}&id={id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<Void> deleteCustomReward(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("id") String rewardId
    );

    /**
     * Returns a list of Custom Reward objects for the Custom Rewards on a channel.
     * <p>
     * Developers only have access to update and delete rewards that were created programmatically by the same/calling client_id.
     * <p>
     * There is a limit of 50 Custom Rewards on a channel at a time. This includes both enabled and disabled Custom Rewards.
     *
     * @param authToken             User access token for the broadcaster (scope: channel:manage:redemptions).
     * @param broadcasterId         Required: The id of the target channel, which must match the token user id.
     * @param rewardIds             Optional: Filters the results to only returns reward objects for the Custom Rewards with matching ID. Maximum: 50.
     * @param onlyManageableRewards Optional: When set to true, only returns custom rewards that the calling client_id can manage. Default: false.
     * @return CustomRewardList
     */
    @RequestLine("GET /channel_points/custom_rewards?broadcaster_id={broadcaster_id}&id={id}&only_manageable_rewards={only_manageable_rewards}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<CustomRewardList> getCustomRewards(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("id") Collection<String> rewardIds,
        @Param("only_manageable_rewards") Boolean onlyManageableRewards
    );

    /**
     * Returns Custom Reward Redemption objects for a Custom Reward on a channel that was created by the same client_id.
     * <p>
     * Developers only have access to get and update redemptions for the rewards created programmatically by the same client_id.
     *
     * @param authToken     User access token for the broadcaster (scope: channel:manage:redemptions).
     * @param broadcasterId The id of the target channel, which must match the token user id.
     * @param rewardId      When ID is not provided, this parameter returns paginated Custom Reward Redemption objects for redemptions of the Custom Reward with ID reward_id.
     * @param redemptionIds When used, this param filters the results and only returns Custom Reward Redemption objects for the redemptions with matching ID. Maximum: 50.
     * @param status        When id is not provided, this param is required and filters the paginated Custom Reward Redemption objects for redemptions with the matching status.
     * @param sort          Sort order of redemptions returned when getting the paginated Custom Reward Redemption objects for a reward. One of: OLDEST, NEWEST. Default: OLDEST.
     * @param after         Cursor for forward pagination: tells the server where to start fetching the next set of results, in a multi-page response. This applies only to queries without ID.
     * @param limit         Number of results to be returned when getting the paginated Custom Reward Redemption objects for a reward. Maximum: 50. Default: 20.
     * @return CustomRewardRedemptionList
     */
    @RequestLine("GET /channel_points/custom_rewards/redemptions?broadcaster_id={broadcaster_id}&reward_id={reward_id}&id={id}&status={status}&sort={sort}&after={after}&first={first}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<CustomRewardRedemptionList> getCustomRewardRedemption(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("reward_id") String rewardId,
        @Param("id") Collection<String> redemptionIds,
        @Param("status") RedemptionStatus status,
        @Param("sort") String sort,
        @Param("after") String after,
        @Param("first") Integer limit
    );

    /**
     * Updates a Custom Reward created on a channel.
     * <p>
     * Only rewards created programmatically by the same client_id can be updated.
     *
     * @param authToken     User access token for the broadcaster (scope: channel:manage:redemptions).
     * @param broadcasterId The id of the target channel, which must match the token user id.
     * @param rewardId      ID of the Custom Reward to delete, must match a Custom Reward on broadcaster_id’s channel.
     * @param updatedReward A CustomReward object with specific field(s) updated.
     * @return CustomRewardList
     */
    @RequestLine("PATCH /channel_points/custom_rewards?broadcaster_id={broadcaster_id}&id={id}")
    @Headers({
        "Authorization: Bearer {token}",
        "Content-Type: application/json"
    })
    HystrixCommand<CustomRewardList> updateCustomReward(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("id") String rewardId,
        CustomReward updatedReward
    );

    /**
     * Updates the status of Custom Reward Redemption objects on a channel that are in the UNFULFILLED status.
     * <p>
     * Only redemptions for a reward created programmatically by the same client_id as attached to the access token can be updated.
     *
     * @param authToken     User access token for the broadcaster (scope: channel:manage:redemptions).
     * @param broadcasterId The id of the target channel, which must match the token user id.
     * @param rewardId      ID of the Custom Reward the redemptions to be updated are for.
     * @param redemptionIds ID of the Custom Reward Redemption to update, must match a Custom Reward Redemption on broadcaster_id’s channel. Max: 50.
     * @param newStatus     The new status to set redemptions to. Can be either FULFILLED or CANCELED. Updating to CANCELED will refund the user their points.
     * @return CustomRewardRedemptionList
     */
    @RequestLine("PATCH /channel_points/custom_rewards/redemptions?broadcaster_id={broadcaster_id}&reward_id={reward_id}&id={id}")
    @Headers({
        "Authorization: Bearer {token}",
        "Content-Type: application/json"
    })
    @Body("%7B\"status\":\"{status}\"%7D")
    HystrixCommand<CustomRewardRedemptionList> updateRedemptionStatus(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("reward_id") String rewardId,
        @Param("id") Collection<String> redemptionIds,
        @Param("status") RedemptionStatus newStatus
    );

    /**
     * Gets a list of custom chat badges that can be used in chat for the specified channel.
     * This includes <a href="https://help.twitch.tv/s/article/subscriber-badge-guide">subscriber badges</a> and <a href="https://help.twitch.tv/s/article/custom-bit-badges-guide">Bit badges</a>.
     *
     * @param authToken     User OAuth Token from the broadcaster.
     * @param broadcasterId The id of the broadcaster whose chat badges are being requested. Provided broadcaster_id must match the user_id in the user OAuth token.
     * @return ChatBadgeSetList
     */
    @RequestLine("GET /chat/badges?broadcaster_id={broadcaster_id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<ChatBadgeSetList> getChannelChatBadges(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId
    );

    /**
     * Gets a list of chat badges that can be used in chat for any channel.
     *
     * @param authToken User OAuth Token or App Access Token.
     * @return ChatBadgeSetList
     */
    @RequestLine("GET /chat/badges/global")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<ChatBadgeSetList> getGlobalChatBadges(
        @Param("token") String authToken
    );

    /**
     * Gets the status of one or more provided codes.
     * <p>
     * The API is throttled to one request per second per authenticated user.
     *
     * @param authToken App access token. The client ID associated with the app access token must be approved by Twitch as part of a contracted arrangement.
     * @param code      The code to get the status of. 1-20 code parameters are allowed.
     * @param userId    Represents a numeric Twitch user ID. The user account which is going to receive the entitlement associated with the code.
     * @return CodeStatusList
     */
    @RequestLine("GET /entitlements/codes?code={code}&user_id={user_id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<CodeStatusList> getCodeStatus(
        @Param("token") String authToken,
        @Param("code") List<String> code,
        @Param("user_id") Integer userId
    );

    /**
     * Redeems one or more provided codes to the authenticated Twitch user.
     * <p>
     * The API is throttled to one request per second per authenticated user.
     *
     * @param authToken App access token. The client ID associated with the app access token must be one approved by Twitch.
     * @param code      The code to redeem to the authenticated user’s account. 1-20 code parameters are allowed.
     * @param userId    Represents a numeric Twitch user ID. The user account which is going to receive the entitlement associated with the code.
     * @return CodeStatusList
     */
    @RequestLine("POST /entitlements/codes?code={code}&user_id={user_id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<CodeStatusList> redeemCode(
        @Param("token") String authToken,
        @Param("code") List<String> code,
        @Param("user_id") Integer userId
    );

    /**
     * Gets a list of entitlements for a given organization that have been granted to a game, user, or both.
     * <p>
     * Valid combinations of requests are:
     * <ul>
     *     <li>No fields - All entitlements with benefits owned by your organization.</li>
     *     <li>Only userId - All entitlements for a user with benefits owned by your organization.</li>
     *     <li>Only gameId - All entitlements for all users for a game. Your organization must own the game.</li>
     *     <li>Both userId and gameId - All entitlements for the game granted to a user. Your organization must own the game.</li>
     * </ul>
     * <p>
     * Pagination support: Forward only
     *
     * @param authToken Required: App Access OAuth Token. OAuth Token Client ID must have ownership of Game: Client ID {@literal >} RBAC Organization ID {@literal >} Game ID.
     * @param id        Optional: Unique Identifier of the entitlement.
     * @param userId    Optional: A Twitch User ID.
     * @param gameId    Optional: A Twitch Game ID.
     * @param after     Optional: The cursor used to fetch the next page of data.
     * @param limit     Optional: Maximum number of entitlements to return. Default: 20. Max: 1000.
     * @return DropsEntitlementList
     */
    @RequestLine("GET /entitlements/drops?id={id}&user_id={user_id}&game_id={game_id}&after={after}&first={first}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<DropsEntitlementList> getDropsEntitlements(
        @Param("token") String authToken,
        @Param("id") String id,
        @Param("user_id") String userId,
        @Param("game_id") String gameId,
        @Param("after") String after,
        @Param("first") Integer limit
    );

    /**
     * Creates an EventSub subscription.
     *
     * @param authToken    Required: App access token.
     * @param subscription Required: The subscription that is being created. Must include type, version, condition, and transport.
     * @return EventSubSubscriptionList
     */
    @RequestLine("POST /eventsub/subscriptions")
    @Headers({
        "Authorization: Bearer {token}",
        "Content-Type: application/json"
    })
    HystrixCommand<EventSubSubscriptionList> createEventSubSubscription(
        @Param("token") String authToken,
        EventSubSubscription subscription
    );

    /**
     * Delete an EventSub subscription.
     *
     * @param authToken      Required: App Access Token.
     * @param subscriptionId Required: The subscription ID for the subscription you want to delete.
     * @return 204 No Content upon a successful deletion
     */
    @RequestLine("DELETE /eventsub/subscriptions?id={id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<Void> deleteEventSubSubscription(
        @Param("token") String authToken,
        @Param("id") String subscriptionId
    );

    /**
     * Get a list of your EventSub subscriptions.
     *
     * @param authToken Required: App Access Token.
     * @param status    Optional: Include this parameter to filter subscriptions by their status.
     * @param after     Optional: Cursor for forward pagination.
     * @param limit     Optional: Maximum number of objects to return. Maximum: 100. Minimum: 10.
     * @return EventSubSubscriptionList
     */
    @RequestLine("GET /eventsub/subscriptions?status={status}&after={after}&first={first}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<EventSubSubscriptionList> getEventSubSubscriptions(
        @Param("token") String authToken,
        @Param("status") EventSubSubscriptionStatus status,
        @Param("after") String after,
        @Param("first") Integer limit
    );

    /**
     * Get Extension Transactions allows extension back end servers to fetch a list of transactions that have occurred for their extension across all of Twitch.
     *
     * @param authToken App Access  OAuth Token
     * @param extensionId ID of the extension to list transactions for.
     * @param transactionIds Transaction IDs to look up. Can include multiple to fetch multiple transactions in a single request. Maximum: 100
     * @param after The cursor used to fetch the next page of data. This only applies to queries without ID. If an ID is specified, it supersedes the cursor.
     * @param limit Maximum number of objects to return. Maximum: 100 Default: 20
     * @return ExtensionTransactionList
     */
    @RequestLine("GET /extensions/transactions?after={after}&extension_id={extension_id}&first={first}&id={id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<ExtensionTransactionList> getExtensionTransactions(
        @Param("token") String authToken,
        @Param("extension_id") String extensionId,
        @Param("id") List<String> transactionIds,
        @Param("after") String after,
        @Param("first") Integer limit
    );

    /**
     * Returns a list of games or categories that match the query via name either entirely or partially
     *
     * @param authToken Auth Token
     * @param query the search query
     * @param limit Maximum number of objects to return. Maximum: 100. Default: 20.
     * @param after Cursor for forward pagination
     * @return CategorySearchList
     */
    @RequestLine("GET /search/categories?after={after}&first={first}&query={query}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<CategorySearchList> searchCategories(
        @Param("token") String authToken,
        @Param("query") String query,
        @Param("first") Integer limit,
        @Param("after") String after
    );

    /**
     * Gets channel information for users
     *
     * @param authToken Auth Token
     * @param broadcasterIds IDs of the channels to be retrieved
     * @return ChannelInformationList
     */
    @RequestLine("GET /channels?broadcaster_id={broadcaster_id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<ChannelInformationList> getChannelInformation(
        @Param("token") String authToken,
        @Param("broadcaster_id") List<String> broadcasterIds
    );

    /**
     * Modifies channel information for users
     *
     * @param authToken Auth Token (scope: channel:manage:broadcast or user:edit:broadcast)
     * @param broadcasterId ID of the channel to be updated (required)
     * @param channelInformation {@link ChannelInformation} (at least one parameter must be provided)
     * @return 204 No Content upon a successful update
     */
    @RequestLine("PATCH /channels?broadcaster_id={broadcaster_id}")
    @Headers({
        "Authorization: Bearer {token}",
        "Content-Type: application/json"
    })
    HystrixCommand<Void> updateChannelInformation(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        ChannelInformation channelInformation
    );

    /**
     * Returns a list of channels (those that have streamed within the past 6 months) that match the query either entirely or partially
     *
     * @param authToken Auth Token
     * @param query the search query
     * @param limit Maximum number of objects to return. Maximum: 100. Default: 20.
     * @param after Cursor for forward pagination
     * @param liveOnly Filter results for live streams only. Default: false
     * @return ChannelSearchList
     */
    @RequestLine("GET /search/channels?after={after}&first={first}&live_only={live_only}&query={query}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<ChannelSearchList> searchChannels(
        @Param("token") String authToken,
        @Param("query") String query,
        @Param("first") Integer limit,
        @Param("after") String after,
        @Param("live_only") Boolean liveOnly
    );

    /**
     * Starts a commercial on a specified channel
     *
     * @param authToken Auth Token (scope: channel:edit:commercial)
     * @param broadcasterId ID of the channel requesting a commercial
     * @param length Desired length of the commercial in seconds. Valid options are 30, 60, 90, 120, 150, 180.
     * @return CommercialList
     */
    @RequestLine("POST /channels/commercial")
    @Headers({
        "Authorization: Bearer {token}",
        "Content-Type: application/json"
    })
    @Body("%7B\"broadcaster_id\":\"{broadcaster_id}\",\"length\":{length}%7D")
    HystrixCommand<CommercialList> startCommercial(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("length") Integer length
    );

    /**
     * Gets a list of users who have editor permissions for a specific channel.
     *
     * @param authToken     Required: User access token of the broadcaster with the channel:read:editors scope.
     * @param broadcasterId Required: Broadcaster’s user ID associated with the channel.
     * @return ChannelEditorList
     * @see com.github.twitch4j.auth.domain.TwitchScopes#HELIX_CHANNEL_EDITORS_READ
     */
    @RequestLine("GET /channels/editors?broadcaster_id={broadcaster_id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<ChannelEditorList> getChannelEditors(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId
    );

    /**
     * Creates a clip programmatically. This returns both an ID and an edit URL for the new clip.
     *
     * @param authToken     Auth Token
     * @param broadcasterId ID of the stream from which the clip will be made.
     * @param hasDelay      If false, the clip is captured from the live stream when the API is called; otherwise, a delay is added before the clip is captured (to account for the brief delay between the broadcaster’s stream and the viewer’s experience of that stream). Default: false.
     * @return CreateClip
     */
    @RequestLine("POST /clips?broadcaster_id={broadcaster_id}&has_delay={has_delay}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<CreateClipList> createClip(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("has_delay") Boolean hasDelay
    );

    /**
     * Gets clip information by clip ID (one or more), broadcaster ID (one only), or game ID (one only).
     * Using user-token or app-token to increase rate limits.
     *
     * @param authToken User or App auth Token, for increased rate-limits
     * @param broadcasterId ID of the broadcaster for whom clips are returned. The number of clips returned is determined by the first query-string parameter (default: 20). Results are ordered by view count.
     * @param gameId        ID of the game for which clips are returned. The number of clips returned is determined by the first query-string parameter (default: 20). Results are ordered by view count.
     * @param id            ID of the clip being queried. Limit: 100.
     * @param after         Cursor for forward pagination: tells the server where to start fetching the next set of results, in a multi-page response.
     * @param before        Cursor for backward pagination: tells the server where to start fetching the next set of results, in a multi-page response.
     * @param limit         Maximum number of objects to return. Maximum: 100. Default: 20.
     * @param startedAt     Starting date/time for returned clips, in RFC3339 format. (Note that the seconds value is ignored.)
     * @param endedAt       Ending date/time for returned clips, in RFC3339 format. (Note that the seconds value is ignored.)
     * @return ClipList Clip List
     */
    @RequestLine("GET /clips?broadcaster_id={broadcaster_id}&game_id={game_id}&id={id}&after={after}&before={before}&first={first}&started_at={started_at}&ended_at={ended_at}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<ClipList> getClips(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("game_id") String gameId,
        @Param("id") String id,
        @Param("after") String after,
        @Param("before") String before,
        @Param("first") Integer limit,
        @Param("started_at") Instant startedAt,
        @Param("ended_at") Instant endedAt
    );

    /**
     * Creates a URL where you can upload a manifest file and notify users that they have an entitlement
     *
     * @param authToken App access token
     * @param manifestId Unique identifier of the manifest file to be uploaded. Must be 1-64 characters.
     * @param type Type of entitlement being granted. Only "bulk_drops_grant" is supported.
     * @return UploadEntitlementUrlList
     */
    @RequestLine("POST /entitlements/upload?manifest_id={manifest_id}&type={type}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<UploadEntitlementUrlList> createEntitlementUploadUrl(
        @Param("token") String authToken,
        @Param("manifest_id") String manifestId,
        @Param("type") String type
    );

    /**
     * Gets game information by game ID or name.
     * Using user-token or app-token to increase rate limits.
     *
     * @param authToken User or App auth Token, for increased rate-limits
     * @param id Game ID. At most 100 id values can be specified.
     * @param name Game name. The name must be an exact match. For instance, â€œPokemonâ€� will not return a list of Pokemon games; instead, query the specific Pokemon game(s) in which you are interested. At most 100 name values can be specified.
     * @return GameList
     */
    @RequestLine("GET /games?id={id}&name={name}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<GameList> getGames(
        @Param("token") String authToken,
        @Param("id") List<String> id,
        @Param("name") List<String> name
    );

    /**
     * Gets the information of the most recent Hype Train of the given channel ID.
     * After 5 days, if no Hype Train has been active, the endpoint will return an empty response
     *
     * @param authToken Auth Token (scope: channel:read:hype_train)
     * @param broadcasterId User ID of the broadcaster (required)
     * @param limit Maximum number of objects to return. Maximum: 100. Default: 1. (optional)
     * @param id The id of the wanted event, if known. (optional)
     * @param after Cursor for forward pagination (optional)
     * @return HypeTrainEventList
     */
    @RequestLine("GET /hypetrain/events?broadcaster_id={broadcaster_id}&first={first}&id={id}&after={after}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<HypeTrainEventList> getHypeTrainEvents(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("first") Integer limit,
        @Param("id") String id,
        @Param("after") String after
    );

    @RequestLine("GET /ingests")
    HystrixCommand<IngestServerList> getIngestServers(URI baseUrl);

    /**
     * Get Ingest Servers returns a list of endpoints for ingesting live video into Twitch.
     *
     * @return IngestServerList
     */
    default HystrixCommand<IngestServerList> getIngestServers() {
        return getIngestServers(INGESTS_BASE_URL);
    }

    /**
     * Allow or deny a message that was held for review by AutoMod.
     * <p>
     * In order to retrieve messages held for review, use ITwitchPubsub#listenForModerationEvents(OAuth2Credential, String, String) and ChatModerationEvent.
     * <p>
     * Note that the scope allows this endpoint to be used for any channel that the authenticated user is a moderator, including their own channel.
     *
     * @param authToken   User OAuth token from a moderator with the moderator:manage:automod scope.
     * @param moderatorId The moderator who is approving or rejecting the held message. Must match the user_id in the user OAuth token.
     * @param messageId   ID of the message to be allowed or denied. These message IDs are retrieved from IRC or PubSub.
     * @param action      The action to take for the message.
     * @return 204 No Content upon a successful request.
     * @see com.github.twitch4j.auth.domain.TwitchScopes#HELIX_AUTOMOD_MANAGE
     */
    @RequestLine("POST /moderation/automod/message")
    @Headers({
        "Authorization: Bearer {token}",
        "Content-Type: application/json"
    })
    @Body("%7B\"user_id\":\"{user_id}\",\"msg_id\":\"{msg_id}\",\"action\":\"{action}\"%7D")
    HystrixCommand<Void> manageAutoModHeldMessage(
        @Param("token") String authToken,
        @Param("user_id") String moderatorId,
        @Param("msg_id") String messageId,
        @Param("action") AutoModHeldMessageAction action
    );

    /**
     * Returns all banned and timed-out users in a channel.
     *
     * @param authToken     Auth Token (scope: moderation:read)
     * @param broadcasterId Required: Provided broadcaster_id must match the user_id in the auth token.
     * @param userId        Optional: Filters the results for only those with a matching user_id. Maximum: 100.
     * @param after         Optional: Cursor for forward pagination.
     * @param before        Optional: Cursor for backward pagination.
     * @param limit         Maximum number of objects to return. Maximum: 100. Default: 20.
     * @return BannedUserList
     */
    @RequestLine("GET /moderation/banned?broadcaster_id={broadcaster_id}&user_id={user_id}&after={after}&before={before}&first={first}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<BannedUserList> getBannedUsers(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("user_id") List<String> userId,
        @Param("after") String after,
        @Param("before") String before,
        @Param("first") Integer limit
    );

    /**
     * Returns all banned and timed-out users in a channel.
     *
     * @param authToken     Auth Token (scope: moderation:read)
     * @param broadcasterId Required: Provided broadcaster_id must match the user_id in the auth token.
     * @param userId        Optional: Filters the results for only those with a matching user_id. Maximum: 100.
     * @param after         Optional: Cursor for forward pagination.
     * @param before        Optional: Cursor for backward pagination.
     * @return BannedUserList
     * @deprecated in favor of getBannedUsers(String, String, List, String, String, Integer) where the last param is the number of objects to retrieve.
     */
    @Deprecated
    default HystrixCommand<BannedUserList> getBannedUsers(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("user_id") List<String> userId,
        @Param("after") String after,
        @Param("before") String before
    ) {
        return this.getBannedUsers(authToken, broadcasterId, userId, after, before, 20);
    }

    /**
     * Returns all user bans and un-bans in a channel.
     *
     * @param authToken     Auth Token (scope: moderation:read)
     * @param broadcasterId Required: Provided broadcaster_id must match the user_id in the auth token.
     * @param userId        Optional: Filters the results and only returns a status object for users who are banned in this channel and have a matching user_id. Maximum: 100.
     * @param after         Optional: Cursor for forward pagination.
     * @param limit         Optional: Maximum number of objects to return. Maximum: 100. Default: 20.
     * @return BannedEventList
     */
    @RequestLine("GET /moderation/banned/events?broadcaster_id={broadcaster_id}&user_id={user_id}&after={after}&first={first}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<BannedEventList> getBannedEvents(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("user_id") List<String> userId,
        @Param("after") String after,
        @Param("first") Integer limit
    );

    /**
     * Determines whether a string message meets the channel’s AutoMod requirements.
     *
     * @param authToken     Auth Token (scope: moderation:read)
     * @param broadcasterId Provided broadcaster_id must match the user_id in the auth token.
     * @param messages      the messages to be checked
     * @return AutomodEnforceStatusList
     */
    @RequestLine("POST /moderation/enforcements/status?broadcaster_id={broadcaster_id}")
    @Headers({
        "Authorization: Bearer {token}",
        "Content-Type: application/json"
    })
    HystrixCommand<AutomodEnforceStatusList> checkAutomodStatus(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        AutomodEnforceCheckList messages
    );

    /**
     * Returns all moderators in a channel.
     *
     * @param authToken User Token for the broadcaster
     * @param broadcasterId Provided broadcaster_id must match the user_id in the auth token.
     * @param userIds Filters the results and only returns a status object for users who are moderators in this channel and have a matching user_id.
     * @param after Cursor for forward pagination: tells the server where to start fetching the next set of results, in a multi-page response. The cursor value specified here is from the pagination response field of a prior query.
     * @param limit Maximum number of objects to return. Maximum: 100. Default: 20.
     * @return ModeratorList
     */
    @RequestLine(value = "GET /moderation/moderators?broadcaster_id={broadcaster_id}&user_id={user_id}&after={after}&first={first}", collectionFormat = CollectionFormat.CSV)
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<ModeratorList> getModerators(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("user_id") List<String> userIds,
        @Param("after") String after,
        @Param("first") Integer limit
    );

    /**
     * Returns all moderators in a channel.
     *
     * @param authToken User Token for the broadcaster
     * @param broadcasterId Provided broadcaster_id must match the user_id in the auth token.
     * @param userIds Filters the results and only returns a status object for users who are moderators in this channel and have a matching user_id.
     * @param after Cursor for forward pagination: tells the server where to start fetching the next set of results, in a multi-page response. The cursor value specified here is from the pagination response field of a prior query.
     * @return ModeratorList
     * @deprecated in favor of getModerators(String, String, List, String, Integer) where the last param is the number of objects to retrieve.
     */
    @Deprecated
    default HystrixCommand<ModeratorList> getModerators(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("user_id") List<String> userIds,
        @Param("after") String after
    ) {
        return this.getModerators(authToken, broadcasterId, userIds, after, 20);
    }

    /**
     * Returns a list of moderators or users added and removed as moderators from a channel.
     *
     * @param authToken User Token for the broadcaster
     * @param broadcasterId Provided broadcaster_id must match the user_id in the auth token.
     * @param userIds Filters the results and only returns a status object for users who are moderators in this channel and have a matching user_id.
     * @param after Cursor for forward pagination: tells the server where to start fetching the next set of results, in a multi-page response. The cursor value specified here is from the pagination response field of a prior query.
     * @param limit Maximum number of objects to return. Maximum: 100. Default: 20.
     * @return ModeratorList
     */
    @RequestLine(value = "GET /moderation/moderators/events?broadcaster_id={broadcaster_id}&user_id={user_id}&after={after}&first={first}", collectionFormat = CollectionFormat.CSV)
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<ModeratorEventList> getModeratorEvents(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("user_id") List<String> userIds,
        @Param("after") String after,
        @Param("first") Integer limit
    );

    /**
     * Returns a list of moderators or users added and removed as moderators from a channel.
     *
     * @param authToken User Token for the broadcaster
     * @param broadcasterId Provided broadcaster_id must match the user_id in the auth token.
     * @param userIds Filters the results and only returns a status object for users who are moderators in this channel and have a matching user_id.
     * @param after Cursor for forward pagination: tells the server where to start fetching the next set of results, in a multi-page response. The cursor value specified here is from the pagination response field of a prior query.
     * @return ModeratorList
     * @deprecated in favor of getModeratorEvents(String, String, List, String, Integer) where the last param is the number of objects to retrieve.
     */
    @Deprecated
    default HystrixCommand<ModeratorEventList> getModeratorEvents(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("user_id") List<String> userIds,
        @Param("after") String after
    ) {
        return this.getModeratorEvents(authToken, broadcasterId, userIds, after, 20);
    }

    /**
     * Get information about all polls or specific polls for a Twitch channel.
     * <p>
     * Poll information is available for 90 days.
     *
     * @param authToken     Required: User OAuth token from the broadcaster with the channel:read:polls scope.
     * @param broadcasterId Required: The broadcaster running polls. Provided broadcaster_id must match the user_id in the user OAuth token.
     * @param pollIds       Optional: ID of a poll. Filters results to one or more specific polls. Not providing one or more IDs will return the full list of polls for the authenticated channel. Maximum: 100.
     * @param after         Optional: Cursor for forward pagination: The cursor value specified here is from the pagination response field of a prior query.
     * @param limit         Optional: Maximum number of objects to return. Maximum: 20. Default: 20.
     * @return PollsList
     * @see com.github.twitch4j.auth.domain.TwitchScopes#HELIX_CHANNEL_POLLS_READ
     */
    @RequestLine("GET /polls?broadcaster_id={broadcaster_id}&id={id}&after={after}&first={first}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<PollsList> getPolls(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("id") List<String> pollIds,
        @Param("after") String after,
        @Param("first") Integer limit
    );

    /**
     * Create a poll for a specific Twitch channel.
     *
     * @param authToken User OAuth token from the broadcaster with the channel:manage:polls scope.
     * @param poll      The new poll to be created. Required: broadcaster_id, title, choices, choice.title, duration. Optional: bits_voting_enabled, bits_per_vote, channel_points_voting_enabled, channel_points_per_vote.
     * @return PollsList
     * @see com.github.twitch4j.auth.domain.TwitchScopes#HELIX_CHANNEL_POLLS_MANAGE
     */
    @RequestLine("POST /polls")
    @Headers({
        "Authorization: Bearer {token}",
        "Content-Type: application/json"
    })
    HystrixCommand<PollsList> createPoll(
        @Param("token") String authToken,
        Poll poll
    );

    /**
     * End a poll that is currently active.
     *
     * @param authToken User OAuth token from the broadcaster with the channel:manage:polls scope.
     * @param poll      The poll to be ended. Required: broadcaster_id, id, status (must be TERMINATED or ARCHIVED, depending on whether it should be viewed publicly).
     * @return PollsList
     * @see com.github.twitch4j.auth.domain.TwitchScopes#HELIX_CHANNEL_POLLS_MANAGE
     */
    @RequestLine("PATCH /polls")
    @Headers({
        "Authorization: Bearer {token}",
        "Content-Type: application/json"
    })
    HystrixCommand<PollsList> endPoll(
        @Param("token") String authToken,
        Poll poll
    );

    /**
     * Get information about all Channel Points Predictions or specific Channel Points Predictions for a Twitch channel.
     *
     * @param authToken     Required: User OAuth token from the broadcaster with the channel:read:predictions scope.
     * @param broadcasterId Required: The broadcaster running Predictions. Provided broadcaster_id must match the user_id in the user OAuth token.
     * @param predictionId  Optional: ID of a Prediction. Filters results to one or more specific Predictions. Not providing one or more IDs will return the full list of Predictions for the authenticated channel. Maximum: 100
     * @param after         Optional: The cursor value specified here is from the pagination response field of a prior query.
     * @param limit         Optional: Maximum number of objects to return. Maximum: 20. Default: 20.
     * @return PredictionsList
     * @see com.github.twitch4j.auth.domain.TwitchScopes#HELIX_CHANNEL_PREDICTIONS_READ
     */
    @RequestLine("GET /predictions?broadcaster_id={broadcaster_id}&id={id}&after={after}&first={first}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<PredictionsList> getPredictions(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("id") List<String> predictionId,
        @Param("after") String after,
        @Param("first") Integer limit
    );

    /**
     * Create a Channel Points Prediction for a specific Twitch channel.
     *
     * @param authToken  User OAuth token from the broadcaster with the channel:manage:predictions scope.
     * @param prediction The prediction to be created. Required: broadcaster_id, title, outcomes, outcome.title, prediction_window.
     * @return PredictionsList
     * @see com.github.twitch4j.auth.domain.TwitchScopes#HELIX_CHANNEL_PREDICTIONS_MANAGE
     */
    @RequestLine("POST /predictions")
    @Headers({
        "Authorization: Bearer {token}",
        "Content-Type: application/json"
    })
    HystrixCommand<PredictionsList> createPrediction(
        @Param("token") String authToken,
        Prediction prediction
    );

    /**
     * Lock, resolve, or cancel a Channel Points Prediction.
     * <p>
     * Active Predictions can be updated to be "locked," "resolved," or "canceled."
     * Locked Predictions can be updated to be "resolved" or "canceled."
     *
     * @param authToken  User OAuth token from the broadcaster with the channel:manage:predictions scope.
     * @param prediction The prediction to be ended. Required: broadcaster_id, id, status (RESOLVED or CANCELED or LOCKED). Optional: winning_outcome_id (must be present for RESOLVED status).
     * @return PredictionsList
     * @see com.github.twitch4j.auth.domain.TwitchScopes#HELIX_CHANNEL_PREDICTIONS_MANAGE
     */
    @RequestLine("PATCH /predictions")
    @Headers({
        "Authorization: Bearer {token}",
        "Content-Type: application/json"
    })
    HystrixCommand<PredictionsList> endPrediction(
        @Param("token") String authToken,
        Prediction prediction
    );

    /**
     * Gets games sorted by number of current viewers on Twitch, most popular first.
     * Using user-token or app-token to increase rate limits.
     *
     * @param authToken User or App auth Token, for increased rate-limits
     * @param after Cursor for forward pagination: tells the server where to start fetching the next set of results, in a multi-page response. The cursor value specified here is from the pagination response field of a prior query.
     * @param before Cursor for backward pagination: tells the server where to start fetching the next set of results, in a multi-page response. The cursor value specified here is from the pagination response field of a prior query.
     * @param first Maximum number of objects to return. Maximum: 100. Default: 20.
     * @return GameList
     */
    @RequestLine("GET /games/top?after={after}&before={before}&first={first}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<GameTopList> getTopGames(
        @Param("token") String authToken,
        @Param("after") String after,
        @Param("before") String before,
        @Param("first") String first
    );

    /**
     * Gets all scheduled broadcasts or specific scheduled broadcasts from a channel’s stream schedule.
     *
     * @param authToken     User OAuth Token or App Access Token.
     * @param broadcasterId User ID of the broadcaster who owns the channel streaming schedule.
     * @param ids           The ID of the stream segment to return. Maximum: 100.
     * @param startTime     A timestamp in RFC3339 format to start returning stream segments from. If not specified, the current date and time is used.
     * @param utcOffset     A timezone offset for the requester specified in minutes. For example, a timezone that is +4 hours from GMT would be “240.” If not specified, “0” is used for GMT.
     * @param after         Cursor for forward pagination: tells the server where to start fetching the next set of results in a multi-page response. The cursor value specified here is from the pagination response field of a prior query.
     * @param limit         Maximum number of stream segments to return. Maximum: 25. Default: 20.
     * @return StreamScheduleResponse
     */
    @RequestLine("GET /schedule?broadcaster_id={broadcaster_id}&id={id}&start_time={start_time}&utc_offset={utc_offset}&first={first}&after={after}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<StreamScheduleResponse> getChannelStreamSchedule(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("id") Collection<String> ids,
        @Param("start_time") Instant startTime,
        @Param("utc_offset") String utcOffset,
        @Param("after") String after,
        @Param("first") Integer limit
    );

    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    @RequestLine("GET /schedule/icalendar?broadcaster_id={broadcaster_id}")
    HystrixCommand<Response> getChannelInternetCalendarResponse(
        @Param("broadcaster_id") String broadcasterId
    );

    /**
     * Gets all scheduled broadcasts from a channel’s stream schedule as an iCalendar.
     *
     * @param broadcasterId User ID of the broadcaster who owns the channel streaming schedule.
     * @return iCalendar data is returned according to RFC5545, in a multi-line String.
     */
    default String getChannelInternetCalendar(String broadcasterId) throws IOException {
        Response response = getChannelInternetCalendarResponse(broadcasterId).execute();
        try (InputStream is = response.body().asInputStream()) {
            return IOUtils.toString(is, response.charset());
        }
    }

    /**
     * Update the settings for a channel’s stream schedule.
     *
     * @param authToken       User OAuth Token of the broadcaster (scope: "channel:manage:schedule").
     * @param broadcasterId   User ID of the broadcaster who owns the channel streaming schedule. Provided broadcaster_id must match the user_id in the user OAuth token.
     * @param vacationEnabled Indicates whether Vacation Mode is enabled.
     * @param vacationStart   Start time for vacation specified in RFC3339 format. Required if is_vacation_enabled is set to true.
     * @param vacationEnd     End time for vacation specified in RFC3339 format. Required if is_vacation_enabled is set to true.
     * @param timezone        The timezone for when the vacation is being scheduled using the IANA time zone database format. Required if is_vacation_enabled is set to true.
     * @return 204 No Content upon a successful call.
     */
    @RequestLine("PATCH /schedule/settings?broadcaster_id={broadcaster_id}&is_vacation_enabled={is_vacation_enabled}&vacation_start_time={vacation_start_time}&vacation_end_time={vacation_end_time}&timezone={timezone}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<Void> updateChannelStreamSchedule(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("is_vacation_enabled") Boolean vacationEnabled,
        @Param("vacation_start_time") Instant vacationStart,
        @Param("vacation_end_time") Instant vacationEnd,
        @Param("timezone") String timezone
    );

    /**
     * Create a single scheduled broadcast or a recurring scheduled broadcast for a channel’s stream schedule.
     *
     * @param authToken     User OAuth Token of the broadcaster (scope: "channel:manage:schedule").
     * @param broadcasterId User ID of the broadcaster who owns the channel streaming schedule. Provided broadcaster_id must match the user_id in the user OAuth token.
     * @param segment       Properties of the scheduled broadcast (required: start_time, timezone, is_recurring).
     * @return StreamScheduleResponse
     */
    @RequestLine("POST /schedule/segment?broadcaster_id={broadcaster_id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<StreamScheduleResponse> createStreamScheduleSegment(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        ScheduleSegmentInput segment
    );

    /**
     * Update a single scheduled broadcast or a recurring scheduled broadcast for a channel’s stream schedule.
     *
     * @param authToken     User OAuth Token of the broadcaster (scope: "channel:manage:schedule").
     * @param broadcasterId User ID of the broadcaster who owns the channel streaming schedule. Provided broadcaster_id must match the user_id in the user OAuth token.
     * @param id            The ID of the streaming segment to update.
     * @param segment       Updated properties of the scheduled broadcast.
     * @return StreamScheduleResponse
     */
    @RequestLine("PATCH /schedule/segment?broadcaster_id={broadcaster_id}&id={id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<StreamScheduleResponse> updateStreamScheduleSegment(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("id") String id,
        ScheduleSegmentInput segment
    );

    /**
     * Delete a single scheduled broadcast or a recurring scheduled broadcast for a channel’s stream schedule.
     *
     * @param authToken     User OAuth Token of the broadcaster (scope: "channel:manage:schedule").
     * @param broadcasterId User ID of the broadcaster who owns the channel streaming schedule. Provided broadcaster_id must match the user_id in the user OAuth token.
     * @param id            The ID of the streaming segment to delete.
     * @return 204 No Content upon a successful call.
     */
    @RequestLine("DELETE /schedule/segment?broadcaster_id={broadcaster_id}&id={id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<Void> deleteStreamScheduleSegment(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("id") String id
    );

    /**
     * Gets information about active streams.
     * <p>
     * Streams are returned sorted by number of current viewers, in descending order.
     * Across multiple pages of results, there may be duplicate or missing streams, as viewers join and leave streams.
     *
     * @param authToken  User or App Access Token
     * @param after      Cursor for forward pagination: tells the server where to start fetching the next set of results, in a multi-page response. The cursor value specified here is from the pagination response field of a prior query.
     * @param before     Cursor for backward pagination: tells the server where to start fetching the next set of results, in a multi-page response. The cursor value specified here is from the pagination response field of a prior query.
     * @param limit      Maximum number of objects to return. Maximum: 100. Default: 20.
     * @param gameIds    Returns streams broadcasting a specified game ID. You can specify up to 100 IDs.
     * @param language   Stream language. You can specify up to 100 languages. A language value must be either the ISO 639-1 two-letter code for a supported stream language or "other".
     * @param userIds    Returns streams broadcast by one or more specified user IDs. You can specify up to 100 IDs.
     * @param userLogins Returns streams broadcast by one or more specified user login names. You can specify up to 100 names.
     * @return StreamList
     */
    @RequestLine("GET /streams?after={after}&before={before}&first={first}&game_id={game_id}&language={language}&user_id={user_id}&user_login={user_login}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<StreamList> getStreams(
        @Param("token") String authToken,
        @Param("after") String after,
        @Param("before") String before,
        @Param("first") Integer limit,
        @Param("game_id") List<String> gameIds,
        @Param("language") List<String> language,
        @Param("user_id") List<String> userIds,
        @Param("user_login") List<String> userLogins
    );

    /**
     * Gets information about active streams.
     *
     * @param authToken   User or App Access Token
     * @param after       Cursor for forward pagination: tells the server where to start fetching the next set of results, in a multi-page response. The cursor value specified here is from the pagination response field of a prior query.
     * @param before      Cursor for backward pagination: tells the server where to start fetching the next set of results, in a multi-page response. The cursor value specified here is from the pagination response field of a prior query.
     * @param limit       Maximum number of objects to return. Maximum: 100. Default: 20.
     * @param communityId Returns streams in a specified community ID. You can specify up to 100 IDs. No longer supported by twitch.
     * @param gameIds     Returns streams broadcasting a specified game ID. You can specify up to 100 IDs.
     * @param language    Stream language. You can specify up to 100 languages.
     * @param userIds     Returns streams broadcast by one or more specified user IDs. You can specify up to 100 IDs.
     * @param userLogins  Returns streams broadcast by one or more specified user login names. You can specify up to 100 names.
     * @return StreamList
     * @deprecated in favor of getStreams(String, String, String, Integer, List, List, List, List); simply remove the argument for communityId to migrate
     */
    @Deprecated
    default HystrixCommand<StreamList> getStreams(
        @Param("token") String authToken,
        @Param("after") String after,
        @Param("before") String before,
        @Param("first") Integer limit,
        @Param("community_id") List<UUID> communityId, // Now unsupported by twitch
        @Param("game_id") List<String> gameIds,
        @Param("language") String language,
        @Param("user_id") List<String> userIds,
        @Param("user_login") List<String> userLogins
    ) {
        return getStreams(authToken, after, before, limit, gameIds, language != null ? Collections.singletonList(language) : null, userIds, userLogins);
    }

    /**
     * Gets information about active streams belonging to channels that the authenticated user follows.
     * <p>
     * Streams are returned sorted by number of current viewers, in descending order.
     * Across multiple pages of results, there may be duplicate or missing streams, as viewers join and leave streams.
     *
     * @param authToken Required: OAuth user token with the user:read:follows scope.
     * @param userId    Required: Results will only include active streams from the channels that this Twitch user follows. This must match the User ID in the bearer token.
     * @param after     Optional: Cursor for forward pagination.
     * @param limit     Optional: Maximum number of objects to return. Maximum: 100. Default: 100.
     * @return StreamList
     * @see com.github.twitch4j.auth.domain.TwitchScopes#HELIX_USER_FOLLOWS_READ
     */
    @RequestLine("GET /streams/followed?after={after}&first={first}&user_id={user_id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<StreamList> getFollowedStreams(
        @Param("token") String authToken,
        @Param("user_id") String userId,
        @Param("after") String after,
        @Param("first") Integer limit
    );

    /**
     * Gets the channel stream key for a user
     *
     * @param authToken Auth Token (scope: channel:read:stream_key)
     * @param broadcasterId User ID of the broadcaster
     * @return StreamKeyList
     */
    @RequestLine("GET /streams/key?broadcaster_id={broadcaster_id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<StreamKeyList> getStreamKey(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId
    );

    /**
     * Gets available Twitch stream tags.
     *
     * @param authToken User Token or App auth Token, for increased rate-limits
     * @param after     Cursor for forward pagination: tells the server where to start fetching the next set of results, in a multi-page response. The cursor value specified here is from the pagination response field of a prior query.
     * @param limit     Maximum number of objects to return. Maximum: 100. Default: 20.
     * @param tagIds    Returns tags by one or more specified tag IDs. You can specify up to 100 IDs. If you search by tagIds, no pagination is used.
     * @return StreamTagList
     */
    @RequestLine("GET /tags/streams?after={after}&first={first}&tag_id={tag_id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<StreamTagList> getAllStreamTags(
            @Param("token") String authToken,
            @Param("after") String after,
            @Param("first") Integer limit,
            @Param("tag_id") List<UUID> tagIds
    );

    /**
     * Gets stream tags which are active on the specified stream.
     *
     * @param authToken     User Token or App auth Token, for increased rate-limits
     * @param broadcasterId ID of the stream to fetch current tags from
     * @return StreamTagList
     */
    @RequestLine("GET /streams/tags?broadcaster_id={broadcaster_id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<StreamTagList> getStreamTags(
            @Param("token") String authToken,
            @Param("broadcaster_id") String broadcasterId
    );

    /**
     * Replaces the active stream tags on the specified stream with the specified tags (or clears all tags, if no new tags are specified).
     * Requires scope: channel:manage:broadcast or user:edit:broadcast
     *
     * @param authToken     Auth Token
     * @param broadcasterId ID of the stream to replace tags for
     * @param tagIds        Tag ids to replace the current stream tags with. Maximum: 100. If empty, all tags are cleared from the stream. Tags currently expire 72 hours after they are applied, unless the stream is live within that time period.
     * @return Object       nothing
     */
    @RequestLine("PUT /streams/tags?broadcaster_id={broadcaster_id}")
    @Headers({
        "Authorization: Bearer {token}",
        "Content-Type: application/json"
    })
    @Body("%7B\"tag_ids\": [{tag_ids}]%7D")
    HystrixCommand<Void> replaceStreamTags(
            @Param("token") String authToken,
            @Param("broadcaster_id") String broadcasterId,
            @Param(value = "tag_ids", expander = ObjectToJsonExpander.class) List<UUID> tagIds
    );

    /**
     * Creates a marker at the current time during a live stream. A marker is a temporal indicator that appears on the Twitch web UI for highlight creation and can also be retrieved with
     * {@link #getStreamMarkers(java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String) getStreamMarkers}. Markers are meant to remind streamers and their video editors of important moments during a stream.
     * Markers can be created only if the broadcast identified by the specified {@code userId} is live and has enabled VOD (past broadcast) storage. Marker creation will fail if the broadcaster is airing a premiere or a rerun.
     * Requires scope: channel:manage:broadcast or user:edit:broadcast
     *
     * @param authToken     Auth Token
     * @param highlight     User id and optional description for the marker
     * @return StreamMarker
     */
    @RequestLine("POST /streams/markers")
    @Headers({
        "Authorization: Bearer {token}",
        "Content-Type: application/json"
    })
    HystrixCommand<StreamMarker> createStreamMarker(
        @Param("token") String authToken,
        Highlight highlight
    );

    /**
     * Gets a list of markers for either a specified user’s most recent stream or a specified VOD/video (stream), ordered by recency. A marker is an arbitrary point in a stream that the broadcaster wants to mark; e.g., to easily return to later. The only markers returned are those created by the user identified by the Bearer token.
     *
     * @param authToken User Auth Token
     * @param after     Cursor for forward pagination: tells the server where to start fetching the next set of results, in a multi-page response. The cursor value specified here is from the pagination response field of a prior query.
     * @param before    Cursor for backward pagination: tells the server where to start fetching the next set of results, in a multi-page response. The cursor value specified here is from the pagination response field of a prior query.
     * @param limit     Maximum number of objects to return. Maximum: 100. Default: 20.
     * @param userId    ID of the broadcaster from whose stream markers are returned.
     * @param videoId   ID of the VOD/video whose stream markers are returned.
     * @return StreamMarkersList
     */
    @RequestLine("GET /streams/markers?after={after}&before={before}&first={first}&user_id={user_id}&video_id={video_id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<StreamMarkersList> getStreamMarkers(
        @Param("token") String authToken,
        @Param("after") String after,
        @Param("before") String before,
        @Param("first") Integer limit,
        @Param("user_id") String userId,
        @Param("video_id") String videoId
    );

    /**
     * Get all subscribers of a channel
     *
     * @param authToken User Auth Token
     * @param broadcasterId User ID of the broadcaster. Must match the User ID in the Bearer token.
     * @param after     Cursor for forward pagination: tells the server where to start fetching the next set of results, in a multi-page response. The cursor value specified here is from the pagination response field of a prior query.
     * @param before    Cursor for backward pagination: tells the server where to start fetching the next set of results, in a multi-page response. The cursor value specified here is from the pagination response field of a prior query.
     * @param limit     Maximum number of objects to return. Maximum: 100. Default: 20.
     * @return SubscriptionList
     */
    @RequestLine("GET /subscriptions?broadcaster_id={broadcaster_id}&after={after}&before={before}&first={first}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<SubscriptionList> getSubscriptions(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("after") String after,
        @Param("before") String before,
        @Param("first") Integer limit
    );

    /**
     * Check subscription status of provided user IDs (one or more) for a given channel
     *
     * @param authToken User Auth Token
     * @param broadcasterId User ID of the broadcaster. Must match the User ID in the Bearer token.
     * @param userIds Unique identifier of account to get subscription status of. Accepts up to 100 values.
     * @return SubscriptionList
     */
    @RequestLine("GET /subscriptions?broadcaster_id={broadcaster_id}&user_id={user_id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<SubscriptionList> getSubscriptionsByUser(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("user_id") List<String> userIds
    );

    /**
     * Returns all subscription events for the past five days.
     *
     * @param authToken     Required: User Auth Token (scope: channel:read:subscriptions).
     * @param broadcasterId Required: User ID of the broadcaster. Must match the User ID in the Bearer token.
     * @param id            Optional: Retrieve a single event by the event ID.
     * @param userId        Optional: ID of the subscribed user.
     * @param after         Optional: Cursor for forward pagination; where to start fetching the next set of results in a multi-page response. This applies only to queries without user_id.
     * @param limit         Optional: Limit the number of items in the response payload. Maximum: 100.
     * @return SubscriptionEventList
     */
    @RequestLine("GET /subscriptions/events?broadcaster_id={broadcaster_id}&id={id}&user_id={user_id}&after={after}&first={first}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<SubscriptionEventList> getSubscriptionEvents(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("id") String id,
        @Param("user_id") String userId,
        @Param("after") String after,
        @Param("first") Integer limit
    );

    /**
     * Checks if a specific user (user_id) is subscribed to a specific channel (broadcaster_id).
     *
     * @param authToken     Token with the user:read:subscriptions scope. App access works if the user has authorized your application.
     * @param broadcasterId User ID of an Affiliate or Partner broadcaster.
     * @param userId        User ID of a Twitch viewer.
     * @return SubscriptionList on success, error 404 if not subscribed
     */
    @RequestLine("GET /subscriptions/user?broadcaster_id={broadcaster_id}&user_id={user_id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<SubscriptionList> checkUserSubscription(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId,
        @Param("user_id") String userId
    );

    /**
     * Gets information for a specific Twitch Team.
     * <p>
     * One of the two optional query parameters must be specified to return Team information.
     *
     * @param authToken User OAuth Token or App Access Token.
     * @param name      Team name.
     * @param id        Team ID.
     * @return TeamList
     */
    @RequestLine("GET /teams?name={name}&id={id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<TeamList> getTeams(
        @Param("token") String authToken,
        @Param("name") String name,
        @Param("id") String id
    );

    /**
     * Retrieves a list of Twitch Teams of which the specified channel/broadcaster is a member.
     *
     * @param authToken     User OAuth Token or App Access Token
     * @param broadcasterId User ID for a Twitch user.
     * @return TeamMembershipList
     */
    @RequestLine("GET /teams/channel?broadcaster_id={broadcaster_id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<TeamMembershipList> getChannelTeams(
        @Param("token") String authToken,
        @Param("broadcaster_id") String broadcasterId
    );

    /**
     * Get Users
     * <p>
     * Gets information about one or more specified Twitch users. Users are identified by optional user IDs and/or login name. If neither a user ID nor a login name is specified, the user is looked up by Bearer token.
     *
     * @param authToken Auth Token (optionally with the user:read:email scope)
     * @param userIds   User ID. Multiple user IDs can be specified. Limit: 100.
     * @param userNames User login name. Multiple login names can be specified. Limit: 100.
     * @return HelixUser
     */
    @RequestLine("GET /users?id={id}&login={login}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<UserList> getUsers(
        @Param("token") String authToken,
        @Param("id") List<String> userIds,
        @Param("login") List<String> userNames
    );

    /**
     * Gets a specified user’s block list.
     * <p>
     * The list is sorted by when the block occurred in descending order (i.e. most recent block first).
     *
     * @param authToken Required: User Access Token with the user:read:blocked_users scope.
     * @param userId    Required: User ID for a Twitch user.
     * @param after     Optional: The cursor value specified here is from the pagination response field of a prior query.
     * @param limit     Optional: Maximum number of objects to return. Maximum: 100. Default: 20.
     * @return BlockedUserList
     * @see com.github.twitch4j.auth.domain.TwitchScopes#HELIX_USER_BLOCKS_READ
     */
    @RequestLine("GET /users/blocks?broadcaster_id={broadcaster_id}&after={after}&first={first}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<BlockedUserList> getUserBlockList(
        @Param("token") String authToken,
        @Param("broadcaster_id") String userId,
        @Param("after") String after,
        @Param("first") Integer limit
    );

    /**
     * Blocks the specified user on behalf of the authenticated user.
     *
     * @param authToken     Required: User Access Token with the user:manage:blocked_users scope.
     * @param targetUserId  Required: User ID of the user to be blocked.
     * @param sourceContext Optional: Source context for blocking the user. Valid values: "chat", "whisper".
     * @param reason        Optional: Reason for blocking the user. Valid values: "spam", "harassment", or "other".
     * @return 204 No Content upon a successful call.
     * @see com.github.twitch4j.auth.domain.TwitchScopes#HELIX_USER_BLOCKS_MANAGE
     */
    @RequestLine("PUT /users/blocks?target_user_id={target_user_id}&source_context={source_context}&reason={reason}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<Void> blockUser(
        @Param("token") String authToken,
        @Param("target_user_id") String targetUserId,
        @Param("source_context") String sourceContext,
        @Param("reason") String reason
    );

    /**
     * Unblocks the specified user on behalf of the authenticated user.
     *
     * @param authToken    Required: User Access Token with the user:manage:blocked_users scope.
     * @param targetUserId Required: User ID of the user to be unblocked.
     * @return 204 No Content upon a successful call.
     * @see com.github.twitch4j.auth.domain.TwitchScopes#HELIX_USER_BLOCKS_MANAGE
     */
    @RequestLine("DELETE /users/blocks?target_user_id={target_user_id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<Void> unblockUser(
        @Param("token") String authToken,
        @Param("target_user_id") String targetUserId
    );

    /**
     * Get Followers
     * <p>
     * Gets information on follow relationships between two Twitch users. Information returned is sorted in order, most recent follow first. This can return information like “who is lirik following,” “who is following lirik,” or “is user X following user Y.”
     * Using user-token or app-token to increase rate limits.
     *
     * @param authToken User or App auth Token, for increased rate-limits
     * @param fromId User ID. The request returns information about users who are being followed by the from_id user.
     * @param toId   User ID. The request returns information about users who are following the to_id user.
     * @param after  Cursor for forward pagination: tells the server where to start fetching the next set of results, in a multi-page response.
     * @param limit  Maximum number of objects to return. Maximum: 100. Default: 20.
     * @return FollowList
     */
    @RequestLine("GET /users/follows?from_id={from_id}&to_id={to_id}&after={after}&first={first}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<FollowList> getFollowers(
        @Param("token") String authToken,
        @Param("from_id") String fromId,
        @Param("to_id") String toId,
        @Param("after") String after,
        @Param("first") Integer limit
    );

    /**
     * Adds a specified user to the followers of a specified channel
     *
     * @param authToken Auth Token (scope: user:edit:follows)
     * @param fromId User ID of the follower
     * @param toId ID of the channel to be followed by the user
     * @param allowNotifications Whether the user gets email or push notifications (depending on the user’s notification settings) when the channel goes live. Default value is false
     * @return 204 No Content upon a successfully created follow
     */
    @RequestLine("POST /users/follows")
    @Headers({
        "Authorization: Bearer {token}",
        "Content-Type: application/json"
    })
    @Body("%7B\"from_id\":\"{from_id}\",\"to_id\":\"{to_id}\",\"allow_notifications\":{allow_notifications}%7D")
    HystrixCommand<Void> createFollow(
        @Param("token") String authToken,
        @Param("from_id") String fromId,
        @Param("to_id") String toId,
        @Param("allow_notifications") boolean allowNotifications
    );

    /**
     * Deletes a specified user from the followers of a specified channel
     *
     * @param authToken Auth Token (scope: user:edit:follows)
     * @param fromId User ID of the follower
     * @param toId Channel to be unfollowed by the user
     * @return 204 No Content upon a successful deletion from the list of channel followers
     */
    @RequestLine("DELETE /users/follows?from_id={from_id}&to_id={to_id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<Void> deleteFollow(
        @Param("token") String authToken,
        @Param("from_id") String fromId,
        @Param("to_id") String toId
    );

    /**
     * Update User
     * <p>
     * Updates the description of a user specified by a Bearer token.
     * Requires scope: user:edit
     *
     * @param authToken   Auth Token
     * @param description New user description
     * @return UserList
     */
    @RequestLine("PUT /users?description={description}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<UserList> updateUser(
        @Param("token") String authToken,
        @Param("description") String description
    );

    /**
     * Get User Extensions
     * <p>
     * Gets a list of all extensions (both active and inactive) for a specified user, identified by a Bearer token. The response has a JSON payload with a data field containing an array of user-information elements.
     * Required scope: user:read:broadcast
     *
     * @param authToken Auth Token
     * @return ExtensionList
     */
    @RequestLine("GET /users/extensions/list")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<ExtensionList> getUserExtensions(
        @Param("token") String authToken
    );

    /**
     * Get User Active Extensions
     * <p>
     * Gets information about active extensions installed by a specified user, identified by a user ID or Bearer token.
     * Optional scope: user:read:broadcast or user:edit:broadcast
     *
     * @param authToken Auth Token
     * @param userId    ID of the user whose installed extensions will be returned. Limit: 1.
     * @return ExtensionActiveList
     */
    @RequestLine("GET /users/extensions?user_id={user_id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<ExtensionActiveList> getUserActiveExtensions(
        @Param("token") String authToken,
        @Param("user_id") String userId
    );

    /**
     * Updates the activation state, extension ID, and/or version number of installed extensions for a specified user, identified by a Bearer token.
     * If you try to activate a given extension under multiple extension types, the last write wins (and there is no guarantee of write order).
     *
     * @param authToken Auth Token (scope: user:edit:broadcast)
     * @param extensions {@link ExtensionActiveList}
     * @return ExtensionActiveList
     */
    @RequestLine("PUT /users/extensions")
    @Headers({
        "Authorization: Bearer {token}",
        "Content-Type: application/json"
    })
    HystrixCommand<ExtensionActiveList> updateUserExtensions(
        @Param("token") String authToken,
        ExtensionActiveList extensions
    );

    /**
     * Get Videos
     * <p>
     * Gets video information by video ID (one or more), user ID (one only), or game ID (one only).
     * The response has a JSON payload with a data field containing an array of video elements. For lookup by user or game, pagination is available, along with several filters that can be specified as query string parameters.
     * Using user-token or app-token to increase rate limits.
     *
     * @param authToken User or App auth Token, for increased rate-limits
     * @param id        Required: ID of the video being queried. Limit: 100. If this is specified, you cannot use any of the optional query string parameters below.
     * @param userId    Required: ID of the user who owns the video. Limit 1.
     * @param gameId    Required: ID of the game the video is of. Limit 1.
     * @param language  Optional: Language of the video being queried. Limit: 1.
     * @param period    Optional: Period during which the video was created. Valid values: "all", "day", "week", "month". Default: "all".
     * @param sort      Optional: Sort order of the videos. Valid values: "time", "trending", "views". Default: "time".
     * @param type      Optional: Type of video. Valid values: "all", "upload", "archive", "highlight". Default: "all".
     * @param after     Optional: Cursor for forward pagination: tells the server where to start fetching the next set of results, in a multi-page response.
     * @param before    Optional: Cursor for backward pagination: tells the server where to start fetching the next set of results, in a multi-page response.
     * @param limit     Optional: Number of values to be returned when getting videos by user or game ID. Limit: 100. Default: 20.
     * @return VideoList
     */
    @RequestLine("GET /videos?id={id}&user_id={user_id}&game_id={game_id}&language={language}&period={period}&sort={sort}&type={type}&after={after}&before={before}&first={first}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<VideoList> getVideos(
        @Param("token") String authToken,
        @Param("id") String id,
        @Param("user_id") String userId,
        @Param("game_id") String gameId,
        @Param("language") String language,
        @Param("period") String period,
        @Param("sort") String sort,
        @Param("type") String type,
        @Param("after") String after,
        @Param("before") String before,
        @Param("first") Integer limit
    );

    /**
     * Deletes one or more videos. Videos are past broadcasts, Highlights, or uploads.
     * <p>
     * Invalid Video IDs will be ignored (i.e. IDs provided that do not have a video associated with it).
     * If the OAuth user token does not have permission to delete even one of the valid Video IDs, no videos will be deleted and the response will return a 401.
     *
     * @param authToken Required: User OAuth token with the channel:manage:videos scope.
     * @param ids       Required: ID of the video(s) to be deleted. Limit: 5.
     * @return DeletedVideoList
     * @see com.github.twitch4j.auth.domain.TwitchScopes#HELIX_CHANNEL_VIDEOS_MANAGE
     */
    @RequestLine("DELETE /videos?id={id}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<DeletedVideoList> deleteVideos(
        @Param("token") String authToken,
        @Param("id") List<String> ids
    );

    /**
     * Get Webhook Subscriptions
     * <p>
     * Gets the Webhook subscriptions of a user identified by a Bearer token, in order of expiration.
     *
     * The response has a JSON payload with a data field containing an array of subscription elements and a pagination field containing information required to query for more subscriptions.
     *
     * @param authToken User or App auth Token, for increased rate-limits
     * @param after    Cursor for forward pagination: tells the server where to start fetching the next set of results, in a multi-page response.
     * @param limit    Number of values to be returned when getting videos by user or game ID. Limit: 100. Default: 20.
     * @return WebhookSubscriptionList
     */
    @RequestLine("GET /webhooks/subscriptions?after={after}&first={first}")
    @Headers("Authorization: Bearer {token}")
    HystrixCommand<WebhookSubscriptionList> getWebhookSubscriptions(
        @Param("token") String authToken,
        @Param("after") String after,
        @Param("first") Integer limit
    );

    /**
     * Subscribe to or unsubscribe from events for a specified topic.
     *
     * <p>
     * Web hook response payloads mimic their respective New Twitch API endpoint responses (with minor omissions for unnecessary fields).
     * That is, a call to a web hook returns in its payload the same data as a call to the corresponding endpoint in the new Twitch API.
     * </p>
     *
     * <p>
     * When you submit a request to subscribe to an event (with the Subscribe To/Unsubscribe From Events endpoint), your request is asynchronously validated to confirm you are allowed to create the subscription.<br>
     * Depending on the results of this validation, Twitch responds by sending you one of two GET requests:
     * <p> * Subscription verify</p>
     * <p> * Subscription denied</p>
     *
     * @param request WebhookRequest to be converted to the Json body of the API call.
     * @param authToken   Auth Token
     *
     * @see <a href="https://dev.twitch.tv/docs/api/webhooks-guid/">Twitch Webhooks Guide</a>
     * @return The response from the server
     */
    @RequestLine("POST /webhooks/hub")
    @Headers({"Authorization: Bearer {token}", "content-type: application/json"})
    HystrixCommand<Response> requestWebhookSubscription(
        WebhookRequest request, // POJO as first arg is assumed by feign to be body if no @Body annotation
        @Param("token") String authToken
    );
}
