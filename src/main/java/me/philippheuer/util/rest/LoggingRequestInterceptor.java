package me.philippheuer.util.rest;

import java.io.IOException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Spring Rest: Logging Request Interceptor
 * <p>
 * The logging request interceptor can be applied to a {@link org.springframework.web.client.RestTemplate} and
 * will log the final request generated by all **previously** added interceptors.
 *
 * @author Philipp Heuer
 * @version %I%, %G%
 * @since 1.0
 */
@Slf4j
public class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

		ClientHttpResponse response = execution.execute(request, body);

		log(request, body);

		return response;
	}

	private void log(HttpRequest request, byte[] body) {
		// do logging
		log.info("Request: [{}] {}", request.getMethod(), request.getURI());
		log.info("Headers: {}", request.getHeaders());
		if (body.length > 0) {
			log.info("Body: {}", Arrays.toString(body));
		}
	}
}
