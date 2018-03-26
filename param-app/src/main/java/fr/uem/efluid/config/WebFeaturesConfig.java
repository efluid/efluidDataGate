package fr.uem.efluid.config;

import java.util.ArrayList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import fr.uem.efluid.Application;
import fr.uem.efluid.rest.RestApi;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Configuration
@ControllerAdvice
@EnableSwagger2
public class WebFeaturesConfig {

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.setAutoGrowCollectionLimit(500000);
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
		};
	}

	/**
	 * @return
	 */
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
}
