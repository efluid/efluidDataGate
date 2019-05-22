package fr.uem.efluid.system.stubs.repositories;

import fr.uem.efluid.system.stubs.entities.SimulatedTableFive;
import fr.uem.efluid.system.stubs.entities.SimulatedTableFour;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulatedTableFiveRepository  extends JpaRepository<SimulatedTableFive, String> {

    // Standard repo
}
