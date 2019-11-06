package fr.uem.efluid.rest.v1;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import fr.uem.efluid.rest.RestApi;
import fr.uem.efluid.rest.v1.model.CreatedDictionaryView;
import fr.uem.efluid.rest.v1.model.VersionView;
import fr.uem.efluid.utils.ApplicationException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * <p>
 * Rest API def for dictionary management
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 2
 */
@RequestMapping(RestApi.API_ROOT + "/v1/dictionary")
@Api("Dictionary management : edit the mananged database parameter table dictionary from a basic API. Can be used in generation processes")
public interface DictionaryApi {

	/**
	 * <p>
	 * Upload a given .par file containing dictionary items, and apply it
	 * </p>
	 */
	@RequestMapping(value = "/upload", method = POST)
	@ResponseBody
	@ApiOperation("Upload and apply a given dictionary \".par\" archive. The archive must be valid. Will provides details on operated changes")
	@ApiImplicitParams({
			@ApiImplicitParam(name = RestApi.TOKEN_PARAM, required = true, dataType = "string", paramType = "query")
	})
	CreatedDictionaryView uploadDictionaryPackage(@RequestParam("file") MultipartFile file) throws ApplicationException;

	@RequestMapping(value = "/version/{name}", method = PUT)
	@ApiOperation("Set the current version of the dictionary")
	@ApiImplicitParams({
			@ApiImplicitParam(name = RestApi.TOKEN_PARAM, required = true, dataType = "string", paramType = "query")
	})
	void setVersion(@PathVariable("name") String versionName) throws ApplicationException;

	@RequestMapping(value = "/version", method = GET)
	@ResponseBody
	@ApiOperation("Get the current version of the dictionary")
	@ApiImplicitParams({
			@ApiImplicitParam(name = RestApi.TOKEN_PARAM, required = true, dataType = "string", paramType = "query")
	})
	VersionView getLastVersion() throws ApplicationException;

	@RequestMapping(value = "/versions", method = GET)
	@ResponseBody
	@ApiOperation("Get all the existing versions of the dictionary")
	@ApiImplicitParams({
			@ApiImplicitParam(name = RestApi.TOKEN_PARAM, required = true, dataType = "string", paramType = "query")
	})
	List<VersionView> getVersions() throws ApplicationException;
}
