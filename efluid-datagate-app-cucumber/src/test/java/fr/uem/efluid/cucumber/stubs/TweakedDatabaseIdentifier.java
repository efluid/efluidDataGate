package fr.uem.efluid.cucumber.stubs;

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
public class TweakedDatabaseIdentifier extends ManagedModelIdentifier {

    private String fixedVersion;
    private boolean hasHistory;

    /**
     *
     */
    public TweakedDatabaseIdentifier(CustomDataSourceParameters parameters) {
        super(parameters);
        reset();
    }

    /**
     *
     */
    public void reset() {
        this.fixedVersion = "1.0.0";
        this.hasHistory = true;
    }

    /**
     * @param hasHistory the hasHistory to set
     */
    public void setHasHistory(boolean hasHistory) {
        this.hasHistory = hasHistory;
    }

    /**
     * @param fixedVersion the fixedVersion to set
     */
    public void setFixedVersion(String fixedVersion) {
        this.fixedVersion = fixedVersion;
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
     * @see ManagedModelIdentifier#extractFromLine(java.sql.ResultSet,
     * int)
     */
    @Override
    public ManagedModelDescription extractFromLine(ResultSet lineResultSet, int index) throws SQLException {
        return new ManagedModelDescription(this.fixedVersion, LocalDateTime.now(), "Tweaked version", "TWEAKED");
    }

    /**
     * @param identity
     * @param existingDescriptions
     * @return
     * @see ManagedModelIdentifier#isValidPastModelIdentifier(java.lang.String,
     * java.util.List)
     */
    @Override
    public boolean isValidPastModelIdentifier(String identity, List<ManagedModelDescription> existingDescriptions) {
        return this.hasHistory;
    }
}
