package fr.uem.efluid.model.repositories;

import fr.uem.efluid.model.metas.ManagedModelDescription;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public interface ManagedModelDescriptionRepository {

	/**
	 * <p>
	 * From the currently managed model, get the corresponding identifier, if any
	 * available
	 * </p>
	 * 
	 * @return
	 */
	ManagedModelDescription getCurrentModelDescription();
}
