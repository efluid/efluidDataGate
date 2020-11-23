package fr.uem.efluid.services.types;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import fr.uem.efluid.model.entities.ApplyHistoryEntry;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class SearchHistoryPage {

	private final int pageIndex;

	private final int pageCount;

	private final long totalCount;

	private final List<HistoryDetails> page;

	private final String search;

	/**
	 * @param pageIndex
	 * @param pageCount
	 * @param page
	 */
	private SearchHistoryPage(int pageIndex, int pageCount, long totalCount, List<HistoryDetails> page, String search) {
		super();
		this.pageIndex = pageIndex;
		this.pageCount = pageCount;
		this.totalCount = totalCount;
		this.page = page;
		this.search = search;
	}

	/**
	 * @return the pageIndex
	 */
	public int getPageIndex() {
		return this.pageIndex;
	}

	/**
	 * @return the pageCount
	 */
	public int getPageCount() {
		return this.pageCount;
	}

	/**
	 * @return the totalCount
	 */
	public long getTotalCount() {
		return this.totalCount;
	}

	/**
	 * @return the page
	 */
	public List<HistoryDetails> getPage() {
		return this.page;
	}

	/**
	 * @return the search
	 */
	public String getSearch() {
		return this.search;
	}

	/**
	 * @param index
	 * @param page
	 * @return
	 */
	public static SearchHistoryPage fromPage(int index, String search, Page<ApplyHistoryEntry> page) {
		List<HistoryDetails> content = page.getContent().stream().map(HistoryDetails::fromEntity).collect(Collectors.toList());
		return new SearchHistoryPage(index, page.getTotalPages(), page.getTotalElements(), content, search);
	}

	/**
	 * @author elecomte
	 * @since v0.0.8
	 * @version 1
	 */
	public static class HistoryDetails {
		private final String query;
		private final String user;
		private final boolean rollback;
		private final UUID attachmentSourceUuid;
		private final LocalDateTime processedTime;
		private final UUID projectId;
		private final String commitComment;

		/**
		 * @param query
		 * @param user
		 * @param rollback
		 * @param processedTime
		 */
		private HistoryDetails(String query, String user, boolean rollback, UUID attachmentSourceUuid, LocalDateTime processedTime, UUID projectId, String commitComment) {
			super();
			this.query = query;
			this.user = user;
			this.rollback = rollback;
			this.attachmentSourceUuid = attachmentSourceUuid;
			this.processedTime = processedTime;
			this.projectId = projectId;
			this.commitComment = commitComment;
		}

		/**
		 * @return the query
		 */
		public String getQuery() {
			return this.query;
		}

		/**
		 * @return the user
		 */
		public String getUser() {
			return this.user;
		}

		/**
		 * @return the rollback
		 */
		public boolean isRollback() {
			return this.rollback;
		}

		/**
		 * @return the attachmentSourceUuid
		 */
		public UUID getAttachmentSourceUuid() {
			return this.attachmentSourceUuid;
		}

		/**
		 * @return the processedTime
		 */
		public LocalDateTime getProcessedTime() {
			return this.processedTime;
		}

		/**
		 * @return the projectId
		 */
		public UUID getProjectId () {
			return this.projectId;
		}

		/**
		 * @return the commitComment
		 */
		public String getCommitComment() {
			return this.commitComment;
		}

		static HistoryDetails fromEntity(ApplyHistoryEntry histo) {

			Timestamp ts = new Timestamp(histo.getTimestamp().longValue());
			return new HistoryDetails(histo.getQuery(), histo.getUser().getEmail(), histo.isRollback(), histo.getAttachmentSourceUuid(),
					ts.toLocalDateTime(), histo.getProjectUuid(), histo.getCommit() != null ? histo.getCommit().getComment() : "");
		}
	}
}
