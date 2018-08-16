package fr.uem.efluid.tools;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import fr.uem.efluid.model.metas.ManagedModelDescription;
import fr.uem.efluid.utils.FormatUtils;

/**
 * <p>
 * The identifier implementation for <b>Efluid</b>
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class EfluidDatabaseIdentifier implements ManagedModelIdentifier {

	/**
	 * @return
	 * @see fr.uem.efluid.tools.ManagedModelIdentifier#getAllModelDescriptionQuery()
	 */
	@Override
	public String getAllModelDescriptionQuery() {
		return "SELECT \"VERSION\", \"DATECREATION\", concat(\"PROJET\",concat(' ', \"MESSAGEINFO\")) as \"DETAILS\" FROM \"TAPPLICATIONINFO\" ORDER BY \"DATECREATION\" ASC";
	}

	/**
	 * @param lineResultSet
	 * @param index
	 * @return
	 * @throws SQLException
	 * @see fr.uem.efluid.tools.ManagedModelIdentifier#extractFromLine(java.sql.ResultSet,
	 *      int)
	 */
	@Override
	public ManagedModelDescription extractFromLine(ResultSet lineResultSet, int index) throws SQLException {

		return new ManagedModelDescription(
				lineResultSet.getString("VERSION"),
				FormatUtils.toLdt(lineResultSet.getTimestamp("DATECREATION")),
				lineResultSet.getString("DETAILS"));
	}

	/**
	 * @param identity
	 * @param existingDescriptions
	 * @return
	 * @see fr.uem.efluid.tools.ManagedModelIdentifier#isValidPastModelIdentifier(java.lang.String,
	 *      java.util.List)
	 */
	@Override
	public boolean isValidPastModelIdentifier(String identity, List<ManagedModelDescription> existingDescriptions) {
		// As we cannot check past versions, old ones are supposed valid ...
		return true;
	}

}
