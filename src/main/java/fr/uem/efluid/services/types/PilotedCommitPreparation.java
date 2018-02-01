package fr.uem.efluid.services.types;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import fr.uem.efluid.model.entities.IndexEntry;

/**
 * <p>
 * Commit preparation details, sharing various info on running diff and providing
 * identifier for commit preparation
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public final class PilotedCommitPreparation {

	private final UUID identifier;

	private final LocalDateTime start;

	private LocalDateTime end;

	private PilotedCommitStatus status;

	private Throwable errorDuringPreparation;

	private Map<UUID, Collection<IndexEntry>> preparedDiff;

	/**
	 * 
	 */
	public PilotedCommitPreparation() {
		this.identifier = UUID.randomUUID();
		this.status = PilotedCommitStatus.DIFF_RUNNING;
		this.start = LocalDateTime.now();
	}

	/**
	 * @return the errorDuringPreparation
	 */
	public Throwable getErrorDuringPreparation() {
		return this.errorDuringPreparation;
	}

	/**
	 * @param errorDuringPreparation
	 *            the errorDuringPreparation to set
	 */
	public void setErrorDuringPreparation(Throwable errorDuringPreparation) {
		this.errorDuringPreparation = errorDuringPreparation;
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
	 * @return the preparedDiff
	 */
	public Map<UUID, Collection<IndexEntry>> getPreparedDiff() {
		return this.preparedDiff;
	}

	/**
	 * @param preparedDiff
	 *            the preparedDiff to set
	 */
	public void setPreparedDiff(Map<UUID, Collection<IndexEntry>> preparedDiff) {
		this.preparedDiff = preparedDiff;
	}

	/**
	 * @param end
	 *            the end to set
	 */
	public void setEnd(LocalDateTime end) {
		this.end = end;
	}

}