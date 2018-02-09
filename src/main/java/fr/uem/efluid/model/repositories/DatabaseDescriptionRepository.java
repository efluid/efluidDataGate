package fr.uem.efluid.model.repositories;

import java.util.Collection;

import fr.uem.efluid.model.metas.TableDescription;
import fr.uem.efluid.utils.TechnicalException;

/**
 * <p>
 * Provider of all table descriptions for Managed database. Implements can use various
 * stategy to get access to table descriptions, and use a cache.
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface DatabaseDescriptionRepository {

	/**
	 * @return completed TableDescription for the managed database.
	 * @throws TechnicalException
	 */
	public Collection<TableDescription> getTables() throws TechnicalException;

	/**
	 * Force refresh on cached data if any
	 */
	public void refreshAll();
}