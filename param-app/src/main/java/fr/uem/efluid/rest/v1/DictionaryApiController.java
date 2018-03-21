package fr.uem.efluid.rest.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.uem.efluid.rest.v1.model.CreatedDictionaryView;
import fr.uem.efluid.services.DictionaryManagementService;
import fr.uem.efluid.services.types.DictionaryExportPackage;
import fr.uem.efluid.services.types.ExportImportResult;
import fr.uem.efluid.services.types.ExportImportResult.ItemCount;
import fr.uem.efluid.services.types.FunctionalExportDomainPackage;
import fr.uem.efluid.services.types.TableLinkExportPackage;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.WebUtils;

/**
 * <p>
 * implemented shared REST API for dictionary
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@RestController
public class DictionaryApiController implements DictionaryApi {

	@Autowired
	private DictionaryManagementService dictionaryManagementService;

	/**
	 * @param request
	 * @return
	 * @throws ApplicationException
	 * @see fr.uem.efluid.rest.v1.DictionaryApi#uploadDictionaryPackage(org.springframework.web.multipart.MultipartHttpServletRequest)
	 */
	@Override
	public CreatedDictionaryView uploadDictionaryPackage(MultipartFile file) throws ApplicationException {

		ExportImportResult<Void> result = this.dictionaryManagementService.importAll(WebUtils.inputExportImportFile(file));

		CreatedDictionaryView view = new CreatedDictionaryView();

		ItemCount dict = result.getCounts().get(DictionaryExportPackage.DICT_EXPORT);
		ItemCount doms = result.getCounts().get(FunctionalExportDomainPackage.DOMAINS_EXPORT);
		ItemCount lins = result.getCounts().get(TableLinkExportPackage.LINKS_EXPORT);

		view.setAddedDictionaryEntryCount(dict.getAdded());
		view.setUpdatedDictionaryEntryCount(dict.getModified());
		view.setAddedDomainCount(doms.getAdded());
		view.setUpdatedDomainCount(doms.getModified());
		view.setAddedLinkCount(lins.getAdded());
		view.setUpdatedLinkCount(lins.getModified());

		return view;
	}

}