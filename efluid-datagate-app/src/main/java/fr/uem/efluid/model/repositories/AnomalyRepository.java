package fr.uem.efluid.model.repositories;

import fr.uem.efluid.model.AnomalyContextType;
import fr.uem.efluid.model.entities.Anomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AnomalyRepository extends JpaRepository<Anomaly, Long> {

    List<Anomaly> findByDetectTimeAfter(LocalDateTime time);

    List<Anomaly> findByContextTypeAndDetectTimeAfter(AnomalyContextType contextType, LocalDateTime time);

    List<Anomaly> findByContextTypeAndContextName(AnomalyContextType contextType, String contextName);

    @Query("select distinct a.contextName from Anomaly a where a.contextType = :type order by a.detectTime asc")
    List<String> findContextNamesForType(@Param("type") AnomalyContextType contextType);
}
