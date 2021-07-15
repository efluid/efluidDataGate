package fr.uem.efluid.stubs;

import fr.uem.efluid.tools.diff.ManagedValueConverter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * For test value init
 *
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public interface SimulatedSourceRepository extends JpaRepository<SimulatedSource, Long> {

    /**
     * @param dataset
     */
    default void initFromDataset(Map<String, String> dataset, ManagedValueConverter converter) {
        saveAll(dataset.entrySet().stream().map(e -> TestUtils.entryToSource(e, converter)).collect(Collectors.toSet()));
    }

    @Modifying
    @Query(value = "DELETE FROM " + TestUtils.SOURCE_TABLE_NAME, nativeQuery = true)
    void cleanAll();
}
