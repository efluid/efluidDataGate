package fr.uem.efluid.model.repositories.impls;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.ManagedUpdateRepository;
import fr.uem.efluid.model.repositories.TableLinkRepository;
import fr.uem.efluid.tools.ManagedQueriesGenerator;
import fr.uem.efluid.tools.ManagedValueConverter;
import fr.uem.efluid.utils.TechnicalException;

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
@Repository
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

	@Autowired
	private TableLinkRepository links;

	/**
	 * @param entry
	 * @param lines
	 * @see fr.uem.efluid.model.repositories.ManagedUpdateRepository#runAllChanges(fr.uem.efluid.model.entities.DictionaryEntry,
	 *      java.util.List)
	 */
	@Override
	public void runAllChanges(List<DiffLine> lines) {

		LOGGER.debug("Identified change to apply on managed DB. Will process {} diffLines", Integer.valueOf(lines.size()));

		// Preload dictionary for direct access by uuid
		Map<UUID, DictionaryEntry> dictEntries = this.dictionary.findAll().stream()
				.collect(Collectors.toMap(DictionaryEntry::getUuid, v -> v));

		// Prepare all queries, ordered by dictionary entry and action regarding links
		String[] queries = lines.stream()
				.sorted(sortedByLinks())
				.map(e -> queryFor(dictEntries.get(e.getDictionaryEntryUuid()), e))
				.toArray(String[]::new);

		// Debug all content
		if (LOGGER.isDebugEnabled()) {
			// Reproduce content to debug - heavy loading !!!
			LOGGER.debug("Queries Before sort : \n{}", lines.stream().map(e -> queryFor(dictEntries.get(e.getDictionaryEntryUuid()), e))
					.collect(Collectors.joining("\n")));
			LOGGER.debug("Queries After sort : \n{}", lines.stream().sorted(sortedByLinks())
					.map(e -> queryFor(dictEntries.get(e.getDictionaryEntryUuid()), e)).collect(Collectors.joining("\n")));
		}
		
		try {
			// Use batch update
			this.managedSource.batchUpdate(queries);
		}

		// Debug complete diff content
		catch (DataAccessException e) {
			LOGGER.error("Error on batched updated for diff content. Top message was {}.", e.getMessage());
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Content of diffline processed for this error :");
				lines.forEach(l -> LOGGER.debug("Dict[{}] : {} [{}] => {}", l.getDictionaryEntryUuid(), l.getAction(), l.getKeyValue(),
						l.getPayload()));
			}
			throw new TechnicalException("Error on batched updated for diff content. Check process model", e);
		}
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
	 * All specified links are used to check association between parameter tables during
	 * sorte. If a is
	 */
	private Comparator<DiffLine> sortedByLinks() {

		// Links by dict Entry uuid
		final Map<UUID, Set<UUID>> relationships = this.links.loadAllDictionaryEntryRelationashipFromLinks();

		// Order regarding the link between tables
		return (a, b) -> {

			// If exact similar, mark it
			if (a.getAction() == b.getAction() && a.getDictionaryEntryUuid().equals(b.getDictionaryEntryUuid())) {
				return 0;
			}

			// -1 = 1st < 2nd

			Set<UUID> relateds = relationships.get(a.getDictionaryEntryUuid());

			// Delete second
			if (a.getAction() == IndexAction.REMOVE) {

				// If both delete, check constraint
				if (b.getAction() == IndexAction.REMOVE) {

					if (relateds != null && relateds.contains(b.getDictionaryEntryUuid())) {
						return 1;
					}

					return -1;
				}

				return 1;
			}

			// Other first
			if (b.getAction() == IndexAction.REMOVE) {
				return -1;
			}

			// If b not remove, reverse relationship
			if (relateds != null && relateds.contains(b.getDictionaryEntryUuid())) {
				return 1;
			}

			return -1;
		};
	}
}
