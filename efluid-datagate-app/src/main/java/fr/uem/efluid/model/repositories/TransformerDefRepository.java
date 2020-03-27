package fr.uem.efluid.model.repositories;

import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.entities.TransformerDef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * TransformerDef management
 *
 * @author elecomte
 * @version 1
 * @since v1.2.0
 */
public interface TransformerDefRepository extends JpaRepository<TransformerDef, UUID> {

    List<TransformerDef> findByProject(Project project);
}
