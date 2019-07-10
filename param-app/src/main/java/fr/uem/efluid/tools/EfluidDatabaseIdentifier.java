package fr.uem.efluid.tools;

import fr.uem.efluid.model.metas.ManagedModelDescription;
import fr.uem.efluid.utils.DatasourceUtils;
import fr.uem.efluid.utils.FormatUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * The identifier implementation for <b>Efluid</b> based on table <code>TAPPLICATIONINFO</code>.
 * </p>
 * <p>
 * Even if this table is supposed to have only 1 line, we want to avoid any
 * error so this extraction is compliant with multiple specified versions
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.0.8
 */
public class EfluidDatabaseIdentifier extends ManagedModelIdentifier {

    public EfluidDatabaseIdentifier(DatasourceUtils.CustomDataSourceParameters parameters) {
        super(parameters);
    }

    /**
     * @return
     * @see fr.uem.efluid.tools.ManagedModelIdentifier#getAllModelDescriptionQuery()
     */
    @Override
    public String getAllModelDescriptionQuery() {
        return "SELECT \"VERSION\", \"DATEMODIFICATION\", concat(\"PROJET\",concat(' ', \"MESSAGEINFO\")) as \"DETAILS\" FROM \"TAPPLICATIONINFO\" ORDER BY \"DATEMODIFICATION\" ASC";
    }

    /**
     * @param lineResultSet
     * @param index
     * @return
     * @throws SQLException
     * @see fr.uem.efluid.tools.ManagedModelIdentifier#extractFromLine(java.sql.ResultSet,
     * int)
     */
    @Override
    public ManagedModelDescription extractFromLine(ResultSet lineResultSet, int index) throws SQLException {

        java.sql.Timestamp dateModification = lineResultSet.getTimestamp("DATEMODIFICATION");

        return new ManagedModelDescription(
                lineResultSet.getString("VERSION"),
                dateModification != null ? FormatUtils.toLdt(dateModification) : LocalDateTime.now(),
                lineResultSet.getString("DETAILS"),
                getDataSourceParameters().getUsername());
    }

    /**
     * @param identity
     * @param existingDescriptions
     * @return
     * @see fr.uem.efluid.tools.ManagedModelIdentifier#isValidPastModelIdentifier(java.lang.String,
     * java.util.List)
     */
    @Override
    public boolean isValidPastModelIdentifier(String identity, List<ManagedModelDescription> existingDescriptions) {
        // As we cannot check past versions, old ones are supposed valid ...
        return true;
    }

}
