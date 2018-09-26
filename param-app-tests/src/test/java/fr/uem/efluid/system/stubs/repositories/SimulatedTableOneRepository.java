package fr.uem.efluid.system.stubs.repositories;

import org.springframework.data.repository.CrudRepository;

import fr.uem.efluid.system.stubs.entities.SimulatedTableOne;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public interface SimulatedTableOneRepository extends CrudRepository<SimulatedTableOne, Long> {

	// Standard repo
}
