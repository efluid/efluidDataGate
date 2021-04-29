package fr.uem.efluid.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uem.efluid.Application;
import fr.uem.efluid.rest.RestApi;
import fr.uem.efluid.utils.SharedOutputInputUtils;
import org.h2.server.web.WebServer;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@Configuration
@ControllerAdvice
@EnableSwagger2
@EnableConfigurationProperties(WebFeaturesConfig.WebProperties.class)
public class WebFeaturesConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebFeaturesConfig.class);

    @Autowired
    private WebProperties properties;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAutoGrowCollectionLimit(500000);
    }

    @Bean
    ObjectMapper defaultMapper(){
        return SharedOutputInputUtils.preparedObjectMapper(false);
    }

    @Bean
    WebMvcConfigurer configurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry
                        .addResourceHandler("/webjars/**")
                        .addResourceLocations("/webjars/");
                registry
                        .addResourceHandler("swagger-ui.html")
                        .addResourceLocations("classpath:/META-INF/resources/");
            }


            @Override
            public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
                resolvers.size();
            }
        };
    }

    @Bean
    public Docket swaggerApi() {

        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage(Application.Packages.REST))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    /**
     * @return swagger api desc
     */
    public ApiInfo apiInfo() {
        return new ApiInfo(
                RestApi.API_TITLE,
                RestApi.API_DESCRIOTION,
                RestApi.ACTIVE_API_VERSION,
                null,
                null,
                RestApi.LICENSE_NAME,
                null,
                new ArrayList<>());
    }


    /**
     * H2 embedded database console for testing purpose
     */
    @ConditionalOnClass(Server.class)
    @ConditionalOnProperty(name = "datagate-efluid.web-options.enable-custom-h2-console", havingValue = "true")
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2CustomWebServer() throws SQLException {

        LOGGER.info("H2 custom console is enabled on tcp port {}", this.properties.getH2ConsolePort());

        return new Server(new WebServer(), "-web", "-webAllowOthers", "-webPort",
                String.valueOf(this.properties.getH2ConsolePort()));
    }

    @ConfigurationProperties(prefix = "datagate-efluid.web-options")
    public static class WebProperties {

        private boolean enableCustomH2Console;

        private int h2ConsolePort;

        public WebProperties() {
            super();
        }

        public boolean isEnableCustomH2Console() {
            return this.enableCustomH2Console;
        }

        public void setEnableCustomH2Console(boolean enableCustomH2Console) {
            this.enableCustomH2Console = enableCustomH2Console;
        }

        public int getH2ConsolePort() {
            return this.h2ConsolePort;
        }

        public void setH2ConsolePort(int h2ConsolePort) {
            this.h2ConsolePort = h2ConsolePort;
        }
    }
}
