package fr.uem.efluid.services.types;

import fr.uem.efluid.model.entities.CommitState;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>
 * For combined multi-prepared commit
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.0.8
 */
public class WizzardCommitPreparationResult {

    private final long totalDiffSize;
    private final int totalTableCount;
    private final int totalProjectCount;
    private final int totalDomainsCount;
    private final PilotedCommitStatus status;
    private final CommitState state;

    /**
     * @param totalDiffSize
     * @param totalTableCount
     * @param totalProjectCount
     * @param totalDomainsCount
     */
    public WizzardCommitPreparationResult(long totalDiffSize, int totalTableCount, int totalProjectCount, int totalDomainsCount,
                                          PilotedCommitStatus status, CommitState state) {
        super();
        this.totalDiffSize = totalDiffSize;
        this.totalTableCount = totalTableCount;
        this.totalProjectCount = totalProjectCount;
        this.totalDomainsCount = totalDomainsCount;
        this.status = status;
        this.state = state;
    }

    /**
     * @return the totalDiffSize
     */
    public long getTotalDiffSize() {
        return this.totalDiffSize;
    }

    /**
     * @return the totalTableCount
     */
    public int getTotalTableCount() {
        return this.totalTableCount;
    }

    /**
     * @return the totalProjectCount
     */
    public int getTotalProjectCount() {
        return this.totalProjectCount;
    }

    /**
     * @return the totalDomainsCount
     */
    public int getTotalDomainsCount() {
        return this.totalDomainsCount;
    }

    /**
     * @return the status
     */
    public PilotedCommitStatus getStatus() {
        return this.status;
    }

    public CommitState getState() {
        return this.state;
    }

    /**
     * @param preps
     * @return
     */
    public static WizzardCommitPreparationResult fromPreparations(Collection<PilotedCommitPreparation<?>> preps) {

        final AtomicLong totalDiffSize = new AtomicLong(0);
        final AtomicInteger totalTableCount = new AtomicInteger(0);
        final AtomicInteger totalProjectCount = new AtomicInteger(0);
        final AtomicInteger totalDomainsCount = new AtomicInteger(0);
        final AtomicBoolean allCompleted = new AtomicBoolean(true);

        preps.forEach(p -> {
            totalDiffSize.addAndGet(p.getTotalCount());
            totalTableCount.addAndGet(p.getTotalTableCount());
            totalProjectCount.incrementAndGet();
            totalDomainsCount.addAndGet(p.getTotalDomainCount());

            if (p.getStatus() != PilotedCommitStatus.COMPLETED) {
                allCompleted.set(false);
            }
        });

        // For state based display in wizzard. Default is merge
        CommitState setState = !preps.isEmpty() ? preps.iterator().next().getPreparingState() : CommitState.MERGED;

        return new WizzardCommitPreparationResult(
                totalDiffSize.get(),
                totalTableCount.get(),
                totalProjectCount.get(),
                totalDomainsCount.get(),
                allCompleted.get() ? PilotedCommitStatus.COMPLETED : PilotedCommitStatus.DIFF_RUNNING,
                setState);
    }
}
