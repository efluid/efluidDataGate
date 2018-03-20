package fr.uem.efluid.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import fr.uem.efluid.utils.SharedOutputInputUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public final class Api {

	public static final String API_ROOT = "/rest";

	public static final HttpHeaders FIXED_JSON_HEADERS = new HttpHeaders();

	public static final HttpHeaders FIXED_UPLOAD_HEADERS = new HttpHeaders();

	private static final String ACCEPT_HEADER = "Accept";

	static {
		FIXED_JSON_HEADERS.add(ACCEPT_HEADER, MediaType.APPLICATION_JSON.toString());
		FIXED_UPLOAD_HEADERS.setContentType(MediaType.MULTIPART_FORM_DATA);
	}

	/**
	 * <p>
	 * Init a rest template for integration with shared API : define common properties for
	 * it
	 * </p>
	 * 
	 * @param restTemplate
	 */
	public static void configureMessageConverters(RestTemplate restTemplate) {

		List<HttpMessageConverter<?>> converters = new ArrayList<>();

		converters.add(0, new ResourceHttpMessageConverter());
		converters.add(new ByteArrayHttpMessageConverter());
		converters.add(new FormHttpMessageConverter());
		converters.add(new StringHttpMessageConverter());

		// Fixed supported types : json (with own modules)
		converters.add(new MappingJackson2HttpMessageConverter(SharedOutputInputUtils.preparedObjectMapper()));

		restTemplate.setMessageConverters(converters);
	}
}
