package fr.uem.efluid.services;

import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.IntegrationTestConfig;
import fr.uem.efluid.TestUtils;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.ManagedParametersRepository;
import fr.uem.efluid.stubs.TestDataLoader;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@SpringBootTest(classes = { IntegrationTestConfig.class })
public class DataDiffServiceBenchTest {

	@Autowired
	private ManagedParametersRepository managed;

	@Autowired
	private DataDiffService service;

	@Autowired
	private TestDataLoader loader;

	@Autowired
	private DictionaryRepository dictionary;

	private UUID dictionaryEntryUuid;

	@Transactional
	public void setupDatabase(String name) {
		this.dictionaryEntryUuid = this.loader.setupDatabaseForDiff(name);
	}

	// TODO : add combined benchmarks for full diff, with database

	@Test
	public void testGenerateDiffBench() {

		final int play = 25;

		int totalLargeKnew = 0;
		int totalLargeActual = 0;
		int totalIndex = 0;
		long totalDurationParallel = 0;
		long totalDurationNotParallel = 0;
		long minDurationParallel = 0;
		long maxDurationParallel = 0;
		long minDurationNotParallel = 0;
		long maxDurationNotParallel = 0;

		// Repeated run
		for (int i = 0; i < play; i++) {
			// Prepare large sources (~100k lines each, 10% differences)
			Map<String, String> largeActual = TestUtils.multiplyDataset(TestUtils.readDataset("diff6/actual.csv"), 1000);

			// Knews are modified during diff : use duplicates for double test
			Map<String, String> largeKnew1 = TestUtils.multiplyDataset(TestUtils.readDataset("diff6/knew.csv"), 1000);
			Map<String, String> largeKnew2 = TestUtils.multiplyDataset(TestUtils.readDataset("diff6/knew.csv"), 1000);

			totalLargeActual += largeActual.size();
			totalLargeKnew += largeKnew1.size();

			// Not Parallel
			this.service.applyParallelMode(false);
			long start = System.currentTimeMillis();
			totalIndex += this.service.generateDiffIndex(largeKnew1, largeActual).size();
			long delay = System.currentTimeMillis() - start;
			totalDurationNotParallel += delay;
			if (minDurationNotParallel == 0 || minDurationNotParallel > delay) {
				minDurationNotParallel = delay;
			}
			if (maxDurationNotParallel < delay) {
				maxDurationNotParallel = delay;
			}

			// parallel
			this.service.applyParallelMode(false);
			start = System.currentTimeMillis();
			totalIndex += this.service.generateDiffIndex(largeKnew2, largeActual).size();
			delay = System.currentTimeMillis() - start;
			totalDurationParallel += delay;
			if (minDurationParallel == 0 || minDurationParallel > delay) {
				minDurationParallel = delay;
			}
			if (maxDurationParallel < delay) {
				maxDurationParallel = delay;
			}
		}

		System.out.println("Processed large bench. For AVG " + totalLargeActual / play + " actual, over " + totalLargeKnew / play
				+ " knew lines, found "
				+ totalIndex / (2 * play) + " index entries. AVG Duration in parallel mode = "
				+ Double.valueOf(totalDurationParallel / play).intValue() + "ms (min = " + minDurationParallel + "/ max = "
				+ maxDurationParallel + "), in not parallel mode = "
				+ Double.valueOf(totalDurationNotParallel / play).intValue() + "ms (min = " + minDurationNotParallel + "/ max = "
				+ maxDurationNotParallel + ")");
	}

}
