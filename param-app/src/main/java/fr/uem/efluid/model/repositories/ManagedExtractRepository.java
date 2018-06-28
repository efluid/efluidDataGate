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
 * @version 1
 */
public interface ManagedExtractRepository {

	/**
	 * <p>
	 * Get real content from managed parameters tables
	 * </p>
	 * 
	 * @param parameterEntry
	 *            table identifier for extraction
	 * @return
	 */
	Map<String, String> extractCurrentContent(DictionaryEntry parameterEntry, Map<String, byte[]> lobs, Project project);

}
