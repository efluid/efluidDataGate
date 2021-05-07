package fr.uem.efluid.model.repositories;

import fr.uem.efluid.model.entities.Export;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
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

    @Query(value = "UPDATE EXPORTS SET DOWNLOADED_TIME = :time: WHERE UUID = :uuid:", nativeQuery = true)
    @Modifying
    void setDownloadedExportTime(UUID exportUUID, LocalDateTime time);

}
