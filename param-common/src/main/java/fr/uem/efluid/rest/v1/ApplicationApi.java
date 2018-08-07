package fr.uem.efluid.rest.v1;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.uem.efluid.rest.RestApi;
import fr.uem.efluid.rest.v1.model.ApplicationInfoView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@RequestMapping(RestApi.API_ROOT + "/v1/app")
@Api("Provides details and management of current running application")
public interface ApplicationApi {

	/**
	 * @return current app info (name / version)
	 */
	@RequestMapping(path = { "/" }, method = GET)
	@ResponseBody
	@ApiOperation("Get name and application version for current instance")
	ApplicationInfoView getCurrentInfo();
}
