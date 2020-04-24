package fr.uem.efluid.config;

import fr.uem.efluid.rest.RestApi;
import fr.uem.efluid.security.AllAuthorizer;
import fr.uem.efluid.security.AppUserCredentialAuthenticator;
import fr.uem.efluid.security.RestTokenAuthenticator;
import fr.uem.efluid.security.providers.AccountProvider;
import fr.uem.efluid.security.providers.DatabaseOnlyAccountProvider;
import fr.uem.efluid.security.providers.LdapAuthAccountProvider;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.http.client.direct.ParameterClient;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.j2e.filter.CallbackFilter;
import org.pac4j.j2e.filter.LogoutFilter;
import org.pac4j.j2e.filter.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@Configuration
@EnableConfigurationProperties(SecurityConfig.SecurityProperties.class)
public class SecurityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    private static final String WEB_CLIENT = "web";
    private static final String REST_CLIENT = "rest";
    private static final String AUTHORIZER = "user";

    @Autowired
    private SecurityConfig.SecurityProperties properties;

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
    public FilterRegistrationBean<SecurityFilter> webSecurityFilter() {

        LOGGER.debug("[SECURITY] Mapping rest security on \"/ui\", \"/ui/*\"");

        FilterRegistrationBean<SecurityFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SecurityFilter(config(), WEB_CLIENT, AUTHORIZER));
        registration.addUrlPatterns("/ui", "/ui/*");
        registration.setName("pac4jWestSecurityFilter");
        registration.setOrder(1);

        return registration;
    }

    @Bean
    public FilterRegistrationBean<SecurityFilter> restSecurityFilter() {

        LOGGER.debug("[SECURITY] Mapping rest security on \"/rest/v1/backlog/*\", \"/rest/v1/dictionary/*\", \"/rest/v1/features/*\", \"/rest/v1/projects/*\"");

        FilterRegistrationBean<SecurityFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SecurityFilter(config(), REST_CLIENT, AUTHORIZER));
        registration.addUrlPatterns("/rest/v1/backlog/*", "/rest/v1/dictionary/*", "/rest/v1/features/*", "/rest/v1/projects/*");
        registration.setName("pac4jRestSecurityFilter");
        registration.setOrder(1);

        return registration;
    }

    @Bean
    public FilterRegistrationBean<CallbackFilter> callbackFilter() {

        LOGGER.debug("[SECURITY] Mapping callback on \"/callback\"");

        FilterRegistrationBean<CallbackFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CallbackFilter(config(), "/"));
        registration.addUrlPatterns("/callback");
        registration.setName("pac4jCallbackFilter");
        registration.setOrder(1);

        return registration;
    }

    @Bean
    public FilterRegistrationBean<LogoutFilter> logoutFilter() {

        LOGGER.debug("[SECURITY] Mapping logout on \"/logout\"");

        FilterRegistrationBean<LogoutFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new LogoutFilter(config(), "/"));
        registration.addUrlPatterns("/logout");
        registration.setName("pac4jLogoutFilter");
        registration.setOrder(1);

        return registration;
    }

    @Bean
    public AccountProvider accountProvider() {
        switch (this.properties.accounting) {
            case LDAP_ALL:
                throw new ApplicationException(ErrorType.INIT_FAILED, "LDAP_ALL is not supported yet for accounting");
            case LDAP_AUTH:
                return new LdapAuthAccountProvider(
                        this.properties.getLdap().getUserSearchBase(),
                        this.properties.getLdap().getUsernameAttribute(),
                        this.properties.getLdap().getMailAttribute());
            default:
                return new DatabaseOnlyAccountProvider();
        }
    }

    @ConfigurationProperties(prefix = "datagate-efluid.security")
    public static class SecurityProperties {

        private String salt;

        private AccountingType accounting;

        private LdapProperties ldap;

        public SecurityProperties() {
        }

        public String getSalt() {
            return salt;
        }

        public void setSalt(String salt) {
            this.salt = salt;
        }

        public AccountingType getAccounting() {
            return accounting;
        }

        public void setAccounting(AccountingType accounting) {
            this.accounting = accounting;
        }

        public LdapProperties getLdap() {
            return ldap;
        }

        public void setLdap(LdapProperties ldap) {
            this.ldap = ldap;
        }

        public static class LdapProperties {

            private String userSearchBase;
            private String usernameAttribute;
            private String mailAttribute;
            private boolean useAuthBinding;

            public LdapProperties() {
            }

            public String getUserSearchBase() {
                return userSearchBase;
            }

            public void setUserSearchBase(String searchUserBase) {
                this.userSearchBase = searchUserBase;
            }

            public String getUsernameAttribute() {
                return usernameAttribute;
            }

            public void setUsernameAttribute(String usernameAttribute) {
                this.usernameAttribute = usernameAttribute;
            }

            public String getMailAttribute() {
                return mailAttribute;
            }

            public void setMailAttribute(String mailAttribute) {
                this.mailAttribute = mailAttribute;
            }

            public boolean isUseAuthBinding() {
                return useAuthBinding;
            }

            public void setUseAuthBinding(boolean useAuthBinding) {
                this.useAuthBinding = useAuthBinding;
            }
        }
    }

    /**
     * For specification of active accounting
     */
    public enum AccountingType {
        DATABASE, LDAP_AUTH, LDAP_ALL;
    }
}
