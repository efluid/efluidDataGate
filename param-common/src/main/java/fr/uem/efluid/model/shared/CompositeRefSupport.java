package fr.uem.efluid.model.shared;

import fr.uem.efluid.model.Shared;

/**
 * <p>
 * Common features for referenced items with composite key support
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public interface CompositeRefSupport<D extends ExportAwareDictionaryEntry<?>> extends Shared {

	/**
	 * @return
	 */
	String getName();

	/**
	 * @return
	 */
	String getColumnFrom();

	/**
	 * @param tableTo
	 *            the tableTo to set
	 */
	String getTableTo();

	/**
	 * @param columnTo
	 *            the columnTo to set
	 */
	String getColumnTo();

	/**
	 * @return the dictionaryEntry
	 */
	D getDictionaryEntry();

	/**
	 * @return
	 */
	String getExt1ColumnTo();

	/**
	 * @return
	 */
	String getExt2ColumnTo();

	/**
	 * @return
	 */
	String getExt3ColumnTo();

	/**
	 * @return
	 */
	String getExt4ColumnTo();

	/**
	 * @return
	 */
	String getExt1ColumnFrom();

	/**
	 * @return
	 */
	String getExt2ColumnFrom();

	/**
	 * @return
	 */
	String getExt3ColumnFrom();

	/**
	 * @return
	 */
	String getExt4ColumnFrom();

}
