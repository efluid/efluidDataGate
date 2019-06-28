package fr.uem.efluid.services;

import fr.uem.efluid.model.repositories.FeatureManager;
import org.springframework.beans.factory.annotation.Autowired;

import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.security.UserHolder;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public abstract class AbstractApplicationService {

	@Autowired
	protected UserHolder holder;

	@Autowired
	protected FeatureManager features;

	/**
	 * @return
	 */
	protected User getCurrentUser() {
		return this.holder.getCurrentUser();
	}

	/**
	 * @param value
	 * @return
	 */
	protected static boolean isNotEmpty(String value) {
		if (value == null) {
			return false;
		}
		return value.length() > 0;
	}
}
