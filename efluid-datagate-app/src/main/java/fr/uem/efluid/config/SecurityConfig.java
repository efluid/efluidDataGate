package fr.uem.efluid.config;

import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.UserRepository;
import fr.uem.efluid.rest.RestApi;
import fr.uem.efluid.security.AllAuthorizer;
import fr.uem.efluid.security.AppUserCredentialAuthenticator;
import fr.uem.efluid.security.RestTokenAuthenticator;
import fr.uem.efluid.security.UserHolder;
import fr.uem.efluid.security.providers.AccountProvider;
import fr.uem.efluid.security.providers.DatabaseOnlyAccountProvider;
import fr.uem.efluid.security.providers.LdapAuthAccountProvider;
import fr.uem.efluid.services.ProjectManagementService;
import fr.uem.efluid.services.types.ProjectData;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.credentials.password.PasswordEncoder;
import org.pac4j.http.client.direct.ParameterClient;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.jee.filter.CallbackFilter;
import org.pac4j.jee.filter.LogoutFilter;
import org.pac4j.jee.filter.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Autowired
    private UserRepository users;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private ProjectManagementService projectService;

    @Value("${datagate-efluid.security.technical-user-token}")
    private String tokenTechnic;

    @PostConstruct
    public void checkUserTechnicExists() {

        if (this.users.findByToken(tokenTechnic).isEmpty()) { //check if user exists

            List<UUID> projects = this.projectService.getAllProjects().stream().map(ProjectData::getUuid).collect(Collectors.toList());

            User user = new User();
            user.setLogin(UserHolder.TECHNICAL_USER);
            user.setPassword(this.encoder.encode(UserHolder.TECHNICAL_USER));
            user.setEmail(UserHolder.TECHNICAL_USER_EMAIL);
            user.setToken(this.tokenTechnic); //set Token stored in application property
            user.setCreatedTime(LocalDateTime.now());
            user = this.users.save(user);

            this.projectService.setPreferedProjectsForUser(user, projects); //add manually all projects as prefered projects

            LOGGER.info("[SECURITY] created technical user: {}", user.getLogin());
        }
    }

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
    @ConditionalOnMissingBean
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
        DATABASE, LDAP_AUTH, LDAP_ALL
    }
}
