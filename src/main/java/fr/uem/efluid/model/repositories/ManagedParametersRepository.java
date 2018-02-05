package fr.uem.efluid.model.repositories;

import java.util.Map;

import fr.uem.efluid.model.entities.DictionaryEntry;

/**
 * <p>
 * Access to raw data of parameters : can read from Parameter source table or regenerate
 * from existing index
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface ManagedParametersRepository {

	/**
	 * <p>
	 * Get real content from managed parameters tables
	 * </p>
	 * 
	 * @param parameterEntry
	 *            table identifier for extraction
	 * @return
	 */
	Map<String, String> extractCurrentContent(DictionaryEntry parameterEntry);

	/**
	 * Produces the knew content for specified table, from recorded index
	 * 
	 * @param parameterEntry
	 * @return
	 */
	Map<String, String> regenerateKnewContent(DictionaryEntry parameterEntry);

}
