package fr.uem.efluid.tools;

import java.io.*;
import java.sql.*;
import java.util.UUID;

import org.slf4j.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.model.repositories.ApplyHistoryEntryRepository;
import fr.uem.efluid.utils.*;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class SqlAttachmentProcessor extends AttachmentProcessor {

	public static final String COMMENT_PREFIX = "--";
	public static final String SEPARATOR = ";";
	public static final String PLSQL_DETECT = "begin";

	public static final String LOG_STORE_HISTORY_ENTRY_FROM_ATTACHMENT = "[ATTACH-SQL] Store an history entry for SQL update \"{}\" from attachment {}";


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
	 */
	@Override
	public void execute(User user, Compliant att, Commit commit) {

		LOGGER.debug("[ATTACH-SQL] Will execute a new SQL update from attachment with user {}, from attachment {}."
				+ " Search for operating type", user.getLogin(), att.getUuid());

		// Query must be initialized by script parsing
		String query = " - not set yet -";

		try (BufferedReader in = new BufferedReader(new StringReader(FormatUtils.toString(att.getData())));
				LineNumberReader fileReader = new LineNumberReader(in);) {

			query = ScriptUtils.readScript(fileReader, COMMENT_PREFIX, SEPARATOR, null);

			// Detect type of script -> PLSQL
			if (query.contains(PLSQL_DETECT) || query.contains(PLSQL_DETECT.toUpperCase())) {
				executeScriptAsStatement(user, query, att.getUuid(), commit);
			}

			// -> Script is standard set of SQL commands
			else {
				executeScriptAsInlineQueries(user, query, att.getUuid(), commit);
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
	 * <p>
	 * For flat script with various commands
	 * </p>
	 *
	 * @param user
	 * @param script
	 * @param attUuid
	 */
	private void executeScriptAsInlineQueries(User user, String script, UUID attUuid, Commit commit) {

		LOGGER.debug("[ATTACH-SQL] Will execute a new SQL update from attachment as SQL SCRIPT with user {}, from attachment {}",
				user.getLogin(), attUuid);

		String[] queryParts = script.split(SEPARATOR);

		for (String sql : queryParts) {

			String cleaned = sql.replaceAll("\n", "").replaceAll("\r", "").trim();

			// Only for content line
			if (cleaned.length() > 0) {

				// Run query "as this"
				this.managedSource.execute(cleaned);

				LOGGER.debug(LOG_STORE_HISTORY_ENTRY_FROM_ATTACHMENT, cleaned, attUuid);

				ApplyHistoryEntry entry = initApplyHistoryEntry(user, attUuid, commit, cleaned);

				this.history.save(entry);
			}
		}
	}

	/**
	 * Will store as a specific attachment exec in history
	 */
	private ApplyHistoryEntry initApplyHistoryEntry(User user, UUID attUuid, Commit commit, String query) {
		ApplyHistoryEntry entry = new ApplyHistoryEntry();
		entry.setRollback(false);
		entry.setTimestamp(System.currentTimeMillis());
		entry.setUser(user);
		entry.setQuery(query);
		entry.setCommit(commit);
		entry.setAttachmentSourceUuid(attUuid);
		return entry;
	}

	/**
	 * <p>
	 * For PLSQL Scripts
	 * </p>
	 *
	 * @param user
	 * @param script
	 * @param attUuid
	 * @throws SQLException
	 */
	private void executeScriptAsStatement(User user, String script, UUID attUuid, Commit commit) throws SQLException {

		LOGGER.debug("[ATTACH-SQL] Will execute a new SQL update from attachment as PLSQL with user {}, from attachment {}",
				user.getLogin(), attUuid);

		try (Connection con = this.managedSource.getDataSource().getConnection();
				CallableStatement cs = con.prepareCall(script);) {

			cs.execute();

			LOGGER.debug(LOG_STORE_HISTORY_ENTRY_FROM_ATTACHMENT, script, attUuid);

			ApplyHistoryEntry entry = initApplyHistoryEntry(user, attUuid, commit, script);

			this.history.save(entry);
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
