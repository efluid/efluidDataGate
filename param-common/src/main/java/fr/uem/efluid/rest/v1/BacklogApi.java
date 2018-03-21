package fr.uem.efluid.rest.v1;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.uem.efluid.rest.Api;
import fr.uem.efluid.rest.v1.model.CommitCreatedResultView;
import fr.uem.efluid.services.types.PilotedCommitStatus;

/**
 * <p>
 * REST API facade for backlog management : prepare, check and validate commits
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@RequestMapping(Api.API_ROOT + "/v1/backlog")
public interface BacklogApi {

	/**
	 * <p>
	 * Start a new commit index analysis, or provides the active running analysis status
	 * </p>
	 * 
	 * @return
	 */
	@RequestMapping(value = "/diff", method = POST)
	@ResponseBody
	PilotedCommitStatus initPreparedCommit();

	/**
	 * <p>
	 * Get the active commit index analysis status
	 * </p>
	 * 
	 * @return
	 */
	@RequestMapping(value = "/status", method = GET)
	@ResponseBody
	PilotedCommitStatus getCurrentPreparedCommitStatus();

	/**
	 * <p>
	 * Commit the prepared analysis immediately, applying all the identified Diff, on all
	 * domains, and specify the commit comment.
	 * </p>
	 * 
	 * @param commitComment
	 * @return
	 */
	@RequestMapping(value = "/commit", method = POST)
	@ResponseBody
	CommitCreatedResultView validateCurrentPreparedCommit(@RequestParam String commitComment);
}
