package fr.uem.efluid.model.repositories;

import fr.uem.efluid.model.entities.Upgrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;

public interface UpgradeRepository extends JpaRepository<Upgrade, String> {


    @Query("select name from Upgrade")
    Collection<String> findRanUpgradeNames();

}
