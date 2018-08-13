package fr.uem.efluid.model;

import java.time.LocalDateTime;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class ManagedModelIdentifier {

	private final String identity;

	private final LocalDateTime updatedTime;

	private final String details;

	/**
	 * @param identity
	 * @param updatedTime
	 * @param details
	 */
	public ManagedModelIdentifier(String identity, LocalDateTime updatedTime, String details) {
		super();
		this.identity = identity;
		this.updatedTime = updatedTime;
		this.details = details;
	}

	/**
	 * @return the identity
	 */
	public String getIdentity() {
		return this.identity;
	}

	/**
	 * @return the updatedTime
	 */
	public LocalDateTime getUpdatedTime() {
		return this.updatedTime;
	}

	/**
	 * @return the details
	 */
	public String getDetails() {
		return this.details;
	}
}
