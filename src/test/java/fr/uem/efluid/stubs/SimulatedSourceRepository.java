package fr.uem.efluid.stubs;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.uem.efluid.TestUtils;

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
	default void initFromDataset(Map<String, String> dataset) {
		save(dataset.entrySet().stream().map(TestUtils::entryToSource).collect(Collectors.toSet()));
	}
}
