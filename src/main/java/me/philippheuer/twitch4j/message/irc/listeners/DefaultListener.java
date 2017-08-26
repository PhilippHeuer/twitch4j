package me.philippheuer.twitch4j.message.irc.listeners;

import me.philippheuer.twitch4j.message.irc.events.*;
import me.philippheuer.twitch4j.model.User;

import java.util.List;
import java.util.Map;

public class DefaultListener implements ITMIListener {
	/**
	 * Received action message on channel.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onAction(ChatEvent event) {

	}

	/**
	 * Username has been banned on a channel.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onBan(ChannelEvent event) {

	}

	/**
	 * Received message on channel.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onChat(ChatEvent event) {

	}

	/**
	 * Username has cheered to a channel.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onCheer(CheerEvent event) {

	}

	/**
	 * Chat of a channel got cleared.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onClearchat(ChannelEvent event) {

	}

	/**
	 * Connected to server.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onConnected(ConnectionEvent event) {

	}

	/**
	 * Connecting to a server.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onConnecting(ConnectionEvent event) {

	}

	/**
	 * Got disconnected from server.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onDisconnected(ConnectionEvent event) {

	}

	/**
	 * Channel enabled or disabled emote-only mode.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onEmoteonly(ChannelEvent event) {

	}

	/**
	 * Received the emote-sets from Twitch.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onEmotesets(Map<String, Map.Entry<Integer, Integer>> event) {

	}

	/**
	 * Channel enabled or disabled followers-only mode.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onFollowersonly(ChannelEvent event) {

	}

	/**
	 * Channel is now hosted by another broadcaster.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onHosted(HostEvent event) {

	}

	/**
	 * Channel is now hosting another channel.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onHosting(HostEvent event) {

	}

	/**
	 * Username has joined a channel.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onJoin(ServerStatusEvent event) {

	}

	/**
	 * Connection established, sending informations to server.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onLogon(ConnectionEvent event) {

	}

	/**
	 * Received a message.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onMessage(ChatEvent event) {

	}

	/**
	 * Someone got modded on a channel.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onMod(ChannelEvent event) {

	}

	/**
	 * Received the list of moderators of a channel.
	 *
	 * @param userMods Event emmiter
	 */
	@Override
	public void onMods(List<User> userMods) {

	}

	/**
	 * Received a notice from server.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onNotice(ServerStatusEvent event) {

	}

	/**
	 * User has left a channel.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onPart(ServerStatusEvent event) {

	}

	/**
	 * Received PING from server.
	 */
	@Override
	public void onPing() {

	}

	/**
	 * Sent a PING request ? PONG.
	 *
	 * @param latency Current latency
	 */
	@Override
	public void onPong(float latency) {

	}

	/**
	 * Channel enabled or disabled R9K mode.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onR9kmode(ChannelEvent event) {

	}

	/**
	 * Trying to reconnect to server.
	 *
	 * @param event
	 */
	@Override
	public void onReconnect(ConnectionEvent event) {

	}

	/**
	 * Username has resubbed on a channel.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onResub(SubscribeEvent event) {

	}

	/**
	 * The current state of the channel.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onRoomstate(ChannelEvent event) {

	}

	/**
	 * Channel is no longer located on this cluster.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onServerchange(ServerStatusEvent event) {

	}

	/**
	 * Gives you the current state of the channel.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onSlowmode(ChannelEvent event) {

	}

	/**
	 * Channel enabled or disabled subscribers-only mode.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onSubscribers(ChannelEvent event) {

	}

	/**
	 * Username has subscribed to a channel.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onSubscription(SubscribeEvent event) {

	}

	/**
	 * Username has been timed out on a channel.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onTimeout(ChannelEvent event) {

	}

	/**
	 * Channel ended the current hosting.
	 *
	 * @param event
	 */
	@Override
	public void onUnhost(HostEvent event) {

	}

	/**
	 * Someone got unmodded on a channel.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onUnmod(ChannelEvent event) {

	}

	/**
	 * Received a whisper.
	 *
	 * @param event Event emmiter
	 */
	@Override
	public void onWhisper(ChatEvent event) {

	}
}
