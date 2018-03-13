package fr.uem.efluid.stubs;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.uem.efluid.tools.ManagedValueConverter;

/**
 * For test value init
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface SimulatedSourceRepository extends JpaRepository<SimulatedSource, Long> {

	/**
	 * @param dataset
	 */
	default void initFromDataset(Map<String, String> dataset, ManagedValueConverter converter) {
		saveAll(dataset.entrySet().stream().map(e -> TestUtils.entryToSource(e, converter)).collect(Collectors.toSet()));
	}
}
