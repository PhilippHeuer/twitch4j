package me.philippheuer.twitch4j.endpoints;

import com.jcabi.log.Logger;
import lombok.Getter;
import lombok.Setter;
import me.philippheuer.twitch4j.TwitchClient;
import me.philippheuer.twitch4j.auth.model.OAuthCredential;
import me.philippheuer.twitch4j.exceptions.RestException;
import me.philippheuer.twitch4j.model.*;
import org.springframework.web.client.RestTemplate;

@Getter
@Setter
public class KrakenEndpoint extends AbstractTwitchEndpoint {

	/**
	 * Constructor - by ChannelId
	 *
	 * @param client todo
	 */
	public KrakenEndpoint(TwitchClient client) {
		super(client);
	}

	/**
	 * Endpoint: Get OAuth Token Information
	 * Gets information about the provided oAuthToken
	 * Requires Scope: none
	 *
	 * @return todo
	 */
	public Token getToken(OAuthCredential credential) {
		// Endpoint
		String requestUrl = String.format("%s", getTwitchClient().getTwitchEndpoint());
		RestTemplate restTemplate = getTwitchClient().getRestClient().getPrivilegedRestTemplate(credential);

		// REST Request
		try {
			TokenResponse responseObject = restTemplate.getForObject(requestUrl, TokenResponse.class);

			return responseObject.getToken();
		} catch (RestException restException) {
			Logger.error(this, "RestException: " + restException.getRestError().toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// Default Response: Invalid Token
		return new Token();
	}

}
