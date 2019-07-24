package fr.uem.efluid.model.repositories.impls;

import fr.uem.efluid.model.metas.ManagedModelDescription;
import fr.uem.efluid.model.repositories.ManagedModelDescriptionRepository;
import fr.uem.efluid.tools.ManagedModelIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Uses a specified <tt>ManagedModelIdentifier</tt> found in context
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@Repository
public class JdbcBasedManagedModelDescriptionRepository implements ManagedModelDescriptionRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcBasedManagedModelDescriptionRepository.class);
    private static final Logger QUERRY_LOGGER = LoggerFactory.getLogger("identifier.queries");

    @Autowired
    private JdbcTemplate managedSource;

    @Autowired
    private ManagedModelIdentifier identifier;

    @Value("${datagate-efluid.model-identifier.enabled}")
    private boolean checkEnabled;

    @Value("${datagate-efluid.model-identifier.show-sql}")
    private boolean showSql;

    /**
     * @return
     * @see fr.uem.efluid.model.repositories.ManagedModelDescriptionRepository#getModelDescriptions()
     */
    @Override
    public List<ManagedModelDescription> getModelDescriptions() {

        String query = this.identifier.getAllModelDescriptionQuery();
        postProcessQuery(query);
        LOGGER.debug("Extracting Managed Model descriptions using query \"{}\"", query);

        // Get columns for all table
        List<ManagedModelDescription> descriptions = this.managedSource.query(query, new DescriptionExtractor(this.identifier));

        if (descriptions.isEmpty()) {
            LOGGER.warn("Cannot found any managed model description with specified identifier. Check if the"
                    + " access query \"{}\" is valid. Will continue with \"not found\" model versionning", query);
        } else if (LOGGER.isDebugEnabled()) {

            ManagedModelDescription last = descriptions.get(descriptions.size() - 1);
            LOGGER.debug("Extracted {} descriptions from managed model. Last one is \"{}\".",
                    Integer.valueOf(descriptions.size()), last.getIdentity());
        }

        return descriptions;
    }

    /**
     * @return
     * @see fr.uem.efluid.model.repositories.ManagedModelDescriptionRepository#hasToCheckDescriptions()
     */
    @Override
    public boolean hasToCheckDescriptions() {
        return this.checkEnabled;
    }

    /**
     * @return
     * @see fr.uem.efluid.model.repositories.ManagedModelDescriptionRepository#getCurrentModelIdentifier()
     */
    @Override
    public String getCurrentModelIdentifier() {

        if (!hasToCheckDescriptions()) {
            return null;
        }

        List<ManagedModelDescription> descs = getModelDescriptions();

        if (descs.size() == 0) {
            return null;
        }

        // Basic access. Always use last
        return descs.get(descs.size() - 1).getIdentity();
    }

    /**
     * @param modelIdentifier
     * @return
     */
    @Override
    public IdentifierType getModelIdentifierType(String modelIdentifier) {

        if (this.checkEnabled) {

            // Current descs
            List<ManagedModelDescription> descs = getModelDescriptions();

            if (!descs.isEmpty()) {

                if (descs.get(descs.size() - 1).getIdentity().equals(modelIdentifier)) {
                    return IdentifierType.CURRENT;
                }

                if (this.identifier.isValidPastModelIdentifier(modelIdentifier, descs)) {
                    return IdentifierType.OLD_ONE;
                }
            }
        }

        // Default unprocessed - unknown
        return IdentifierType.UNKNOWN;
    }

    /**
     * @param query
     */
    private void postProcessQuery(String query) {

        // Can output query (using a similar logger than the one from Hibernate on
        // show-sql configuration parameter)
        if (this.showSql) {
            QUERRY_LOGGER.info(query);
        }
    }

    /**
     * <p>
     * Processing component for description query result. Process the result set and call
     * for available <tt>ManagedModelIdentifier</tt> to extract version one by one
     * </p>
     *
     * @author elecomte
     * @version 1
     * @since v0.0.8
     */
    private static class DescriptionExtractor implements ResultSetExtractor<List<ManagedModelDescription>> {

        private final ManagedModelIdentifier identifier;

        /**
         * @param identifier
         */
        public DescriptionExtractor(ManagedModelIdentifier identifier) {
            super();
            this.identifier = identifier;
        }

        /**
         * @param rs
         * @return
         * @throws SQLException
         * @throws DataAccessException
         * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql.ResultSet)
         */
        @Override
        public List<ManagedModelDescription> extractData(ResultSet rs) throws SQLException, DataAccessException {

            List<ManagedModelDescription> descriptions = new ArrayList<>();

            int line = 0;
            while (rs.next()) {
                // Basic extract, line after line
                descriptions.add(this.identifier.extractFromLine(rs, line));
                line++;
            }

            return descriptions;
        }
    }
}
