package fr.uem.efluid.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.uem.efluid.model.repositories.FeatureManager;
import fr.uem.efluid.model.repositories.impls.PreSpecifiedFeatureManager;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@Configuration
public class FeatureConfig {

	/**
	 * @return
	 */
	@Bean
	public FeatureManager preSpecifiedFeatureManager() {
		return new PreSpecifiedFeatureManager();
	}
}
