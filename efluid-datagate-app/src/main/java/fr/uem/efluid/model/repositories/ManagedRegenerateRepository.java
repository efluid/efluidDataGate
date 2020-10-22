package fr.uem.efluid.model.repositories;

import java.util.List;
import java.util.Map;

import fr.uem.efluid.model.DiffLine;
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
public interface ManagedRegenerateRepository {

	/**
	 * <p>
	 * Produces the knew content for specified table, from recorded index
	 * </p>
	 */
	Map<String, String> regenerateKnewContent(DictionaryEntry parameterEntry);

	/**
	 * <p>
	 * Produces the knew content for specified table, from sorted recorded index on parameter
	 * until specified ldt, then with specified index (adapted for merge process)
	 * </p>
	 */
	Map<String, String> regenerateKnewContent(/* sorted */List<? extends DiffLine> specifiedIndex);

	/**
	 * Refresh cached data if any
	 */
	void refreshAll();
}
