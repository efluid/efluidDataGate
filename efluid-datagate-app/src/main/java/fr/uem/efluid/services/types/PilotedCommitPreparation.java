package fr.uem.efluid.services.types;

import fr.uem.efluid.model.entities.CommitState;
import fr.uem.efluid.tools.AsyncDriver;
import fr.uem.efluid.utils.ApplicationException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * A <tt>PilotedCommitPreparation</tt> is a major load event associated to a preparation
 * of index or index related data. Their is only ONE preparation of any kind which is
 * available in the application, due to memory use and data extraction heavy load. But
 * this preparation can be of various type.
 * </p>
 * <p>
 * Common rules for a preparation :
 * <ul>
 * <li>Used to prepare a commit of a fixed {@link CommitState}</li>
 * <li>Identified by uuid, but not exported. (currently not realy used)</li>
 * <li>Identified with start and end time of preparation</li>
 * <li>Associated to an evolving status : defines how far we are in the preparation. Can
 * evolve to include a full "% remaining" process</li>
 * <li>Holds a content, the "result" of the preparation. Supposed to be related to
 * <tt>DiffLine</tt> (but type is free in this vearsion)</li>
 * <li>Associated to a commit definition which will embbed the result of the preparation
 * once completed and validated.</li>
 * </ul>
 * </p>
 *
 * @param <T>
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public final class PilotedCommitPreparation<T extends DiffDisplay<?>> implements AsyncDriver.AsyncSourceProcess {

    private final UUID identifier;

    private final LocalDateTime start;

    private final CommitState preparingState;

    private LocalDateTime end;

    private PilotedCommitStatus status;

    private String errorKey;

    private ApplicationException errorDuringPreparation;

    private CommitEditData commitData;

    private AtomicInteger processRemaining;

    private int processStarted;

    // For percentDone step counts
    private AtomicInteger totalStepCount = new AtomicInteger(0);

    private Map<String, byte[]> diffLobs;

    private UUID projectUuid;

    private List<DomainDiffDisplay<T>> domains;

    // For attachments
    private boolean attachmentDisplaySupport;
    private boolean attachmentExecuteSupport;

    // For simplification of rendering
    private boolean displayAll = true;


    /**
     *
     */
    public PilotedCommitPreparation(CommitState preparingState) {
        this.identifier = UUID.randomUUID();
        this.status = PilotedCommitStatus.DIFF_RUNNING;
        this.start = LocalDateTime.now();
        this.preparingState = preparingState;
    }

    public void incrementProcessStep() {
        this.totalStepCount.incrementAndGet();
    }

    /**
     * @return the attachmentDisplaySupport
     */
    public boolean isAttachmentDisplaySupport() {
        return this.attachmentDisplaySupport;
    }

    /**
     * @param attachmentDisplaySupport the attachmentDisplaySupport to set
     */
    public void setAttachmentDisplaySupport(boolean attachmentDisplaySupport) {
        this.attachmentDisplaySupport = attachmentDisplaySupport;
    }

    /**
     * @return the attachmentExecuteSupport
     */
    public boolean isAttachmentExecuteSupport() {
        return this.attachmentExecuteSupport;
    }

    /**
     * @param attachmentExecuteSupport the attachmentExecuteSupport to set
     */
    public void setAttachmentExecuteSupport(boolean attachmentExecuteSupport) {
        this.attachmentExecuteSupport = attachmentExecuteSupport;
    }

    /**
     * @return the processRemaining
     */
    public AtomicInteger getProcessRemaining() {
        return this.processRemaining;
    }

    /**
     * @param processRemaining the processRemaining to set
     */
    public void setProcessRemaining(AtomicInteger processRemaining) {
        this.processRemaining = processRemaining;
    }

    /**
     * @return the processStarted
     */
    public int getProcessStarted() {
        return this.processStarted;
    }

    /**
     * @param processStarted the processStarted to set
     */
    public void setProcessStarted(int processStarted) {
        this.processStarted = processStarted;
    }

    /**
     * @return the errorDuringPreparation
     */
    public ApplicationException getErrorDuringPreparation() {
        return this.errorDuringPreparation;
    }

    /**
     * @param error
     */
    @Override
    public <F> F fail(ApplicationException error) {
        this.errorDuringPreparation = error;
        setStatus(PilotedCommitStatus.FAILED);
        throw error;
    }

    /**
     * @return
     * @see AsyncDriver.AsyncSourceProcess#hasSourceFailure()
     */
    @Override
    public boolean hasSourceFailure() {
        return getErrorDuringPreparation() != null;
    }

    /**
     * @return
     * @see AsyncDriver.AsyncSourceProcess#getSourceFailure()
     */
    @Override
    public ApplicationException getSourceFailure() {
        return getErrorDuringPreparation();
    }

    /**
     * @return the status
     */
    public PilotedCommitStatus getStatus() {
        return this.status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(PilotedCommitStatus status) {
        this.status = status;
    }

    /**
     * @return the identifier
     */
    @Override
    public UUID getIdentifier() {
        return this.identifier;
    }

    @Override
    public String getDescription() {
        return "Preparation for state " + this.preparingState + " created " + this.start + " - current status \"" + this.status + "\"";
    }

    @Override
    public LocalDateTime getCreatedTime() {
        return this.start;
    }

    @Override
    public int getAyncStepNbr() {
        // Use remaining process steps
        return getProcessRemaining().get();
    }

    /**
     * @return the start
     */
    public LocalDateTime getStart() {
        return this.start;
    }

    /**
     * @return the end
     */
    public LocalDateTime getEnd() {
        return this.end;
    }

    /**
     * @return the errorKey
     */
    public String getErrorKey() {
        return this.errorKey;
    }

    /**
     * @param errorKey the errorKey to set
     */
    public void setErrorKey(String errorKey) {
        this.errorKey = errorKey;
    }

    /**
     * @return the commitData
     */
    public CommitEditData getCommitData() {
        return this.commitData;
    }

    /**
     * @param commitData the commitData to set
     */
    public void setCommitData(CommitEditData commitData) {
        this.commitData = commitData;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    /**
     * @return the preparingState
     */
    public CommitState getPreparingState() {
        return this.preparingState;
    }

    /**
     * @return the domains
     */
    public List<DomainDiffDisplay<T>> getDomains() {
        return this.domains;
    }

    /**
     *
     */
    public void resetDiffDisplayContent() {
        this.domains.forEach(d -> d.setPreparedContent(null));
    }

    /**
     * <p>
     * Applies the diff to corresponding domain display, and provides the top level domain
     * DiffDisplay with a diff
     * </p>
     * <p>
     * Affect also an index for each diff line for the whole preparation (for quick access
     * / reference)
     * </p>
     *
     * @param fullDiff
     */
    public void applyDiffDisplayContent(Collection<T> fullDiff) {

        Map<UUID, DomainDiffDisplay<T>> domainsByUuid = this.domains.stream()
                .collect(Collectors.toMap(DomainDiffDisplay::getDomainUuid, d -> d));

        AtomicLong indexInPreparation = new AtomicLong(0);

        fullDiff.forEach(d -> {
            DomainDiffDisplay<T> domain = domainsByUuid.get(d.getDomainUuid());

            // Init domain diff content holder
            if (domain.getPreparedContent() == null) {
                domain.setPreparedContent(new ArrayList<>());
            }

            domain.getPreparedContent().add(d);

            // Apply index for the item
            d.getDiff().forEach(l -> l.setIndexForDiff(indexInPreparation.getAndIncrement()));
        });
    }

    /**
     * @param domains the domains to set
     */
    public void setDomains(List<DomainDiffDisplay<T>> domains) {
        this.domains = domains;
    }

    /**
     * Quick access to covered Functional domains in preparation (used for commit detail
     * page)
     *
     * @return
     */
    public Collection<String> getSelectedFunctionalDomainNames() {
        return this.domains != null && this.domains.size() > 0
                ? this.domains.stream()
                .filter(DomainDiffDisplay::isHasSelectedItems)
                .map(DomainDiffDisplay::getDomainName)
                .collect(Collectors.toSet())
                : Collections.emptyList();
    }

    /**
     * @return
     */
    public long getTotalCount() {
        return this.domains != null && this.domains.size() > 0
                ? this.domains.stream().mapToLong(DomainDiffDisplay::getTotalCount).sum()
                : 0;
    }

    /**
     * @return
     */
    public int getTotalTableCount() {
        return this.domains != null && this.domains.size() > 0
                ? this.domains.stream().filter(d -> d.getPreparedContent() != null)
                .mapToInt(d -> d.getPreparedContent().size()).sum()
                : 0;
    }

    /**
     * @return
     */
    public int getTotalDomainCount() {
        return this.domains != null ? this.domains.size() : 0;
    }

    /**
     * @return the diffLobs
     */
    public Map<String, byte[]> getDiffLobs() {
        return this.diffLobs;
    }

    /**
     * @param diffLobs the diffLobs to set
     */
    public void setDiffLobs(Map<String, byte[]> diffLobs) {
        this.diffLobs = diffLobs;
    }

    /**
     * @return the projectUuid
     */
    public UUID getProjectUuid() {
        return this.projectUuid;
    }

    /**
     * @param projectUuid the projectUuid to set
     */
    public void setProjectUuid(UUID projectUuid) {
        this.projectUuid = projectUuid;
    }

    /**
     * <p>
     * For easy control on diff content regarding domain + DiffDisplay tree content
     * </p>
     *
     * @param pred
     * @return
     */
    public boolean isAnyDiffValidate(Predicate<T> pred) {
        return (this.domains != null && this.domains.size() > 0)
                && this.domains.stream()
                .filter(d -> d.getPreparedContent() != null && d.getPreparedContent().size() > 0)
                .map(DomainDiffDisplay::getPreparedContent)
                .flatMap(Collection::stream)
                .anyMatch(pred);
    }

    /**
     * @return
     */
    public boolean isHasSomeDiffRemarks() {
        return isAnyDiffValidate(DiffDisplay::isHasRemarks);
    }

    /**
     * @return
     */
    public boolean isEmptyDiff() {
        return !isAnyDiffValidate(DiffDisplay::isHasContent);
    }

    /**
     * @return
     */
    public boolean isHasDiffDisplay() {
        return (this.domains != null && this.domains.size() > 0)
                && this.domains.stream().filter(d -> d.getPreparedContent() != null)
                .anyMatch(d -> d.getPreparedContent().size() > 0);
    }

    /**
     * @return
     */
    public Stream<T> streamDiffDisplay() {
        return this.domains.stream()
                .map(DomainDiffDisplay::getPreparedContent)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream);
    }

    /**
     * @return
     */
    public List<DiffRemark<?>> getAllDiffRemarks() {
        return this.domains != null && this.domains.size() > 0
                ? this.domains.stream()
                .map(DomainDiffDisplay::getAllDiffRemarks)
                .flatMap(Collection::stream)
                .collect(Collectors.toList())
                : Collections.emptyList();
    }

    public boolean isDisplayAll() {
        return this.displayAll;
    }

    public void setDisplayAll(boolean displayAll) {
        this.displayAll = displayAll;
    }

    /**
     * Calculated percent of process on current preparation
     *
     * @return
     */
    @Override
    public int getPercentDone() {

        if (getProcessStarted() == 0) {
            return 0;
        }

        return (this.totalStepCount.get() * 100) / (getPreparingState().getProcessingSteps() * getProcessStarted());
    }

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Preparation[" + this.identifier + "|" + this.status + "]";
    }
}