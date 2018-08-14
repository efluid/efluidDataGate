package fr.uem.efluid.rest.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import fr.uem.efluid.model.metas.ManagedModelDescription;
import fr.uem.efluid.rest.v1.model.ApplicationInfoView;
import fr.uem.efluid.services.ApplicationDetailsService;
import fr.uem.efluid.services.types.ApplicationInfo;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@RestController
public class ApplicationApiController implements ApplicationApi {

	@Autowired
	private ApplicationDetailsService applicationDetailsService;

	/**
	 * @return
	 * @see fr.uem.efluid.rest.v1.ApplicationApi#getCurrentInfo()
	 */
	@Override
	public ApplicationInfoView getCurrentInfo() {

		ApplicationInfo info = this.applicationDetailsService.getInfo();
		ManagedModelDescription desc = this.applicationDetailsService.getCurrentModelId();
		return new ApplicationInfoView(info.getVersion(), info.getInstanceName(), desc != null ? desc.getIdentity() : null);
	}

}
