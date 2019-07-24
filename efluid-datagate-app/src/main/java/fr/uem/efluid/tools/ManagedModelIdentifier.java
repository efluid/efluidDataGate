package fr.uem.efluid.tools;

import fr.uem.efluid.model.metas.ManagedModelDescription;
import fr.uem.efluid.utils.DatasourceUtils.CustomDataSourceParameters;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * <p>
 * Basic process for extracting the managed database identity when required.
 * </p>
 * <p>
 * Implemented by application regarding its own description holder or whatever can
 * describe a managed database with a JDBC call
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
public abstract class ManagedModelIdentifier {

    private final CustomDataSourceParameters parameters;

    public ManagedModelIdentifier(CustomDataSourceParameters parameters) {
        this.parameters = parameters;
    }

    /**
     * For access to some datasource related values
     *
     * @return specified DS params
     */
    protected CustomDataSourceParameters getDataSourceParameters() {
        return this.parameters;
    }

    /**
     * <p>
     * Model-only SQL query string providing the model description content line by line.
     * It is required that the descriptions (if more than one can be identified) are
     * ordered by update date and that the last one <b>must</b> be the currently active
     * one
     * </p>
     *
     * @return
     */
    public abstract String getAllModelDescriptionQuery();

    /**
     * <p>
     * For a managed model execution of the line generated with
     * {@link #getAllModelDescriptionQuery()}, transform the <tt>ResultSet</tt> into
     * required <tt>ManagedModelDescription</tt>. Index starts with 0, ResultSet is
     * already moved to the right position
     * </p>
     *
     * @param lineResultSet
     * @param index
     * @return
     * @throws SQLException
     */
    public abstract ManagedModelDescription extractFromLine(ResultSet lineResultSet, int index) throws SQLException;

    /**
     * <p>
     * Allows to specify a clear compliance rule for <code>old</code> model identity,
     * regarding the implemented model rules. Can check into existing descriptions
     * (provided as a convenience) or whatever can be useful to do so
     * </p>
     *
     * @param identity
     * @param existingDescriptions
     * @return
     */
    public abstract boolean isValidPastModelIdentifier(String identity, List<ManagedModelDescription> existingDescriptions);
}
