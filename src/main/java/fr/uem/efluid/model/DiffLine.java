package fr.uem.efluid.model;

import java.util.UUID;

import fr.uem.efluid.model.entities.IndexAction;

/**
 * <p>
 * Minimal model for a diff line
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface DiffLine {

	UUID getDictionaryEntryUuid();

	String getKeyValue();

	String getPayload();

	IndexAction getAction();
}
