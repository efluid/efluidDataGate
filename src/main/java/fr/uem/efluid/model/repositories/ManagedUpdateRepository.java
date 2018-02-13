package fr.uem.efluid.model.repositories;

import java.util.List;

import fr.uem.efluid.model.DiffLine;

/**
 * <p>
 * Applicator of changes in managed DB, using available transactional process
 * </p>
 * <p>
 * <b><font color="red">Transaction management for Managed DB update is customized in the
 * apply method and WILL NOT use standard Spring Transactional process. The update need to
 * be rollbacked immediatly if failed in application.</font></b>.So this flow is used,
 * with associated rules for caller service :
 * <ul>
 * <li>If the update fail in method {@link #runAllChangesAndCommit(List)} then the changes
 * are <b>all rollbacked immediately</b>. A TechnicalException is throwed, and can be used
 * to fail caller process (and rollback also caller process, this time using standard
 * Spring transactional process)</li>
 * <li>If the update is successful, then it is <b>commited immediately, even if something
 * may fail in top caller service</b>. That's why <font color="red"><b>it is very
 * important to take care of this commit action, and avoid all other operations in core DB
 * AFTER a call to this repository</b></font>. All Status preparation should be done
 * before (they will be rollbacked automatically if repo fail here)</li>
 * </ul>
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface ManagedUpdateRepository {

	/**
	 * <p>
	 * Process a batched update, for mixed DiffLine combining multiple DictionaryEntries
	 * </p>
	 * <p>
	 * Should be batched, and made with prepared statment
	 * </p>
	 * <p>
	 * Due to the "double transaction" behavior of this process, it is needed by feature
	 * to commit immediately successful changes in Managed DB. So please check that their
	 * is no update on CORE db AFTER a call to this method
	 * </p>
	 * 
	 * @param lines
	 */
	void runAllChangesAndCommit(List<? extends DiffLine> lines);
}
