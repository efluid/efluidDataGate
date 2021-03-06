package fr.uem.efluid.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.uem.efluid.config.ManagedDatasourceConfig.DataSourceProperties;
import fr.uem.efluid.model.repositories.DatabaseDescriptionRepository;
import fr.uem.efluid.model.repositories.impls.OracleDatabaseDescriptionRepository;
import fr.uem.efluid.model.repositories.impls.PureJdbcDatabaseDescriptionRepository;

/**
 * <p>
 * Init management and Force-load for the metadata for Managed database
 * </p>
 * <p>
 * Implements are depending from the database type
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@EnableConfigurationProperties(DataSourceProperties.class)
@Configuration
public class ManagedMetadataConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(ManagedMetadataConfig.class);

	private static final String ORACLE_DRIVER_START = "oracle";

	@Autowired
	private DataSourceProperties props;

	@Bean
	public DatabaseDescriptionRepository initDatabaseDescriptionRepository() {

		DatabaseDescriptionRepository extractor;

		if (this.props.getDriverClassName().toLowerCase().startsWith(ORACLE_DRIVER_START)) {
			LOGGER.info("Using an Oracle database : load dedicated extractor for Oracle metadata");
			extractor = new OracleDatabaseDescriptionRepository();
		} else {

			LOGGER.info("Load standard JDBC extractor for metadata");
			extractor = new PureJdbcDatabaseDescriptionRepository();
		}

		return extractor;
	}

	@Bean
	@ConditionalOnProperty(name = "datagate-efluid.managed-datasource.meta.preload", havingValue = "true")
	public Object postLoad(DatabaseDescriptionRepository metas) {

		LOGGER.info("Metadata preload asked. Process it on extractor");
		metas.getTables();
		return new Object();
	}
}
