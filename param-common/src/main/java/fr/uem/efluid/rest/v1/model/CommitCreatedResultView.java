package fr.uem.efluid.rest.v1.model;

import java.util.List;
import java.util.UUID;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class CommitCreatedResultView {

	private List<String> commitDomainNames;

	private UUID commitUuid;

	private long indexRowCount;

	/**
	 * 
	 */
	public CommitCreatedResultView() {
		super();
	}

	/**
	 * @return the commitDomainNames
	 */
	public List<String> getCommitDomainNames() {
		return this.commitDomainNames;
	}

	/**
	 * @param commitDomainNames
	 *            the commitDomainNames to set
	 */
	public void setCommitDomainNames(List<String> commitDomainNames) {
		this.commitDomainNames = commitDomainNames;
	}

	/**
	 * @return the commitUuid
	 */
	public UUID getCommitUuid() {
		return this.commitUuid;
	}

	/**
	 * @param commitUuid
	 *            the commitUuid to set
	 */
	public void setCommitUuid(UUID commitUuid) {
		this.commitUuid = commitUuid;
	}

	/**
	 * @return the indexRowCount
	 */
	public long getIndexRowCount() {
		return this.indexRowCount;
	}

	/**
	 * @param indexRowCount
	 *            the indexRowCount to set
	 */
	public void setIndexRowCount(long indexRowCount) {
		this.indexRowCount = indexRowCount;
	}

}
