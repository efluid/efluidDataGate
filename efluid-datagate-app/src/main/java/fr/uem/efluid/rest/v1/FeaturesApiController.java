package fr.uem.efluid.rest.v1;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import fr.uem.efluid.model.repositories.FeatureManager;
import fr.uem.efluid.services.Feature;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@RestController
@Transactional
public class FeaturesApiController implements FeaturesApi {

	@Autowired
	private FeatureManager manager;

	/**
	 * @see fr.uem.efluid.rest.v1.FeaturesApi#enableFeature(fr.uem.efluid.services.Feature)
	 */
	@Override
	public void enableFeature(Feature feature) {
		this.manager.setFeatureState(feature, true);

	}

	/**
	 * @see fr.uem.efluid.rest.v1.FeaturesApi#disableFeature(fr.uem.efluid.services.Feature)
	 */
	@Override
	public void disableFeature(Feature feature) {
		this.manager.setFeatureState(feature, false);

	}

	/**
	 * @see fr.uem.efluid.rest.v1.FeaturesApi#getFeatureStates()
	 */
	@Override
	public Map<Feature, Boolean> getFeatureStates() {
		return this.manager.getAllFeatureStates();
	}

}
