package fr.uem.efluid.services.types;

import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.tools.AsyncDriver;
import fr.uem.efluid.utils.ApplicationException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Global result for a compare between 2 commits
 *
 * @author elecomte
 * @version 1
 * @since v3.1.11
 */
public class CommitCompareResult extends DiffContentHolder<CommitCompareIndexEntry> implements AsyncDriver.AsyncSourceProcess {

    private final UUID identifier;
    private final LocalDateTime createdTime;
    private final List<CommitEditData> compareCommits;

    private final AtomicInteger processRemaining;

    private CommitCompareStatus status;

    private transient ApplicationException error;

    public CommitCompareStatus getStatus() {
        return this.status;
    }

    public void setStatus(CommitCompareStatus status) {
        this.status = status;
    }

    public CommitCompareResult(
            Collection<DictionaryEntrySummary> referencedTables,
            List<Commit> commits) {

        super(ConcurrentHashMap.newKeySet(), referencedTables.stream()
                .collect(Collectors.toMap(DictionaryEntrySummary::getUuid, d -> d)));

        this.processRemaining = new AtomicInteger(referencedTables.size());
        this.identifier = UUID.randomUUID();
        this.createdTime = LocalDateTime.now();
        this.compareCommits = commits.stream().map(CommitEditData::fromEntity).collect(Collectors.toList());

        this.status = CommitCompareStatus.NOT_LAUNCHED;
    }

    public AtomicInteger getProcessRemaining() {
        return this.processRemaining;
    }

    @Override
    public UUID getIdentifier() {
        return this.identifier;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public LocalDateTime getCreatedTime() {
        return this.createdTime;
    }

    @Override
    public int getAyncStepNbr() {
        return 5;
    }

    @Override
    public boolean hasSourceFailure() {
        return false;
    }

    @Override
    public int getPercentDone() {
        return 0;
    }

    @Override
    public <F> F fail(ApplicationException error) {
        this.error = error;
        throw error;
    }

    @Override
    public ApplicationException getSourceFailure() {
        return this.error;
    }

    public List<CommitEditData> getCompareCommits() {
        return this.compareCommits;
    }

    public static CommitCompareResult empty(List<Commit> commits) {
        CommitCompareResult result = new CommitCompareResult(Collections.emptyList(), commits);
        result.setStatus(CommitCompareStatus.COMPLETED);
        return result;
    }
}
