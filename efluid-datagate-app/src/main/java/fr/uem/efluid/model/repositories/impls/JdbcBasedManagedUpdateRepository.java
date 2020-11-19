package fr.uem.efluid.model.repositories.impls;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.entities.TableLink;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.FeatureManager;
import fr.uem.efluid.model.repositories.ManagedUpdateRepository;
import fr.uem.efluid.model.repositories.TableLinkRepository;
import fr.uem.efluid.services.Feature;
import fr.uem.efluid.tools.ManagedQueriesGenerator;
import fr.uem.efluid.tools.ManagedValueConverter;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.DatasourceUtils;
import fr.uem.efluid.utils.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static fr.uem.efluid.utils.ErrorType.*;

/**
 * <p>
 * Very basic implements. Statement not optimized
 * </p>
 * <p>
 * Process the priority checking depends between tables
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@Repository
public class JdbcBasedManagedUpdateRepository implements ManagedUpdateRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcBasedManagedUpdateRepository.class);
    private static final Logger QUERRY_LOGGER = LoggerFactory.getLogger("update.queries");

    private static final Charset ERROR_OUT_CHARSET = Charset.forName("utf8");
    @Autowired
    private JdbcTemplate managedSource;

    // For MANAGED DB transaction. Do not follow "implicit" core DB trx
    @Autowired
    @Qualifier(DatasourceUtils.MANAGED_TRANSACTION_MANAGER)
    private PlatformTransactionManager managedDbTransactionManager;

    @Autowired
    private ManagedQueriesGenerator queryGenerator;

    @Autowired
    private ManagedValueConverter payloadConverter;

    @Autowired
    private DictionaryRepository dictionary;

    @Autowired
    private TableLinkRepository links;

    @Autowired
    private FeatureManager features;

    @Value("${datagate-efluid.managed-updates.output-failed-query-set}")
    private boolean outputFailedQuerySet;

    @Value("${datagate-efluid.managed-updates.output-failed-query-set-file}")
    private String outputFile;

    /**
     * @param lines
     * @param allLobs
     * @param project
     * @return
     */
    @Override
    public String[] runAllChangesAndCommit(List<? extends DiffLine> lines, Map<String, byte[]> allLobs, Project project) {

        LOGGER.debug("Identified change to apply on managed DB. Will process {} diffLines", lines.size());

        // Preload dictionary for direct access by uuid and tab name
        Map<UUID, DictionaryEntry> dictEntries = this.dictionary.findAllByProjectMappedToUuid(project);
        Map<String, DictionaryEntry> dictByTab = dictEntries.values().stream()
                .collect(Collectors.toMap(DictionaryEntry::getTableName, d -> d));

        // Preload all table links for mapped queries
        Map<UUID, List<TableLink>> mappedLinks = this.links.findAllMappedByDictionaryEntryUUID();

        // Manually perform Managed DB transaction for fine rollback management
        DefaultTransactionDefinition paramTransactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus status = this.managedDbTransactionManager.getTransaction(paramTransactionDefinition);
        List<Object[]> lobArgs = new ArrayList<>();

        String currentQuery = null;
        String[] queries = null;

        try {
            checkUpdatesAndDeleteMissingIds(
                    lines,
                    dictEntries,
                    this.features.isEnabled(Feature.CHECK_MISSING_IDS_AT_MANAGED_DELETE),
                    this.features.isEnabled(Feature.CHECK_MISSING_IDS_AT_MANAGED_UPDATE));

            // Prepare all queries, ordered by dictionary entry and action regarding links
            queries = lines.stream()
                    .sorted(sortedByLinks())
                    .map(e -> queryFor(dictEntries.get(e.getDictionaryEntryUuid()), e, allLobs, mappedLinks, dictByTab, lobArgs))
                    .toArray(String[]::new);

            // Debug all content
            if (LOGGER.isDebugEnabled()) {
                // Reproduce content to debug - heavy loading !!!
                LOGGER.debug("Queries Before sort : \n{}",
                        lines.stream().map(e -> queryFor(dictEntries.get(e.getDictionaryEntryUuid()), e, allLobs, mappedLinks, dictByTab,
                                new ArrayList<>()))
                                .collect(Collectors.joining("\n")));
                LOGGER.debug("Queries After sort : \n{}", lines.stream().sorted(sortedByLinks())
                        .map(e -> queryFor(dictEntries.get(e.getDictionaryEntryUuid()), e, allLobs, mappedLinks, dictByTab,
                                new ArrayList<>()))
                        .collect(Collectors.joining("\n")));
            }

            // Use one-by-one update
            for (int i = 0; i < queries.length; i++) {
                Object[] args = lobArgs.get(i);
                currentQuery = queries[i];
                if (args.length > 0) {
                    this.managedSource.update(currentQuery, args);
                } else {
                    this.managedSource.update(currentQuery);
                }

                // Log from file spec
                if (QUERRY_LOGGER.isDebugEnabled()) {
                    QUERRY_LOGGER.debug(currentQuery);
                }
            }

            // Commit immediately the update if successful
            this.managedDbTransactionManager.commit(status);

            // For history saving
            return queries;
        }

        // Debug complete diff content
        catch (DataAccessException e) {
            this.managedDbTransactionManager.rollback(status);

            String details = getDataAccessExceptionDetails(currentQuery, e);
            LOGGER.error("Error on batched updated for diff content. Top message was \"{}\", will share details \"{}\".",
                    e.getMessage(), details);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Content of diffline processed for this error :");
                lines.forEach(l -> LOGGER.debug("Dict[{}] : {} [{}] => {}", l.getDictionaryEntryUuid(), l.getAction(), l.getKeyValue(),
                        l.getPayload()));
            }

            // Output query set if required
            if (this.outputFailedQuerySet) {
                outputErrorQuerySet(details, currentQuery, queries);
            }

            throw new ApplicationException(APPLY_FAILED, "Error on batched updated for diff content. Check process model", e, details);
        }
    }

    private static String getDataAccessExceptionDetails(String currentQuery, DataAccessException e) {
        return currentQuery + " ==> " + e.getCause().getMessage();
    }

    /**
     * <p>Prepare the query from a line. Produces the SQL code + select the correct lobs to inject as preparedStatment
     * Args during the query launch. Other "simple" values are directly combined in the query using "protect" rules
     * from {@link ManagedQueriesGenerator}
     * </p>
     * <p>The most complexe part here is the lob processing. The lobs are extracted by their hash (used in diff line
     * payload) from the map of "all lobs" <code>allLobs</code> and added to specified <code>lobArgs</code>. We have to
     * manage differently the BLOBs and the CLOBS :
     * <ul><li>BLOBs are managed in args as simple byte array and used "as this" in prepared statement arg inject</li>
     * <li>CLOBs are converted to strings and used as string in ps arg inject</li></ul>
     * </p>
     * <p>This method is built with performance in mind, but still could be optimized</p>
     *
     * @param entry
     * @param line
     * @return
     */
    private String queryFor(
            DictionaryEntry entry,
            DiffLine line,
            Map<String, byte[]> allLobs,
            Map<UUID, List<TableLink>> mappedLinks,
            Map<String, DictionaryEntry> dictByTab,
            List<Object[]> lobArgs) {

        final List<String> refLobKeys = new ArrayList<>();

        // Values may be needed in updates for ar
        List<fr.uem.efluid.services.types.Value> values = null;
        String q;

        switch (line.getAction()) {
            case ADD:
                values = this.payloadConverter.expandInternalValue(line.getPayload());
                q = this.queryGenerator.producesApplyAddQuery(entry, line.getKeyValue(), values, refLobKeys, mappedLinks.get(entry.getUuid()), dictByTab);
                break;
            case REMOVE:
                q = this.queryGenerator.producesApplyRemoveQuery(entry, line.getKeyValue());
                break;
            case UPDATE:
            default:
                values = this.payloadConverter.expandInternalValue(line.getPayload());
                q = this.queryGenerator.producesApplyUpdateQuery(entry, line.getKeyValue(), values, refLobKeys, mappedLinks.get(entry.getUuid()), dictByTab);
        }

        // If lobs were referenced, prepare params
        if (refLobKeys.size() > 0) {
            final Set<String> clobKeys = new HashSet<>();

            // Detect the lobs hash wich are CLOBs (from values details)
            if (values != null) {
                values.stream().filter(v -> v.getType() == ColumnType.TEXT).forEach(v -> clobKeys.add(v.getValueAsString()));
            }

            lobArgs.add(refLobKeys.stream().map(l -> {
                // If identified as a CLOB, convert to string for PS arg inject
                if (clobKeys.contains(l)) {
                    LOGGER.debug("Process LOB {} as a CLOB text", l);
                    return FormatUtils.toString(allLobs.get(l));
                }
                // Else we use directly the byte[]
                return allLobs.get(l);
            }).toArray());
        }

        // Else empty param
        else {
            lobArgs.add(new Object[]{});
        }

        return q;
    }

    /**
     * @param line
     * @param checkDeleteFeature
     * @param checkUpdateFeature
     * @return
     */
    private static boolean isCheckingRequired(DiffLine line, boolean checkDeleteFeature, boolean checkUpdateFeature) {
        return ((line.getAction() == IndexAction.REMOVE && checkDeleteFeature)
                || (line.getAction() == IndexAction.UPDATE && checkUpdateFeature));
    }

    /**
     * <p>
     * Not optimized AT ALL ... But will create one select for each update / delete, and
     * control that a result is provided. Process each diff one by one, as they can
     * concern many, many different tables, situations ...
     * </p>
     * <p>
     * Enabled only if one of <code>checkDeleteMissingIds</code> or
     * <code>checkUpdateMissingIds</code> is true.
     * </p>
     *
     * @param lines
     * @param dictEntries
     */
    private void checkUpdatesAndDeleteMissingIds(List<? extends DiffLine> lines, Map<UUID, DictionaryEntry> dictEntries,
                                                 final boolean checkDeleteFeature, final boolean checkUpdateFeature) {

        if (checkDeleteFeature || checkUpdateFeature) {
            LOGGER.debug("Check on updates or delete missing ids is enabled : transform as select queries all concerned changes");
            lines.stream()
                    .filter(l -> isCheckingRequired(l, checkDeleteFeature, checkUpdateFeature))
                    .map(e -> this.queryGenerator.producesGetOneQuery(dictEntries.get(e.getDictionaryEntryUuid()), e.getKeyValue()))
                    .forEach(s -> this.managedSource.query(s, rs -> {
                        if (!rs.next()) {
                            throw new ApplicationException(VERIFIED_APPLY_NOT_FOUND, "Item not found. Checking query was " + s, s);
                        }
                        return null;
                    }));
        } else {
            LOGGER.debug("Do not check updates and delete missing ids");
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
                        return -1;
                    }

                    return 1;
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

    /**
     * <p>
     * When required, can ouptut the full processed query set when an error occurs. Write
     * it into a file
     * </p>
     *
     * @param errorDetails
     * @param currentQuery
     * @param queries
     */
    private void outputErrorQuerySet(String errorDetails, String currentQuery, String[] queries) {

        Path output = new File(this.outputFile).toPath();

        try {
            if (!output.toFile().exists()) {
                Files.createFile(output);
            }

            StringBuilder content = new StringBuilder();

            content
                    .append("######################################################################################################\n")
                    .append("Fatal error on update at ").append(FormatUtils.format(LocalDateTime.now())).append("\n")
                    .append("######################################################################################################\n")
                    .append(errorDetails)
                    .append("\n######################################################################################################\n")
                    .append("All ordered queries for this update : \n");

            for (String query : queries) {
                if (query.equals(currentQuery)) {
                    content.append("==> ");
                }
                content.append("    ").append(query).append("\n");
            }

            content.append("\n\n");

            Files.write(output, content.toString().getBytes(ERROR_OUT_CHARSET), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new ApplicationException(OUTPUT_ERROR, "Cannot append to file " + output, e);
        }
    }
}
