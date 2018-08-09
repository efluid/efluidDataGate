package fr.uem.efluid.model.repositories;

import java.util.Map;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.Project;

/**
 * <p>
 * Access to raw data of parameters : can read from Parameter source table or regenerate
 * from existing index
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 2
 */
public interface ManagedExtractRepository {

	/**
	 * <p>
	 * Get real content from managed parameters tables
	 * </p>
	 * 
	 * @param parameterEntry
	 *            table identifier for extraction
	 * @param lobs
	 *            where the extracted lob values will be stored
	 * @param project
	 *            working project
	 * @return extracted content (key - payload map)
	 */
	Map<String, String> extractCurrentContent(DictionaryEntry parameterEntry, Map<String, byte[]> lobs, Project project);

	/**
	 * <p>
	 * Get the content which should be extracted if unchecked joins was valid
	 * </p>
	 * 
	 * @param parameterEntry
	 *            table identifier for extraction
	 * @param project
	 *            working project
	 * @return extracted missing content (key - payload map)
	 */
	Map<String, String> extractCurrentMissingContentWithUncheckedJoins(DictionaryEntry parameterEntry, Project project);

	/**
	 * <p>
	 * For the specified table, get the current count of result with standard select
	 * criteria and with unchecked join. This allows to compare if their is count
	 * differences between extraction and existing values, to check if their is some
	 * unmatched join condition (this situation occurs a lot as EFLUID is mostly using
	 * unspecified foreign key)
	 * </p>
	 * 
	 * @param parameterEntry
	 *            table identifier for extraction
	 * @param project
	 *            working project
	 * @return current count with unchecked join
	 */
	int countCurrentContentWithUncheckedJoins(DictionaryEntry parameterEntry, Project project);
}
