package fr.uem.efluid.config;

import fr.uem.efluid.config.ManagedModelIdentifierConfig.ModelIdenfierProperties;
import fr.uem.efluid.model.metas.ManagedModelDescription;
import fr.uem.efluid.tools.versions.ManagedModelIdentifier;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.DatasourceUtils;
import fr.uem.efluid.utils.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.List;

/**
 * <p>
 * Model identifier allows to check the managed database version from a table or whatever,
 * and manage it as a <tt>Version</tt> endorsement
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@Configuration
@EnableConfigurationProperties(ModelIdenfierProperties.class)
public class ManagedModelIdentifierConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedModelIdentifierConfig.class);

    @Autowired
    private ModelIdenfierProperties properties;

    @Autowired
    private DatasourceUtils.CustomDataSourceParameters dsProps;

    @Bean
    @SuppressWarnings("unchecked")
    public ManagedModelIdentifier modelIdentifier() throws ApplicationException {

        // Disabled
        if (!this.properties.isEnabled()) {

            LOGGER.debug("[MODEL-ID] Disabled model identifier check. Load empty one");

            return new ManagedModelIdentifier(null) {

                @Override
                public String getAllModelDescriptionQuery() {
                    return null;
                }

                @Override
                public ManagedModelDescription extractFromLine(ResultSet lineResultSet, int index) {
                    return null;
                }

                @Override
                public boolean isValidPastModelIdentifier(String identity, List<ManagedModelDescription> existingDescriptions) {
                    return true;
                }

            };
        }

        LOGGER.debug("[MODEL-ID] Enabled identifier with type {}", this.properties.getClassName());

        try {
            Class<ManagedModelIdentifier> identifierType = (Class<ManagedModelIdentifier>) Class.forName(this.properties.getClassName());
            ManagedModelIdentifier identifier = identifierType
                    .getConstructor(DatasourceUtils.CustomDataSourceParameters.class)
                    .newInstance(this.dsProps);

            LOGGER.info("[MODEL-ID] Model Identifier enabled with type {}", this.properties.getClassName());

            return identifier;

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new ApplicationException(ErrorType.INIT_FAILED, "Cannot init MODEL_ID with type " + this.properties.getClassName(), e);
        }

    }

    /**
     * <p>
     * Use config properties for extensible parameters
     * </p>
     *
     * @author elecomte
     * @version 1
     * @since v2.0.0
     */
    @ConfigurationProperties(prefix = "datagate-efluid.model-identifier")
    @SuppressWarnings("unused")
    public static class ModelIdenfierProperties {

        private boolean enabled;
        private String className;
        private boolean showSql;

        /**
         *
         */
        public ModelIdenfierProperties() {
            super();
        }

        /**
         * @return the enabled
         */
        public boolean isEnabled() {
            return this.enabled;
        }

        /**
         * @param enabled the enabled to set
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * @return the className
         */
        public String getClassName() {
            return this.className;
        }

        /**
         * @param className the className to set
         */
        public void setClassName(String className) {
            this.className = className;
        }

        /**
         * @return the showSql
         */
        public boolean isShowSql() {
            return this.showSql;
        }

        /**
         * @param showSql the showSql to set
         */
        public void setShowSql(boolean showSql) {
            this.showSql = showSql;
        }

    }
}
