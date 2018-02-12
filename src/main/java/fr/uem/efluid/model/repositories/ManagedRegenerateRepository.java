package fr.uem.efluid.model.repositories;

import java.util.List;
import java.util.Map;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexEntry;

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
public interface ManagedRegenerateRepository {

	/**
	 * Produces the knew content for specified table, from recorded index
	 * 
	 * @param parameterEntry
	 * @return
	 */
	Map<String, String> regenerateKnewContent(DictionaryEntry parameterEntry);

	/**
	 * Produces the knew content for specified table, from recorded index
	 * 
	 * @param parameterEntry
	 * @param specifiedIndex
	 * @return
	 */
	Map<String, String> regenerateKnewContent(List<IndexEntry> specifiedIndex);

}
