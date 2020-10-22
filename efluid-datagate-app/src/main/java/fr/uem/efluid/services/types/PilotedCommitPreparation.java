package fr.uem.efluid.services.types;

import fr.uem.efluid.model.entities.CommitState;
import fr.uem.efluid.tools.AsyncDriver;
import fr.uem.efluid.tools.TransformerProcessor;
import fr.uem.efluid.utils.ApplicationException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * A <tt>PilotedCommitPreparation</tt> is a major load event associated to a preparation
 * of index or index related data. Their is only ONE preparation of any kind which is
 * available for an active instance, due to memory use and data extraction heavy load. But
 * this preparation can be of various type.
 * </p>
 * <p>
 * Common rules for a preparation :
 * <ul>
 * <li>Used to prepare a commit of a fixed {@link CommitState}</li>
 * <li>Identified by uuid, but not exported.</li>
 *  * <li>Associated to an evolving status : defines how far we are in the preparation. Can
 * evolve to include a full "% remaining" process</li>
 * <li>Holds a content, the "result" of the preparation. Supposed to be related to
 * <tt>DiffLine</tt> (but type is free in this vearsion)</li>
 * <li>Associated to a commit definition which will embbed the result of the preparation
 * once completed and validated.</li>
 * <li>Associated to referenced tables</li>
 * <li>Can include some remarks on incorrect values identified during diff build</li>
 * </ul>
 * </p>
 * <p>All diff related contents are thread safe (and are updated in asynchronous diff processes)</p>
 * <p>No content are sorted or filtered. Everything is stored in memory. The display of filtered / sorted values is processed in higher level rendering</p>
 *
 * @param <T>
 * @author elecomte
 * @version 2
 * @since v0.0.1
 */
public final class PilotedCommitPreparation<T extends PreparedIndexEntry> extends DiffContentHolder<T> implements AsyncDriver.AsyncSourceProcess {

    private final UUID identifier;

    private final LocalDateTime start;

    private final CommitState preparingState;

    private PilotedCommitStatus status;

    private ApplicationException errorDuringPreparation;

    private CommitEditData commitData;

    private String sourceFilename;

    private AtomicInteger processRemaining;

    private int processStarted;

    // For percentDone step counts
    private final AtomicInteger totalStepCount = new AtomicInteger(0);

    private UUID projectUuid;

    /* Asynchronously completed content of preparation : diff, associated lobs / remarks, and details on referenced tables */
    private final Map<String, byte[]> diffLobs = new ConcurrentHashMap<>();
    private final Collection<DiffRemark<?>> diffRemarks = ConcurrentHashMap.newKeySet();

    /* For attachments */
    private boolean attachmentDisplaySupport;
    private boolean attachmentExecuteSupport;


    private TransformerProcessor transformerProcessor;

    /**
     *
     */
    public PilotedCommitPreparation(CommitState preparingState) {

        // Holder with asynchronous completion of content
        super(ConcurrentHashMap.newKeySet(), new ConcurrentHashMap<>());

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
     * @return the preparingState
     */
    public CommitState getPreparingState() {
        return this.preparingState;
    }

    /**
     * @return the diffLobs
     */
    public Map<String, byte[]> getDiffLobs() {
        return this.diffLobs;
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

    public String getSourceFilename() {
        return sourceFilename;
    }

    public void setSourceFilename(String sourceFilename) {
        this.sourceFilename = sourceFilename;
    }

    /**
     * @return
     */
    public boolean isHasSomeDiffRemarks() {
        return this.diffRemarks.size() > 0;
    }


    public TransformerProcessor getTransformerProcessor() {
        return transformerProcessor;
    }

    public void setTransformerProcessor(TransformerProcessor transformerProcessor) {
        this.transformerProcessor = transformerProcessor;
    }

    /**
     * Entry point to diff remarks holder. Referenced collection is thread safe and can be updated / accessed without locking
     *
     * @return holder accessor
     */
    public Collection<DiffRemark<?>> getDiffRemarks() {
        return this.diffRemarks;
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