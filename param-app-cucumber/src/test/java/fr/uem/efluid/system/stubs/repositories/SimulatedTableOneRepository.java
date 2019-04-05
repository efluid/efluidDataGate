package fr.uem.efluid.system.stubs.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.uem.efluid.system.stubs.entities.SimulatedTableOne;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public interface SimulatedTableOneRepository extends JpaRepository<SimulatedTableOne, Long> {

	// Standard repo
}
