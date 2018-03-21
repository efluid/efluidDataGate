package fr.uem.efluid.config;

import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.http.client.direct.ParameterClient;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.MappedInterceptor;

import fr.uem.efluid.rest.RestApi;
import fr.uem.efluid.security.AllAuthorizer;
import fr.uem.efluid.security.AppUserCredentialAuthenticator;
import fr.uem.efluid.security.RestTokenAuthenticator;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Configuration
@ComponentScan(basePackages = "org.pac4j.springframework.web")
public class SecurityConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

	public static final String WEB_CLIENT = "web";
	public static final String REST_CLIENT = "rest";
	public static final String AUTHORIZER = "user";

	@Value("${param-efluid.security.salt}")
	private String salt;

	@Autowired
	private AllAuthorizer authorizer;

	@Autowired
	private AppUserCredentialAuthenticator webAuthent;

	@Autowired
	private RestTokenAuthenticator restAuthent;

	@Bean
	public Config config() {

		final FormClient formClient = new FormClient("/login", this.webAuthent);
		formClient.setName(WEB_CLIENT);

		final ParameterClient restClient = new ParameterClient(RestApi.TOKEN_PARAM, this.restAuthent);
		restClient.setName(REST_CLIENT);
		restClient.setSupportGetRequest(true);
		restClient.setSupportPostRequest(true);

		final Clients clients = new Clients("/callback", formClient, restClient);

		final Config config = new Config(clients);
		config.addAuthorizer(AUTHORIZER, this.authorizer);

		LOGGER.info("[SECURITY] pac4j based security config is ready. Will process web and rest authentication");

		return config;
	}

	@Bean
	public MappedInterceptor webInterceptor() {
		return new MappedInterceptor(new String[] { "/ui", "/ui/*" },
				new SecurityInterceptor(config(), WEB_CLIENT, AUTHORIZER));
	}

	@Bean
	public MappedInterceptor restInterceptor() {
		return new MappedInterceptor(new String[] { "/rest/v1/backlog/*", "/rest/v1/dictionary/*" },
				new SecurityInterceptor(config(), REST_CLIENT, AUTHORIZER));
	}
}
