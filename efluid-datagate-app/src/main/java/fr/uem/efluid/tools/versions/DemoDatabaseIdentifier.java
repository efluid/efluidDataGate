package fr.uem.efluid.tools.versions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import fr.uem.efluid.model.metas.ManagedModelDescription;
import fr.uem.efluid.utils.DatasourceUtils;
import fr.uem.efluid.utils.FormatUtils;

/**
 * <p>
 * A demo identifier implementation using table "TDEMO_VERSION" for version search
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class DemoDatabaseIdentifier extends ManagedModelIdentifier {

	public DemoDatabaseIdentifier(DatasourceUtils.CustomDataSourceParameters parameters) {
		super(parameters);
	}

	/**
	 * @return
	 * @see ManagedModelIdentifier#getAllModelDescriptionQuery()
	 */
	@Override
	public String getAllModelDescriptionQuery() {
		return "SELECT \"VERSION\", \"UPDATE_TIME\", \"DETAIL\" FROM \"TDEMO_VERSION\" ORDER BY \"UPDATE_TIME\" ASC";
	}

	/**
	 * @param lineResultSet
	 * @param index
	 * @return
	 * @throws SQLException
	 * @see ManagedModelIdentifier#extractFromLine(java.sql.ResultSet,
	 *      int)
	 */
	@Override
	public ManagedModelDescription extractFromLine(ResultSet lineResultSet, int index) throws SQLException {

		return new ManagedModelDescription(
				lineResultSet.getString("VERSION"),
				FormatUtils.toLdt(lineResultSet.getTimestamp("UPDATE_TIME")),
				lineResultSet.getString("DETAIL"),
				getDataSourceParameters().getUsername());
	}

	/**
	 * @param identity
	 * @param existingDescriptions
	 * @return
	 * @see ManagedModelIdentifier#isValidPastModelIdentifier(java.lang.String,
	 *      java.util.List)
	 */
	@Override
	public boolean isValidPastModelIdentifier(String identity, List<ManagedModelDescription> existingDescriptions) {
		// All the valid versions are always provided, so must be in here
		return existingDescriptions.stream().anyMatch(d -> d.getIdentity().equals(identity));
	}

}
