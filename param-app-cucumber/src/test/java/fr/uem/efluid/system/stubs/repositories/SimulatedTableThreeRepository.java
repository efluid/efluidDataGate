package fr.uem.efluid.system.stubs.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.uem.efluid.system.stubs.entities.SimulatedTableThree;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public interface SimulatedTableThreeRepository extends JpaRepository<SimulatedTableThree, String> {

	// Standard repo
}
