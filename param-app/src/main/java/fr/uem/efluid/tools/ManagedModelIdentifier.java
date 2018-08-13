package fr.uem.efluid.tools;

import java.sql.ResultSet;

import fr.uem.efluid.model.metas.ManagedModelDescription;

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
 * @since v0.0.8
 * @version 1
 */
public interface ManagedModelIdentifier {

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
	String getAllModelDescriptionQuery();

	/**
	 * <p>
	 * For a managed model execution of the line generated with
	 * {@link #getAllModelDescriptionQuery()}, transform the <tt>ResultSet</tt> into
	 * required <tt>ManagedModelDescription</tt>. Index starts with 0
	 * </p>
	 * 
	 * @param lineResultSet
	 * @param index
	 * @return
	 */
	ManagedModelDescription extractFromLine(ResultSet lineResultSet, int index);
}
