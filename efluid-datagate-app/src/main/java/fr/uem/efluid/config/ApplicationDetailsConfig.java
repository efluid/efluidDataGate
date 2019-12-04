package fr.uem.efluid.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.uem.efluid.config.ApplicationDetailsConfig.DetailProperties;
import fr.uem.efluid.services.types.ApplicationInfo;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@Configuration
@EnableConfigurationProperties(DetailProperties.class)
public class ApplicationDetailsConfig {

	@Autowired
	private DetailProperties props;

	@Bean
	public ApplicationInfo preparedInfo() {
		return new ApplicationInfo(this.props.getVersion(), this.props.getInstanceName());
	}

	/**
	 * @author elecomte
	 * @since v0.0.8
	 * @version 1
	 */
	@ConfigurationProperties(prefix = "datagate-efluid.details")
	public static class DetailProperties {

		private String version;
		private String instanceName;

		public DetailProperties() {
			super();
		}

		public String getVersion() {
			return this.version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public String getInstanceName() {
			return this.instanceName;
		}

		public void setInstanceName(String instanceName) {
			this.instanceName = instanceName;
		}
	}
}
