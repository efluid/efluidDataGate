package fr.uem.efluid.config;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.uem.efluid.model.metas.ManagedModelDescription;
import fr.uem.efluid.tools.ManagedModelIdentifier;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;

/**
 * <p>
 * Model identifier allows to check the managed database version from a table or whatever,
 * and manage it as a <tt>Version</tt> endorsement
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@Configuration
public class ManagedModelIdentifierConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(ManagedModelIdentifierConfig.class);

	@Value("${param-efluid.model-identifier.class-name}")
	private String identifierTypeName;

	@Value("${param-efluid.model-identifier.enabled}")
	private boolean checkEnabled;

	@Bean
	@SuppressWarnings("unchecked")
	public ManagedModelIdentifier modelIdentifier() throws ApplicationException {

		// Disabled
		if (!this.checkEnabled) {

			LOGGER.debug("[MODEL-ID] Disabled model identifier check. Load empty one");

			return new ManagedModelIdentifier() {

				@Override
				public String getAllModelDescriptionQuery() {
					return null;
				}

				@Override
				public ManagedModelDescription extractFromLine(ResultSet lineResultSet, int index) throws SQLException {
					return null;
				}

				@Override
				public boolean isValidPastModelIdentifier(String identity, List<ManagedModelDescription> existingDescriptions) {
					return true;
				}

			};
		}

		LOGGER.debug("[MODEL-ID] Enabled identifier with type {}", this.identifierTypeName);

		try {
			Class<ManagedModelIdentifier> identifierType = (Class<ManagedModelIdentifier>) Class.forName(this.identifierTypeName);
			ManagedModelIdentifier identifier = identifierType.getConstructor().newInstance();

			LOGGER.info("[MODEL-ID] Model Identifier enabled with type {}", this.identifierTypeName);

			return identifier;

		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new ApplicationException(ErrorType.INIT_FAILED, "Cannot init MODEL_ID with type " + this.identifierTypeName, e);
		}

	}
}
