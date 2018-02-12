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
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.ManagedExtractRepository;
import fr.uem.efluid.model.repositories.ManagedRegenerateRepository;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.stubs.TestDataLoader;
import fr.uem.efluid.stubs.TestUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@SpringBootTest(classes = { IntegrationTestConfig.class })
public class PrepareDiffServiceBenchTest {

	@Autowired
	private ManagedRegenerateRepository regenerate;

	@Autowired
	private ManagedExtractRepository extract;

	@Autowired
	private PrepareDiffService service;

	@Autowired
	private TestDataLoader loader;

	@Autowired
	private DictionaryRepository dictionary;

	private UUID dictionaryEntryUuid;

	@Transactional
	public void setupSourceOnly(String name) {
		this.loader.setupSourceDatabaseForDiff(name);
		this.dictionaryEntryUuid = this.loader.setupDictionnaryForDiff().getUuid();
	}

	@Transactional
	public void setupIndexOnly(String name) {
		this.dictionaryEntryUuid = this.loader.setupIndexDatabaseForDiff(name).getUuid();
	}

	@Transactional
	public void setupFullDatabase(String name) {
		this.dictionaryEntryUuid = this.loader.setupDatabaseForDiff(name);
	}

	/**
	 * 
	 */
	@Test
	public void runBenchmarks() {

		System.out.println("#### BENCHMARKING OF DIFF SERVICE AND ASSOCIATED FEATURES ####");
		runExtractBenchmark();
		runRegenerateBenchmark();
		runRandomStaticDiffBenchmark();
		runFullDiffBenchmark();
	}

	/**
	 * 
	 */
	private void runFullDiffBenchmark() {

		final int play = 25;

		long totalDuration = 0;
		long minDuration = 0;
		long maxDuration = 0;

		for (int i = 0; i < play; i++) {
			setupFullDatabase("diff8");
			long start = System.currentTimeMillis();
			this.service.currentContentDiff(this.dictionary.findOne(this.dictionaryEntryUuid));
			long delay = System.currentTimeMillis() - start;
			totalDuration += delay;
			if (minDuration == 0 || minDuration > delay) {
				minDuration = delay;
			}
			if (maxDuration < delay) {
				maxDuration = delay;
			}
		}

		System.out.println(" * FULL DIFF => AVG Duration = "
				+ Double.valueOf(totalDuration / play).intValue() + "ms (min = " + minDuration + "/ max = "
				+ maxDuration + ")");
	}

	/**
	 * 
	 */
	private void runRegenerateBenchmark() {

		final int play = 25;

		long totalDuration = 0;
		long minDuration = 0;
		long maxDuration = 0;

		for (int i = 0; i < play; i++) {
			setupIndexOnly("diff8");
			long start = System.currentTimeMillis();
			this.regenerate.regenerateKnewContent(this.dictionary.findOne(this.dictionaryEntryUuid));
			long delay = System.currentTimeMillis() - start;
			totalDuration += delay;
			if (minDuration == 0 || minDuration > delay) {
				minDuration = delay;
			}
			if (maxDuration < delay) {
				maxDuration = delay;
			}
		}

		System.out.println(" * REGENERATE KNEW CONTENT => AVG Duration = "
				+ Double.valueOf(totalDuration / play).intValue() + "ms (min = " + minDuration + "/ max = "
				+ maxDuration + ")");
	}

	/**
	 * 
	 */
	private void runExtractBenchmark() {

		final int play = 25;

		long totalDuration = 0;
		long minDuration = 0;
		long maxDuration = 0;

		for (int i = 0; i < play; i++) {
			setupSourceOnly("diff8");
			long start = System.currentTimeMillis();
			this.extract.extractCurrentContent(this.dictionary.findOne(this.dictionaryEntryUuid));
			long delay = System.currentTimeMillis() - start;
			totalDuration += delay;
			if (minDuration == 0 || minDuration > delay) {
				minDuration = delay;
			}
			if (maxDuration < delay) {
				maxDuration = delay;
			}
		}

		System.out.println(" * SOURCE EXTRACT => AVG Duration = "
				+ Double.valueOf(totalDuration / play).intValue() + "ms (min = " + minDuration + "/ max = "
				+ maxDuration + ")");
	}

	/**
	 * 
	 */
	private void runRandomStaticDiffBenchmark() {

		final int play = 25;
		final DictionaryEntry dic = this.dictionary.findOne(this.dictionaryEntryUuid);

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
			Map<String, String> largeActual = TestUtils.multiplyDataset(this.loader.readDataset("diff6/actual.csv"), 1000);

			// Knews are modified during diff : use duplicates for double test
			Map<String, String> largeKnew1 = TestUtils.multiplyDataset(this.loader.readDataset("diff6/knew.csv"), 1000);
			Map<String, String> largeKnew2 = TestUtils.multiplyDataset(this.loader.readDataset("diff6/knew.csv"), 1000);

			totalLargeActual += largeActual.size();
			totalLargeKnew += largeKnew1.size();

			// Not Parallel
			this.service.applyParallelMode(false);
			long start = System.currentTimeMillis();
			totalIndex += this.service.generateDiffIndexFromContent(PreparedIndexEntry::new, largeKnew1, largeActual, dic).size();
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
			totalIndex += this.service.generateDiffIndexFromContent(PreparedIndexEntry::new, largeKnew2, largeActual, dic).size();
			delay = System.currentTimeMillis() - start;
			totalDurationParallel += delay;
			if (minDurationParallel == 0 || minDurationParallel > delay) {
				minDurationParallel = delay;
			}
			if (maxDurationParallel < delay) {
				maxDurationParallel = delay;
			}
		}

		System.out.println(
				" * STATIC DIFF WITH RANDOM VALUES =>  For AVG " + totalLargeActual / play + " actual, over " + totalLargeKnew / play
						+ " knew lines, found "
						+ totalIndex / (2 * play) + " index entries. AVG Duration in parallel mode = "
						+ Double.valueOf(totalDurationParallel / play).intValue() + "ms (min = " + minDurationParallel + "/ max = "
						+ maxDurationParallel + "), in not parallel mode = "
						+ Double.valueOf(totalDurationNotParallel / play).intValue() + "ms (min = " + minDurationNotParallel + "/ max = "
						+ maxDurationNotParallel + ")");
	}

}
