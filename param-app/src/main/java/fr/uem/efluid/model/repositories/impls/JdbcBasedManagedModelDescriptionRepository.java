package fr.uem.efluid.model.repositories.impls;

import org.springframework.stereotype.Repository;

import fr.uem.efluid.model.metas.ManagedModelDescription;
import fr.uem.efluid.model.repositories.ManagedModelDescriptionRepository;

/**
 * <p>
 * Uses a specified <tt>ManagedModelIdentifier</tt> found in context
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@Repository
public class JdbcBasedManagedModelDescriptionRepository implements ManagedModelDescriptionRepository {

	/**
	 * @return
	 * @see fr.uem.efluid.model.repositories.ManagedModelDescriptionRepository#getCurrentModelDescription()
	 */
	@Override
	public ManagedModelDescription getCurrentModelDescription() {
		// TODO Auto-generated method stub
		return null;
	}

}
