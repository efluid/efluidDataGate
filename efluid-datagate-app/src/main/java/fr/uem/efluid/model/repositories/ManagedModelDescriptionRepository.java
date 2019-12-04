package fr.uem.efluid.model.repositories;

import java.util.List;

import fr.uem.efluid.model.metas.ManagedModelDescription;

/**
 * <p>
 * Access to managed database model description (version ID for example)
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public interface ManagedModelDescriptionRepository {

	/**
	 * <p>
	 * From the currently managed model, get the corresponding identifier, if any
	 * available. The processed query can depends on external identifier
	 * </p>
	 * 
	 * @return a list of all identified model descriptions. Cannot be null, be can be
	 *         empty
	 */
	List<ManagedModelDescription> getModelDescriptions();

	/**
	 * @return true if the description check is enabled / available
	 */
	boolean hasToCheckDescriptions();

	/**
	 * <p>
	 * From descriptions, if check is enabled, return the current model id. Else return
	 * null
	 * </p>
	 */
	String getCurrentModelIdentifier();

	IdentifierType getModelIdentifierType(String modelIdentifier);

	/**
	 * @author elecomte
	 * @since v0.0.8
	 * @version 1
	 */
	enum IdentifierType {
		CURRENT,
		OLD_ONE,
		UNKNOWN;
	}
}
