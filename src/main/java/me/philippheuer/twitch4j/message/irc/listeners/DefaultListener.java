package me.philippheuer.twitch4j.message.irc.listeners;

import me.philippheuer.twitch4j.message.irc.events.*;

import java.util.List;
import java.util.Map;

public class DefaultListener implements ITMIListener {
	@Override
	public void onAction(ChatEvent event) {

	}

	@Override
	public void onBan(ChannelEvent event) {

	}

	@Override
	public void onChat(ChatEvent event) {

	}

	@Override
	public void onCheer(ChatEvent event) {

	}

	@Override
	public void onClearchat(ChannelEvent event) {

	}

	@Override
	public void onConnected(ConnectionEvent event) {

	}

	@Override
	public void onConnecting(ConnectionEvent event) {

	}

	@Override
	public void onDisconnected(ConnectionEvent event) {

	}

	@Override
	public void onEmoteOnly(ChannelState event) {

	}

	@Override
	public void onFollowersonly(RoomStateEvent.Status event) {

	}

	@Override
	public void onHosted(HostEvent event) {

	}

	@Override
	public void onHosting(HostEvent event) {

	}

	@Override
	public void onJoin(UserStatusEvent event) {

	}

	@Override
	public void onMessage(ChatEvent event) {

	}

	@Override
	public void onMod(ChannelMod event) {

	}

	@Override
	public void onMods(ChannelMods userMods) {

	}

	@Override
	public void onNotice(ChannelEvent event) {

	}

	@Override
	public void onPart(UserStatusEvent event) {

	}

	@Override
	public void onPing() {

	}

	@Override
	public void onPong(float latency) {

	}

	@Override
	public void onR9kMode(ChannelState event) {

	}

	@Override
	public void onReconnect(ConnectionEvent event) {

	}

	@Override
	public void onResub(SubscribeEvent event) {

	}

	@Override
	public void onRoomstate(RoomStateEvent event) {

	}

	@Override
	public void onServerchange(UserStatusEvent event) {

	}

	@Override
	public void onSlowmode(RoomStateEvent.Status event) {

	}

	@Override
	public void onSubscribers(ChannelState event) {

	}

	@Override
	public void onSubscription(SubscribeEvent event) {

	}

	@Override
	public void onTimeout(ChannelEvent event) {

	}

	@Override
	public void onUnhost(HostEvent event) {

	}

	@Override
	public void onUnmod(ChannelMod event) {

	}

	@Override
	public void onWhisper(ChatEvent event) {

	}

	@Override
	public void onNames(List<String> userlist) {

	}

	@Override
	public void onDisconnecting(ConnectionEvent connectionEvent) {

	}
}
