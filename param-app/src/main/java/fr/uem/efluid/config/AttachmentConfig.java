package fr.uem.efluid.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import fr.uem.efluid.config.AttachmentConfig.AttachmentConfigProperties;
import fr.uem.efluid.model.entities.AttachmentType;
import fr.uem.efluid.model.repositories.ApplyHistoryEntryRepository;
import fr.uem.efluid.tools.AttachmentProcessor;
import fr.uem.efluid.tools.MarkdownAttachmentProcessor;
import fr.uem.efluid.tools.SqlAttachmentProcessor;
import fr.uem.efluid.tools.TextAttachmentProcessor;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@Configuration
@EnableConfigurationProperties(AttachmentConfigProperties.class)
public class AttachmentConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentConfig.class);

	@Autowired
	private AttachmentConfigProperties properties;

	@Autowired
	private JdbcTemplate managedSource;

	@Autowired
	private ApplyHistoryEntryRepository history;

	@Bean
	public AttachmentProcessor.Provider attchProcProvider() {

		AttachmentProcessor.Provider provider = new AttachmentProcessor.Provider(this.properties.isEnableDisplay());

		// Init display processors only if needed
		if (provider.isDisplaySupport()) {
			LOGGER.debug("[ATTACHMENTS] Add display support for TXT and MD attachments");
			provider.addProcessor(new TextAttachmentProcessor(), AttachmentType.TEXT_FILE);
			provider.addProcessor(new MarkdownAttachmentProcessor(), AttachmentType.MD_FILE);
		}

		// Init SQL Processor only if needed
		if (this.properties.isEnableSqlExecute()) {
			LOGGER.info("[ATTACHMENTS] Add execute support for SQL attachments");
			provider.addProcessor(new SqlAttachmentProcessor(this.managedSource, this.history), AttachmentType.SQL_FILE);
		}

		return provider;
	}

	/**
	 * @author elecomte
	 * @since v0.0.8
	 * @version 1
	 */
	@ConfigurationProperties(prefix = "param-efluid.attachments")
	public static class AttachmentConfigProperties {

		private boolean enableSqlExecute;
		private boolean enableDisplay;

		/**
		 * 
		 */
		public AttachmentConfigProperties() {
			super();
			// TODO Auto-generated constructor stub
		}

		/**
		 * @return the enableSqlExecute
		 */
		public boolean isEnableSqlExecute() {
			return this.enableSqlExecute;
		}

		/**
		 * @param enableSqlExecute
		 *            the enableSqlExecute to set
		 */
		public void setEnableSqlExecute(boolean enableSqlExecute) {
			this.enableSqlExecute = enableSqlExecute;
		}

		/**
		 * @return the enableDisplay
		 */
		public boolean isEnableDisplay() {
			return this.enableDisplay;
		}

		/**
		 * @param enableDisplay
		 *            the enableDisplay to set
		 */
		public void setEnableDisplay(boolean enableDisplay) {
			this.enableDisplay = enableDisplay;
		}

	}
}
