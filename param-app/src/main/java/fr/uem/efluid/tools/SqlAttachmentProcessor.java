package fr.uem.efluid.tools;

import java.io.BufferedReader;
import java.io.LineNumberReader;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import fr.uem.efluid.model.entities.ApplyHistoryEntry;
import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.ApplyHistoryEntryRepository;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import fr.uem.efluid.utils.FormatUtils;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class SqlAttachmentProcessor extends AttachmentProcessor {

	public static final String COMMENT_PREFIX = "--";
	public static final String SEPARATOR = ";";

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

		// Query must be initialized by script parsing
		String query = " - not set yet -";

		try (BufferedReader in = new BufferedReader(new StringReader(FormatUtils.toString(att.getData())));
				LineNumberReader fileReader = new LineNumberReader(in);) {

			query = ScriptUtils.readScript(fileReader, COMMENT_PREFIX, SEPARATOR);

			String[] queryParts = query.split(SEPARATOR);

			for (String sql : queryParts) {

				String cleaned = sql.replaceAll("\n", "").replaceAll("\r", "").trim();
				
				// Only for content line
				if (cleaned.length() > 0) {

					// Run query "as this"
					this.managedSource.execute(cleaned);

					LOGGER.debug("[ATTACH-SQL] Store an history entry for SQL update \"{}\" from attachment {}", cleaned, att.getUuid());

					// Will store as a specific attachment exec in history
					ApplyHistoryEntry entry = new ApplyHistoryEntry();
					entry.setRollback(false);
					entry.setTimestamp(Long.valueOf(System.currentTimeMillis()));
					entry.setUser(user);
					entry.setQuery(cleaned);
					entry.setAttachmentSourceUuid(att.getUuid());

					this.history.save(entry);
				}
			}

			LOGGER.info("[ATTACH-SQL] Completed execute by user {} of attachment SQL {} with name \"{}\" and content \"{}\"",
					user.getLogin(), att.getUuid(), att.getName(), query);
		}

		// Provides formated error for any run problem, with detailled message
		catch (Throwable t) {
			String message = "Error \"" + t.getMessage() + "\" provided as " + t.getClass().getSimpleName() + " for SQL script \""
					+ att.getName() + "\". The identified query to process was : " + query;
			throw new ApplicationException(ErrorType.ATTACHMENT_EXEC_ERROR, message, t, message);
		}
	}

	/**
	 * @param att
	 * @return
	 * @see fr.uem.efluid.tools.AttachmentProcessor#formatForDisplay(fr.uem.efluid.tools.AttachmentProcessor.Compliant)
	 */
	@Override
	protected String formatForDisplay(Compliant att) {

		// Basic text line formating. Nothing more
		return super.formatForDisplay(att)
				.replaceAll("--(.*)", "<span class=\"sql-comment\">--$1</span>")
				.replaceAll("\n", "<br/>");
	}

}
