package fr.uem.efluid.rest.v1;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.uem.efluid.rest.RestApi;
import fr.uem.efluid.rest.v1.model.CommitCreatedResultView;
import fr.uem.efluid.rest.v1.model.CommitPrepareDetailsView;
import fr.uem.efluid.services.types.PilotedCommitStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * <p>
 * REST API facade for backlog management : prepare, check and validate commits
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@RequestMapping(RestApi.API_ROOT + "/v1/backlog")
@Api("Backlog management : create diff with \"/diff\", check status and content with \"/status\" and \"/details\" and then create a new commit with \"commit\"")
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
	@ApiOperation("Start a new diff : launch an asynchronous analysis of the differences found in the managed database, to prepare a new commit. Provides the diff status")
	@ApiImplicitParams({
			@ApiImplicitParam(name = RestApi.TOKEN_PARAM, required = true, dataType = "string", paramType = "query")
	})
	PilotedCommitStatus initPreparedCommit();

	/**
	 * <p>
	 * Start a new commit index analysis, or provides the active running analysis status
	 * </p>
	 * 
	 * @return
	 */
	@RequestMapping(value = "/cancel", method = POST)
	@ResponseBody
	@ApiOperation("Cancel a running or prepared diff. Will be ready to start a new one")
	@ApiImplicitParams({
			@ApiImplicitParam(name = RestApi.TOKEN_PARAM, required = true, dataType = "string", paramType = "query")
	})
	PilotedCommitStatus cancelPreparedCommit();

	/**
	 * <p>
	 * Get the active commit index analysis status
	 * </p>
	 * 
	 * @return
	 */
	@RequestMapping(value = "/status", method = GET)
	@ResponseBody
	@ApiOperation("Get the currently running diff status. If diff completed, status will be \"COMMIT_CAN_PREPARE\"")
	@ApiImplicitParams({
			@ApiImplicitParam(name = RestApi.TOKEN_PARAM, required = true, dataType = "string", paramType = "query")
	})
	PilotedCommitStatus getCurrentPreparedCommitStatus();

	/**
	 * <p>
	 * Get more informations about the prepared commit, to check if their is some content
	 * for example before validating it
	 * </p>
	 * 
	 * @return
	 */
	@RequestMapping(value = "/details", method = GET)
	@ResponseBody
	@ApiOperation("Get some details on the \"COMMIT_CAN_PREPARE\" diff. If not completed yet, result is empty")
	@ApiImplicitParams({
			@ApiImplicitParam(name = RestApi.TOKEN_PARAM, required = true, dataType = "string", paramType = "query")
	})
	CommitPrepareDetailsView getCurrentPreparedCommitDetails();

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
	@ApiOperation("Approve all diff content on currently \"COMMIT_CAN_PREPARE\" diff, and create a commit with the specified comment. If diff is not completed yet, will fail.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = RestApi.TOKEN_PARAM, required = true, dataType = "string", paramType = "query")
	})
	CommitCreatedResultView validateCurrentPreparedCommit(@RequestParam String commitComment);

}
