package fr.uem.efluid.upgrades;

import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.IndexEntry;
import fr.uem.efluid.model.repositories.CommitRepository;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.services.UpgradeService;
import fr.uem.efluid.utils.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * With the addition of the "previous" data holder in index we need to init it for all
 * existing values
 *
 * @author elecomte
 * @version 1
 * @since v2.0.19
 */
@Component
public class InitPreviousPayloadUpgrade implements UpgradeProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeService.class);

    @Autowired
    private IndexRepository index;

    @Autowired
    private DictionaryRepository dictionary;

    @Autowired
    private CommitRepository commits;

    @Override
    public boolean repeat() {
        return false;
    }

    @Override
    public int index() {
        return 1;
    }

    @Override
    public String name() {
        return "previous-payload-init";
    }

    @Override
    public void runUpgrade() throws ApplicationException {

        List<Commit> allCommits = this.commits.findAll();
        AtomicInteger current = new AtomicInteger(1);
        allCommits.forEach(c -> {

            Map<UUID, List<IndexEntry>> entriesByTables = this.index.findWithUpgradablePreviosByCommitUuid(c.getUuid().toString()).stream()
                    .collect(Collectors.groupingBy(IndexEntry::getDictionaryEntryUuid));
            List<IndexEntry> toUpdate = new ArrayList<>();
            entriesByTables.forEach((t, i) -> {

                DictionaryEntry entry = this.dictionary.getOne(t);

                Map<String, IndexEntry> previouses = this.index.findAllPreviousIndexEntriesExcludingExisting(entry, i);

                i.forEach(e -> {
                    IndexEntry previous = previouses.get(e.getKeyValue());
                    if (previous != null) {
                        e.setPrevious(previous.getPayload());
                        toUpdate.add(e);
                    }
                });

            });

            this.index.saveAll(toUpdate);
            this.index.flush();

            LOGGER.info("[UPGRADE] Upgraded commit {}/{}", current.getAndIncrement(), allCommits.size());
        });
    }


}
