package fr.uem.efluid.rest.v1;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import fr.uem.efluid.rest.v1.model.AsyncProcessView;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.web.bind.annotation.*;

import fr.uem.efluid.rest.RestApi;
import fr.uem.efluid.rest.v1.model.ApplicationInfoView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@RequestMapping(RestApi.API_ROOT + "/v1/app")
@Api("Provides details and management of current running application")
public interface ApplicationApi {

    /**
     * @return current app info (name / version)
     */
    @GetMapping("/")
    @ResponseBody
    @ApiOperation("Get name and application version for current instance")
    ApplicationInfoView getCurrentInfo();


    /**
     * @return current app state (RUNNING)
     */
    @GetMapping("/state")
    @ResponseBody
    @ApiOperation("Get running state of application")
    String getCurrentState();


    /**
     * @return current app processes
     */
    @GetMapping("/processes")
    @ResponseBody
    @ApiOperation("Get active async processes from application, like preparations")
    @ApiImplicitParams({
            @ApiImplicitParam(name = RestApi.TOKEN_PARAM, required = true, dataType = "string", paramType = "query")
    })
    List<AsyncProcessView> getCurrentProcesses();


    @PostMapping("/processes")
    @ApiOperation("Update active project for user.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = RestApi.TOKEN_PARAM, required = true, dataType = "string", paramType = "query")
    })
    void killActiveProcess(@RequestParam String processUUID);
}
