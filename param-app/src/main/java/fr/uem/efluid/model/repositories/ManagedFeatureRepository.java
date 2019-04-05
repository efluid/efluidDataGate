package fr.uem.efluid.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.uem.efluid.model.entities.ManagedFeature;
import fr.uem.efluid.services.Feature;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public interface ManagedFeatureRepository extends JpaRepository<ManagedFeature, Feature> {

	/**
	 * @param feat
	 * @return
	 */
	@Query("select enabled from ManagedFeature where feature = :feat")
	boolean getManagedState(@Param("feat") Feature feat);
}
