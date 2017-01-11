package de.philippheuer.twitch4j;

import de.philippheuer.twitch4j.chat.IrcClient;
import de.philippheuer.twitch4j.endpoints.*;
import de.philippheuer.twitch4j.pubsub.TwitchPubSub;
import lombok.*;

@Getter
@Setter
public class TwitchClient {
	
	/**
	 * Twitch API Endpoint
	 */
	public final String twitchEndpoint = "https://api.twitch.tv/kraken";
	
	/**
	 * Twitch API Version
	 */
	public final int twitchEndpointVersion = 5;
	
	/**
	 * Twitch PubSub Endpoint
	 */
	public final String twitchPubSubEndpoint = "wss://pubsub-edge.twitch.tv";
	
	/**
	 * Twitch IRC Endpoint
	 */
	public final String twitchIrcEndpoint = "irc.chat.twitch.tv:443";
	
	/**
	 * IRC Client
	 */
	private IrcClient ircClient;
	
    /**
     * Twitch Client Id
     */
    private String clientId;
    
    /**
     * Twitch Client Id
     */
    private String clientSecret;
    
    /**
     * PubSub Service
     */
    private TwitchPubSub pubSub;
    
    /**
     * Constructs a Twitch application instance.
     */
    public TwitchClient(String clientId, String clientSecret) {
        super();
        
        setClientId(clientId);
        setClientSecret(clientSecret);
        
        // Connect to IRC
        setIrcClient(new IrcClient(this));
        
        // Init PubSub API
        setPubSub(new TwitchPubSub(this));
    }
    
    /**
     * Get User Endpoint
     */
    public UserEndpoint getUserEndpoint() {
    	return new UserEndpoint(this);
    }
    
    /**
     * Get Channel Endpoint
     */
    public ChannelEndpoint getChannelEndpoint(Long channelId) {
    	return new ChannelEndpoint(this, channelId);
    }
    
    /**
     * Get Game Endpoint
     */
    public GameEndpoint getGameEndpoint() {
    	return new GameEndpoint(this);
    }
}
