package fr.uem.efluid.model.repositories;

import java.util.Map;

import fr.uem.efluid.services.Feature;

/**
 * <p>
 * Basic feature management
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public interface FeatureManager {

	boolean isEnabled(Feature feature);

	void setFeatureState(Feature feature, boolean value);

	Map<Feature, Boolean> getAllFeatureStates();
}
