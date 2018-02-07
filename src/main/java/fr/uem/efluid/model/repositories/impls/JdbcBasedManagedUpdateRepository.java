package fr.uem.efluid.model.repositories.impls;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.ManagedUpdateRepository;
import fr.uem.efluid.tools.ManagedQueriesGenerator;
import fr.uem.efluid.tools.ManagedValueConverter;

/**
 * <p>
 * Very basic implements, with batched process but not preparedStatement : all queries are
 * ran as predefined.
 * </p>
 * <p>
 * Process the priority checking depends between tables
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class JdbcBasedManagedUpdateRepository implements ManagedUpdateRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(JdbcBasedManagedUpdateRepository.class);

	@Autowired
	private JdbcTemplate managedSource;

	@Autowired
	private ManagedQueriesGenerator queryGenerator;

	@Autowired
	private ManagedValueConverter payloadConverter;

	@Autowired
	private DictionaryRepository dictionary;

	/**
	 * @param entry
	 * @param lines
	 * @see fr.uem.efluid.model.repositories.ManagedUpdateRepository#runAllChanges(fr.uem.efluid.model.entities.DictionaryEntry,
	 *      java.util.List)
	 */
	@Override
	public void runAllChanges(List<DiffLine> lines) {

		// Sort by Dictionary Entry
		Map<UUID, List<DiffLine>> byDict = lines.stream().collect(Collectors.groupingBy(DiffLine::getDictionaryEntryUuid));

		LOGGER.debug("Identified change to apply on managed DB. Will process {} diffLines, on {} tables",
				Integer.valueOf(lines.size()), Integer.valueOf(byDict.size()));

		// Prepare all queries
		String[] queries = byDict.entrySet().stream().flatMap(e -> {
			DictionaryEntry dict = this.dictionary.findOne(e.getKey());
			return e.getValue().stream().map(d -> queryFor(dict, d));
		}).toArray(String[]::new);

		// Use batch update
		this.managedSource.batchUpdate(queries);
	}

	/**
	 * @param entry
	 * @param line
	 * @return
	 */
	private String queryFor(DictionaryEntry entry, DiffLine line) {

		switch (line.getAction()) {
		case ADD:
			return this.queryGenerator.producesApplyAddQuery(entry, line.getKeyValue(),
					this.payloadConverter.expandInternalValue(line.getPayload()));
		case REMOVE:
			return this.queryGenerator.producesApplyRemoveQuery(entry, line.getKeyValue());
		case UPDATE:
		default:
			return this.queryGenerator.producesApplyUpdateQuery(entry, line.getKeyValue(),
					this.payloadConverter.expandInternalValue(line.getPayload()));
		}
	}

	/**
	 * 
	 */
	private void processConstaints() {

	}
}
