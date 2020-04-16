package fr.uem.efluid.model.repositories;

import fr.uem.efluid.model.entities.Export;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Management of Export data
 *
 * @author elecomte
 * @version 1
 * @since v1.1.0
 */
public interface ExportRepository extends JpaRepository<Export, UUID> {

    // Use standard features

}
