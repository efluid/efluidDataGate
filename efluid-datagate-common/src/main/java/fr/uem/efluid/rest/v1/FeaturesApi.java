package fr.uem.efluid.rest.v1;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.uem.efluid.rest.RestApi;
import fr.uem.efluid.services.Feature;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * <p>
 * Feature management : enable / disable asked features
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@RequestMapping(RestApi.API_ROOT + "/v1/features")
@Api("Feature management : enable / disable app features dynamically")
public interface FeaturesApi {

	@RequestMapping(value = "/enable/{feature}", method = POST)
	@ApiOperation("Enable the provided feature for the whole application")
	@ApiImplicitParams({
			@ApiImplicitParam(name = RestApi.TOKEN_PARAM, required = true, dataTypeClass = String.class, paramType = "query")
	})
	void enableFeature(@PathVariable("feature") Feature feature);

	@RequestMapping(value = "/disable/{feature}", method = POST)
	@ApiOperation("Disable the provided feature for the whole application")
	@ApiImplicitParams({
			@ApiImplicitParam(name = RestApi.TOKEN_PARAM, required = true, dataTypeClass = String.class, paramType = "query")
	})
	void disableFeature(@PathVariable("feature") Feature feature);

	@RequestMapping(value = "/", method = GET)
	@ResponseBody
	@ApiOperation("Get managed features with states for application")
	@ApiImplicitParams({
			@ApiImplicitParam(name = RestApi.TOKEN_PARAM, required = true, dataTypeClass = String.class, paramType = "query")
	})
	Map<Feature, Boolean> getFeatureStates();
}
