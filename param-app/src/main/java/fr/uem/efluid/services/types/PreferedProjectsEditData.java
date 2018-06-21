package fr.uem.efluid.services.types;

import java.util.List;
import java.util.UUID;

/**
 * <p>
 * Form content when editing user prefered projects
 * </p>
 * 
 * @author elecomte
 * @since v0.2.0
 * @version 1
 */
public class PreferedProjectsEditData {

	private List<UUID> preferedProjectUuids;

	/**
	 * 
	 */
	public PreferedProjectsEditData() {
		super();
	}

	/**
	 * @return the preferedProjectUuids
	 */
	public List<UUID> getPreferedProjectUuids() {
		return this.preferedProjectUuids;
	}

	/**
	 * @param preferedProjectUuids
	 *            the preferedProjectUuids to set
	 */
	public void setPreferedProjectUuids(List<UUID> preferedProjectUuids) {
		this.preferedProjectUuids = preferedProjectUuids;
	}

}
