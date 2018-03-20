package fr.uem.efluid.rest.v1;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import fr.uem.efluid.rest.Api;
import fr.uem.efluid.rest.v1.model.CreatedDictionaryView;
import fr.uem.efluid.utils.ApplicationException;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@RequestMapping(Api.API_ROOT + "/v1/dictionary")
public interface DictionaryApi {

	@RequestMapping(value = "/upload", method = POST)
	@ResponseBody
	CreatedDictionaryView uploadDictionaryPackage(@RequestParam("file") MultipartFile file) throws ApplicationException;

}
