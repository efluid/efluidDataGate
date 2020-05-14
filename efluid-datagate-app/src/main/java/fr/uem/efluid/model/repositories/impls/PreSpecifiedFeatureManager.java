package fr.uem.efluid.model.repositories.impls;

import fr.uem.efluid.model.entities.ManagedFeature;
import fr.uem.efluid.model.repositories.FeatureManager;
import fr.uem.efluid.model.repositories.ManagedFeatureRepository;
import fr.uem.efluid.services.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@Repository
@Transactional
public class PreSpecifiedFeatureManager implements FeatureManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreSpecifiedFeatureManager.class);

    @Autowired
    private ManagedFeatureRepository features;

    @Autowired
    private Environment env;

    /**
     * @param feature
     * @return
     * @see fr.uem.efluid.model.repositories.FeatureManager#isEnabled(fr.uem.efluid.services.Feature)
     */
    @Override
    @Cacheable(cacheNames = "features", key = "#feature")
    public boolean isEnabled(Feature feature) {
        return this.features.getManagedState(feature);
    }

    /**
     * @param feature
     * @param value
     * @see fr.uem.efluid.model.repositories.FeatureManager#setFeatureState(fr.uem.efluid.services.Feature,
     * boolean)
     */
    @Override
    @CacheEvict(cacheNames = "features", allEntries = true)
    public void setFeatureState(Feature feature, boolean value) {
        LOGGER.debug("[FEATURE] Update managed feature {} with value {}", feature, value);

        ManagedFeature managed = this.features.findById(feature).orElse(new ManagedFeature(feature));

        managed.setEnabled(value);
        managed.setUpdatedTime(LocalDateTime.now());

        this.features.save(managed);
    }

    @PostConstruct
    public void initFromEnvironment() {

        // Init from env if doesn't exist yet
        for (Feature feat : Feature.values()) {
            if (!this.features.existsById(feat)) {

                LOGGER.info("[FEATURE] Feature {} not specified yet for application. Init from property \"{}\"",
                        feat.toString(), feat.getPropertyKey());

                ManagedFeature managed = new ManagedFeature();
                managed.setEnabled(this.env.getProperty(feat.getPropertyKey(), Boolean.class, Boolean.FALSE));
                managed.setUpdatedTime(LocalDateTime.now());
                managed.setFeature(feat);
                this.features.save(managed);
            }
        }
    }

    /**
     * Mostly for testing purpose : can reset all features from existing environment (config)
     */
    @CacheEvict(cacheNames = "features", allEntries = true)
    public void resetFromEnvironment(){
        for (Feature feat : Feature.values()) {
            setFeatureState(feat, this.env.getProperty(feat.getPropertyKey(), Boolean.class, Boolean.FALSE));
        }
    }

    /**
     * @return
     * @see fr.uem.efluid.model.repositories.FeatureManager#getAllFeatureStates()
     */
    @Override
    public Map<Feature, Boolean> getAllFeatureStates() {
        return Stream.of(Feature.values()).collect(Collectors.toMap(f -> f, f -> this.features.getManagedState(f)));
    }

}
