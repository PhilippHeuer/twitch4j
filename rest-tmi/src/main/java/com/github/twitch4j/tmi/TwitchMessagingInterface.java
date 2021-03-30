package com.github.twitch4j.tmi;

import com.github.twitch4j.common.annotation.Unofficial;
import com.github.twitch4j.tmi.domain.BadgeSets;
import com.github.twitch4j.tmi.domain.Chatters;
import com.github.twitch4j.tmi.domain.HostList;
import com.netflix.hystrix.HystrixCommand;
import feign.Param;
import feign.RequestLine;

import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

/**
 * Twitch - Messaging Interface
 */
public interface TwitchMessagingInterface {

    /**
     * The default baseUrl to pass to {@link #getGlobalBadges(URI, String)} and {@link #getChannelBadges(URI, String, String)}
     */
    @Unofficial
    URI BADGES_BASE_URL = ((Supplier<URI>) () -> {
        try {
            return new URI("https://badges.twitch.tv/v1");
        } catch (Exception e) {
            return null;
        }
    }).get();

    @Unofficial
    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    @RequestLine("GET /badges/global/display?language={language}")
    HystrixCommand<BadgeSets> getGlobalBadges(
        URI baseUrl,
        @Param("language") String language
    );

    /**
     * Get the chat badges that are globally available in chat.
     *
     * @param language Two-letter language code. Default: en.
     * @return BadgeSets
     */
    @Unofficial
    default HystrixCommand<BadgeSets> getGlobalBadges(String language) {
        return getGlobalBadges(BADGES_BASE_URL, language);
    }

    @Unofficial
    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    @RequestLine("GET /badges/channels/{channel_id}/display?language={language}")
    HystrixCommand<BadgeSets> getChannelBadges(
        URI baseUrl,
        @Param("channel_id") String channelId,
        @Param("language") String language
    );

    /**
     * Get the chat badges that are specific for the given channel.
     *
     * @param channelId The ID of the channel to query the badges of.
     * @param language  Two-letter language code. Default: en.
     * @return BadgeSets
     */
    @Unofficial
    default HystrixCommand<BadgeSets> getChannelBadges(String channelId, String language) {
        return getChannelBadges(BADGES_BASE_URL, channelId, language);
    }

    /**
     * Get Chatters
     * <p>
     * This endpoint returns all chatters in a channel. It's not official, bot pretty much every single bot depends on this so i doubt that they will ever remove it.
     *
     * @param channelName Channel Name
     * @return List of all Viewers/mods/...
     */
    @RequestLine("GET /group/user/{channel}/chatters")
    HystrixCommand<Chatters> getChatters(
        @Param("channel") String channelName
    );

    /**
     * Get Hosts
     * <p>
     * This endpoint returns a "host" record for each channel ID provided. If the channel is not hosting anyone,
     * the target_id and target_login fields will not be present.
     * Since it is not official and undocumented, this could disappear at any time.
     * Hopefully they would provide an official way to retrieve this data before removing it, though.
     *
     * @param channelIds A list containing a user ID for each channel to check.
     * @return Host information for each channel ID passed.
     */
    @RequestLine("GET /hosts?include_logins=1&host={id}")
    HystrixCommand<HostList> getHosts(
        @Param("id") List<String> channelIds
    );

    /**
     * Get Hosts of target channel
     * <p>
     * This endpoint returns a "host" record for each channel hosting the channel with the provided targetId.
     * It does not return the login of the target, only of the hosts.
     * Therefore getTargetLogin will return null for each Host in HostList.getHosts().
     * Since it is not official and undocumented, this could disappear at any time.
     * Hopefully they would provide an official way to retrieve this data before removing it, though.
     *
     * @param targetId The user ID of the channel for which to get host information.
     * @return List of hosts of the target channel.
     * @deprecated no longer functioning, so TwitchKraken#getHostsOf should be used
     */
    @Deprecated
    @RequestLine("GET /hosts?include_logins=1&target={id}")
    HystrixCommand<HostList> getHostsOf(
        @Param("id") String targetId
    );
}
