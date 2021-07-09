package fr.uem.efluid;

import fr.uem.efluid.model.metas.ManagedModelDescription;
import fr.uem.efluid.tools.versions.ManagedModelIdentifier;
import fr.uem.efluid.utils.DatasourceUtils.CustomDataSourceParameters;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * The identifier implementation for <b>Efluid</b>
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
public class FixedModelIdentifier extends ManagedModelIdentifier {

    static final String VERSION = "Vtest-model";

    /**
     *
     */
    public FixedModelIdentifier(CustomDataSourceParameters parameters) {
        super(parameters);
    }

    /**
     * @return
     * @see ManagedModelIdentifier#getAllModelDescriptionQuery()
     */
    @Override
    public String getAllModelDescriptionQuery() {
        return "SELECT 1";
    }

    /**
     * @param lineResultSet
     * @param index
     * @return
     * @throws SQLException
     * @see ManagedModelIdentifier#extractFromLine(ResultSet,
     * int)
     */
    @Override
    public ManagedModelDescription extractFromLine(ResultSet lineResultSet, int index) throws SQLException {
        return new ManagedModelDescription(VERSION, LocalDateTime.now(), "Fixed version", "FIXED");
    }

    /**
     * @param identity
     * @param existingDescriptions
     * @return
     * @see ManagedModelIdentifier#isValidPastModelIdentifier(String,
     * List)
     */
    @Override
    public boolean isValidPastModelIdentifier(String identity, List<ManagedModelDescription> existingDescriptions) {
        return true;
    }
}
