package me.philippheuer.twitch4j.message.irc;

import com.jcabi.log.Logger;
import me.philippheuer.twitch4j.TwitchClient;
import me.philippheuer.twitch4j.enums.SubPlan;
import me.philippheuer.twitch4j.message.irc.events.*;
import me.philippheuer.twitch4j.message.irc.listeners.ITMIListener;

import java.util.*;

public class IRCDispatcher <T extends ITMIListener> {
	private final TwitchClient twitchClient;
	private final List<T> listeners = new LinkedList<T>();

	IRCDispatcher(TwitchClient client) {
		twitchClient = client;
	}

	void dispatch(IRCParser parser, Object... data) {
		Logger.debug(this, parser.toString());
		String botname = twitchClient.getCredentialManager().getTwitchCredentialsForIRC().get().getUserName();
		listeners.forEach(listener -> {
			List<String> userlist = new ArrayList<String>(); // onNames(List<String> userlist)
			switch (parser.getCommand().toUpperCase()) {
				case "001":
				case "002":
				case "003":
				case "004":
				case "372":
				case "375":
				case "376":
				case "CAP":
				// Wrong cluster..
				case "SERVERCHANGE":
					break;
				// https://dev.twitch.tv/docs/v5/guides/irc/#msg-id-tags-for-the-notice-commands-capability
				case "NOTICE":
					listenNotice(parser, listener);
					break;
				// Handle subanniversary / resub..
				case "USERNOTICE":
					listenSub(parser, listener);
					break;
				// Channel is now hosting another channel or exited host mode..
				case "HOSTTARGET":
					listenHost(parser, listener);
					break;
				// Someone has been timed out or chat has been cleared by a moderator..
				case "CLEARCHAT":
					listenClear(parser, listener);
					break;
				// Received a reconnection request from the server..
				case "RECONNECT":
					Logger.debug(this, "Requesting for reconnect by server.");
					twitchClient.reconnect();
					break;
				// Received when joining a channel and every time you send a PRIVMSG to a channel.
				case "USERSTATE":
				// Describe non-channel-specific state informations. (On successful login.)
				case "GLOBALUSERSTATE":
				// Received when joining a channel and every time one of the chat room settings, like slow mode, change.
				// The message on join contains all room settings.
				case "ROOMSTATE":
				// Messages from jtv. (Moderators)
				case "MODE":
					break;
				// User chat list
				case "353":
					userlist.addAll(Arrays.asList(parser.getMessage().split(" ")));
					break;
				case "366":
					listener.onNames(userlist);
					break;
				// Someone has joined the channel.
				case "JOIN":
				// Someone has left the channel.
				case "PART":
				// Received a whisper.
				case "WHISPER":
				// Received channel message
				case "PRIVMSG":

				// Could not parse message
				default:
					// TODO: warn-debugged message without exception
					break;
			}
		});
	}

	private void listenClear(IRCParser parser, T listener) {

	}

	private void listenHost(IRCParser parser, T listener) {

	}

	private void listenSub(IRCParser parser, T listener) {
		String msgId = parser.getTags().getTag("msg-id").toString();
		switch (msgId) {
			case "resub":
				listener.onResub(new SubscribeEvent(parser));
				break;
			case "sub":
				listener.onSubscription(new SubscribeEvent(parser));
				break;
			default:
				break;
		}
	}

	private void listenNotice(IRCParser parser, T listener) {
		ChannelEvent event = new ChannelEvent(parser);
		switch (parser.getTags().getTag("msg-id").toString().toLowerCase()) {
			// This room is now in subscribers-only mode.
			case "subs_on":
				listener.onSubscribers(new ChannelState(parser.getChannelName(), false));
				break;
			// This room is no longer in subscribers-only mode.
			case "subs_off":
				listener.onSubscribers(new ChannelState(parser.getChannelName(), false));
				break;
			case "emote_only_on": // This room is now in emote-only mode.
				listener.onEmoteOnly(new ChannelState(parser.getChannelName(), true));
				break;
			case "emote_only_off": // This room is no longer in emote-only mode.
				listener.onEmoteOnly(new ChannelState(parser.getChannelName(), false));
				break;
			// This room is now in r9k mode.
			case "r9k_on":
				listener.onR9kMode(new ChannelState(parser.getChannelName(), true));
				break;
			// This room is no longer in r9k mode.
			case "r9k_off":
				listener.onR9kMode(new ChannelState(parser.getChannelName(), false));
				break;
			// The moderators of this room are [...]
			case "room_mods":
			// There are no moderators for this room.
			case "no_mods":
				listener.onMods(new ChannelMods(parser));
				break;
			// Channel is suspended..
			case "msg_channel_suspended":
			// Ban command failed..
			case "already_banned":
			case "bad_ban_admin":
			case "bad_ban_broadcaster":
			case "bad_ban_global_mod":
			case "bad_ban_self":
			case "bad_ban_staff":
			case "usage_ban":
			// Ban command success..
			case "ban_success":
			// Clear command failed..
			case "usage_clear":
			// Mods command failed..
			case "usage_mods":
			// Mod command success..
			case "mod_success":
			// Mod command failed..
			case "usage_mod":
			case "bad_mod_banned":
			case "bad_mod_mod":
			// Unmod command success..
			case "unmod_success":
			// Unmod command failed..
			case "usage_unmod":
			case "bad_unmod_mod":
			// Color command success..
			case "color_changed":
			// Color command failed..
			case "usage_color":
			case "turbo_only_color":
			// Commercial command success..
			case "commercial_success":
			// Commercial command failed..
			case "usage_commercial":
			case "bad_commercial_error":
			// Host command success..
			case "hosts_remaining":
			// Host command failed..
			case "bad_host_hosting":
			case "bad_host_rate_exceeded":
			case "bad_host_error":
			case "usage_host":
			// r9kbeta command failed..
			case "already_r9k_on":
			case "usage_r9k_on":
			// r9kbetaoff command failed..
			case "already_r9k_off":
			case "usage_r9k_off":
			// Timeout command success..
			case "timeout_success":
			// Subscribersoff command failed..
			case "already_subs_off":
			case "usage_subs_off":
			// Subscribers command failed..
			case "already_subs_on":
			case "usage_subs_on":
			// Emoteonlyoff command failed..
			case "already_emote_only_off":
			case "usage_emote_only_off":
			// Emoteonly command failed..
			case "already_emote_only_on":
			case "usage_emote_only_on":
			// Slow command failed.
			case "usage_slow_on":
			// Slowoff command failed.
			case "usage_slow_off":
			// Timeout command failed..
			case "usage_timeout":
			case "bad_timeout_admin":
			case "bad_timeout_broadcaster":
			case "bad_timeout_duration":
			case "bad_timeout_global_mod":
			case "bad_timeout_self":
			case "bad_timeout_staff":
			// Unban command success..
			// Unban can also be used to cancel an active timeout.
			case "untimeout_success":
			case "unban_success":
			// Unban command failed..
			case "usage_unban":
			case "bad_unban_no_ban":
			// Unhost command failed..
			case "usage_unhost":
			case "not_hosting":
			// Whisper command failed..
			case "whisper_invalid_login":
			case "whisper_invalid_self":
			case "whisper_limit_per_min":
			case "whisper_limit_per_sec":
			case "whisper_restricted_recipient":
			// Permission error..
			case "no_permission":
			case "msg_banned":
			// Send the following msg-ids to the notice event listener..
			case "cmds_available":
			case "host_target_went_offline":
			case "msg_censored_broadcaster":
			case "msg_duplicate":
			case "msg_emoteonly":
			case "msg_verified_email":
			case "msg_ratelimit":
			case "msg_subsonly":
			case "msg_timedout":
			case "no_help":
			case "usage_disconnect":
			case "usage_help":
			case "usage_me":
				listener.onNotice(event);
				break;
			// Unrecognized command..
			case "unrecognized_cmd":
				// TODO: Warn whisper
				listener.onNotice(event);
//				if (msg.split(" ").splice(-1)[0] === "/w") {
//					this.log.warn("You must be connected to a group server to send or receive whispers.");
//				}
				break;

			// IGNORES

			// Ignore this because we are already listening to HOSTTARGET.
			case "host_on":
			case "host_off":
				break;
			// Do not handle slow_on/off here, listen to the ROOMSTATE notice instead as it returns the delay.
			case "slow_on":
			case "slow_off":
			// Do not handle followers_on/off here, listen to the ROOMSTATE notice instead as it returns the delay.
			case "followers_on_zero":
			case "followers_on":
			case "followers_off":
				break;
			default:
				if (parser.getMessage().contains("Login unsuccessful") || parser.getMessage().contains("Login authentication failed")) {
					// TODO: Connection exception
				}
				else if (parser.getMessage().contains("Error logging in") || parser.getMessage().contains("Improperly formatted auth")) {
					// TODO: Login Error exception
				}
				else if (parser.getMessage().contains("Invalid NICK")) {
					// TODO: Username is invalid exception
				}
				else {
					// TODO: Other exceptions
					// example:
					//throw new IRCException(parser);
				}
				break;
		}
	}

	public void addAll(T... listener) {
		listeners.addAll(Arrays.asList(listener));
	}

	public void addAll(Collection<T> listener) {
		listeners.addAll(listener);
	}

	public void addListener(T listener) {
		if (!listeners.contains(listener)) listeners.add(listener);
	}

	public void removeListener(T listener) {
		if (listeners.contains(listener)) listeners.remove(listener);
	}
}
