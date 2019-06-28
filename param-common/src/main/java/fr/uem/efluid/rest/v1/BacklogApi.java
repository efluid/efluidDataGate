package fr.uem.efluid.rest.v1;

import fr.uem.efluid.rest.RestApi;
import fr.uem.efluid.rest.v1.model.CommitCreatedResultView;
import fr.uem.efluid.rest.v1.model.CommitPrepareDetailsView;
import fr.uem.efluid.rest.v1.model.StartedMergeView;
import fr.uem.efluid.services.types.PreparationState;
import fr.uem.efluid.utils.ApplicationException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * <p>
 * REST API facade for backlog management : prepare, check and validate commits
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.1
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
    PreparationState initPreparedCommit();

    /**
     * <p>
     * Cancel a commit preparation
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
    PreparationState cancelPreparedCommit();

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
    PreparationState getCurrentPreparedCommitState();

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


    /**
     * <p>
     * Upload a given .par file containing commit items, and start a merge commit prepare
     * </p>
     *
     * @param file
     * @return
     * @throws ApplicationException
     */
    @RequestMapping(value = "/upload", method = POST)
    @ResponseBody
    @ApiOperation("Upload a commit \".par\" archive and start an asynchronous merge commit preparation. " +
            "The running merge is processed as a preparing commit and can be validated or canceled using other services")
    @ApiImplicitParams({
            @ApiImplicitParam(name = RestApi.TOKEN_PARAM, required = true, dataType = "string", paramType = "query")
    })
    StartedMergeView uploadAndInitPreparedCommit(@RequestParam("file") MultipartFile file) throws ApplicationException;

}
