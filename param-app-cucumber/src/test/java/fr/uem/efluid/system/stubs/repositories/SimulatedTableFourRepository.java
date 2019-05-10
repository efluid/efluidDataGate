package fr.uem.efluid.system.stubs.repositories;

import fr.uem.efluid.system.stubs.entities.SimulatedTableFour;
import fr.uem.efluid.system.stubs.entities.SimulatedTableTwo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public interface SimulatedTableFourRepository extends JpaRepository<SimulatedTableFour, String> {

	// Standard repo
}
