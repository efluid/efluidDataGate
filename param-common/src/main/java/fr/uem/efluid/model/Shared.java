package fr.uem.efluid.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * <p>
 * Spec for an entity which can be exported and imported between app instances.
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface Shared {

	/**
	 * @return
	 */
	UUID getUuid();

	/**
	 * @return
	 */
	LocalDateTime getImportedTime();
	
	/**
	 * @return
	 */
	String serialize();
	
	/**
	 * @param raw
	 */
	void deserialize(String raw);
}
