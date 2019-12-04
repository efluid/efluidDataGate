package fr.uem.efluid.rest.v1;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.uem.efluid.rest.RestApi;
import fr.uem.efluid.rest.v1.model.ProjectDetailView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * <p>
 * REST API facade for project management : listing / active
 * </p>
 * 
 * @author elecomte
 * @since v0.2.0
 * @version 1
 */
@RequestMapping(RestApi.API_ROOT + "/v1/projects")
@Api("Project management / use : list available projects and select active project for user")
public interface ProjectApi {

	@RequestMapping(value = "/all", method = GET)
	@ResponseBody
	@ApiOperation("Get all available projects for current user (= prefered projects only)")
	@ApiImplicitParams({
			@ApiImplicitParam(name = RestApi.TOKEN_PARAM, required = true, dataType = "string", paramType = "query")
	})
	List<ProjectDetailView> getAvailableProjectDetails();

	@RequestMapping(value = "/active", method = GET)
	@ResponseBody
	@ApiOperation("Get active project for user.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = RestApi.TOKEN_PARAM, required = true, dataType = "string", paramType = "query")
	})
	ProjectDetailView getCurrentActiveProject();

	@RequestMapping(value = "/active", method = POST)
	@ApiOperation("Update active project for user.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = RestApi.TOKEN_PARAM, required = true, dataType = "string", paramType = "query")
	})
	void setCurrentActiveProject(@RequestParam String projectUuid);

}
