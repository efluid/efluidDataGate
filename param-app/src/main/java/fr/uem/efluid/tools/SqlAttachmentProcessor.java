package fr.uem.efluid.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import fr.uem.efluid.model.entities.ApplyHistoryEntry;
import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.ApplyHistoryEntryRepository;
import fr.uem.efluid.utils.FormatUtils;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class SqlAttachmentProcessor extends AttachmentProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(SqlAttachmentProcessor.class);

	private JdbcTemplate managedSource;

	private ApplyHistoryEntryRepository history;

	/**
	 * @param managedSource
	 * @param history
	 */
	public SqlAttachmentProcessor(JdbcTemplate managedSource, ApplyHistoryEntryRepository history) {
		super();
		this.managedSource = managedSource;
		this.history = history;
	}

	/**
	 * @param att
	 * @see fr.uem.efluid.tools.AttachmentProcessor#execute(fr.uem.efluid.tools.AttachmentProcessor.Compliant)
	 */
	@Override
	public void execute(User user, Compliant att) {

		LOGGER.debug("[ATTACH-SQL] Will execute a new SQL update from attachment with user {}, from attachment {}",
				user.getLogin(), att.getUuid());

		// Basic raw edit
		String sql = FormatUtils.toString(att.getData());

		// Run query "as this"
		this.managedSource.execute(sql);

		LOGGER.debug("[ATTACH-SQL] Store an history entry for SQL update from attachment {}", att.getUuid());

		// Will store as a specific attachment exec in history
		ApplyHistoryEntry entry = new ApplyHistoryEntry();
		entry.setRollback(false);
		entry.setTimestamp(Long.valueOf(System.currentTimeMillis()));
		entry.setUser(user);
		entry.setQuery(sql);
		entry.setAttachmentSourceUuid(att.getUuid());

		this.history.save(entry);

		LOGGER.info("[ATTACH-SQL] Completed execute by user {} of attachment SQL {} with name \"{}\" and content \"{}\"",
				user.getLogin(), att.getUuid(), att.getName(), sql);
	}

	/**
	 * @param att
	 * @return
	 * @see fr.uem.efluid.tools.AttachmentProcessor#formatForDisplay(fr.uem.efluid.tools.AttachmentProcessor.Compliant)
	 */
	@Override
	protected String formatForDisplay(Compliant att) {

		// Basic text line formating. Nothing more
		return super.formatForDisplay(att).replaceAll("\n", "<br/>");
	}

}
