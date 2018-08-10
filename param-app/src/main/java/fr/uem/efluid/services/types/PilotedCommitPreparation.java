package fr.uem.efluid.services.types;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.uem.efluid.model.entities.CommitState;
import fr.uem.efluid.utils.ApplicationException;

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
 * @author elecomte
 * @since v0.0.1
 * @version 1
 * @param <T>
 */
public final class PilotedCommitPreparation {

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

	private Map<String, byte[]> diffLobs;

	private UUID projectUuid;
	
	private List<DomainDiffDisplay<?>> domains;

	/**
	 * For pushed form only
	 */
	public PilotedCommitPreparation() {
		this.identifier = null;
		this.status = PilotedCommitStatus.COMMIT_CAN_PREPARE;
		this.preparingState = null;
		this.start = null;
	}

	/**
	 * 
	 */
	public PilotedCommitPreparation(CommitState preparingState) {
		this.identifier = UUID.randomUUID();
		this.status = PilotedCommitStatus.DIFF_RUNNING;
		this.start = LocalDateTime.now();
		this.preparingState = preparingState;
	}

	/**
	 * @return the processRemaining
	 */
	public AtomicInteger getProcessRemaining() {
		return this.processRemaining;
	}

	/**
	 * @param processRemaining
	 *            the processRemaining to set
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
	 * @param processStarted
	 *            the processStarted to set
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
	public <F> F fail(ApplicationException error) {
		this.errorDuringPreparation = error;
		setStatus(PilotedCommitStatus.FAILED);
		throw error;
	}

	/**
	 * @return the status
	 */
	public PilotedCommitStatus getStatus() {
		return this.status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(PilotedCommitStatus status) {
		this.status = status;
	}

	/**
	 * @return the identifier
	 */
	public UUID getIdentifier() {
		return this.identifier;
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
	 * @param errorKey
	 *            the errorKey to set
	 */
	public void setErrorKey(String errorKey) {
		this.errorKey = errorKey;
	}

	/**
	 * @return the preparedContent
	 */
	public List<T> getPreparedContent() {
		return this.preparedContent;
	}

	/**
	 * @param preparedContent
	 *            the preparedContent to set
	 */
	public void setPreparedContent(List<T> preparedContent) {
		this.preparedContent = preparedContent;
	}

	/**
	 * @return the commitData
	 */
	public CommitEditData getCommitData() {
		return this.commitData;
	}

	/**
	 * @param commitData
	 *            the commitData to set
	 */
	public void setCommitData(CommitEditData commitData) {
		this.commitData = commitData;
	}

	/**
	 * @param end
	 *            the end to set
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
	 * Quick access to covered Functional domains in preparation (used for commit detail
	 * page)
	 * 
	 * @return
	 */
	public Collection<String> getSelectedFunctionalDomainNames() {
		return this.preparedContent != null ? this.preparedContent.stream()
				.filter(d -> d.getDiff().stream().anyMatch(PreparedIndexEntry::isSelected))
				.map(DiffDisplay::getDomainName)
				.collect(Collectors.toSet()) : Collections.emptyList();
	}

	/**
	 * @return
	 */
	public boolean isEmptyDiff() {
		return this.preparedContent.stream().allMatch(d -> d.getDiff().isEmpty());
	}

	/**
	 * @return
	 */
	public long getTotalCount() {
		return this.preparedContent != null
				? this.preparedContent.stream().flatMap(d -> d.getDiff() != null ? d.getDiff().stream() : Stream.of()).count()
				: 0;
	}

	/**
	 * @return the diffLobs
	 */
	public Map<String, byte[]> getDiffLobs() {
		return this.diffLobs;
	}

	/**
	 * @param diffLobs
	 *            the diffLobs to set
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
	 * @param projectUuid
	 *            the projectUuid to set
	 */
	public void setProjectUuid(UUID projectUuid) {
		this.projectUuid = projectUuid;
	}

	/**
	 * @return
	 */
	public boolean isHasSomeDiffRemarks() {

		for (DiffDisplay<?> diff : this.preparedContent) {
			if (diff.isHasRemarks()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @return
	 */
	public List<DiffRemark<?>> getAllDiffRemarks() {

		return this.preparedContent.stream()
				.filter(d -> d.getRemarks() != null)
				.flatMap(d -> d.getRemarks().stream())
				.collect(Collectors.toList());
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