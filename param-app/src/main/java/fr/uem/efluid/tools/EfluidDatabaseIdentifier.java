package fr.uem.efluid.tools;

import java.sql.ResultSet;

import fr.uem.efluid.model.metas.ManagedModelDescription;

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
		return null;
	}

	/**
	 * @param lineResultSet
	 * @param index
	 * @return
	 * @see fr.uem.efluid.tools.ManagedModelIdentifier#extractFromLine(java.sql.ResultSet,
	 *      int)
	 */
	@Override
	public ManagedModelDescription extractFromLine(ResultSet lineResultSet, int index) {
		return null;
	}

}
