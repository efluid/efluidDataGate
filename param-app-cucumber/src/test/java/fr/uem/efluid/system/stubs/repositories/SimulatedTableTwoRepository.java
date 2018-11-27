package fr.uem.efluid.system.stubs.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.uem.efluid.system.stubs.entities.SimulatedTableTwo;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public interface SimulatedTableTwoRepository extends JpaRepository<SimulatedTableTwo, String> {

	// Standard repo
}
