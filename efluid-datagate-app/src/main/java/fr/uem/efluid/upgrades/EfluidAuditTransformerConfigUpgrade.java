package fr.uem.efluid.upgrades;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.TransformerDef;
import fr.uem.efluid.model.repositories.TransformerDefRepository;
import fr.uem.efluid.services.UpgradeService;
import fr.uem.efluid.transformers.EfluidAuditDataTransformer;
import fr.uem.efluid.transformers.Transformer;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * The EfluidAuditDataTransformer config format has been updated and is not compliant anymore with existing
 * transformer defs. We need to upgrade the data
 *
 * @author elecomte
 * @version 1
 * @since v2.0.19
 */
@Component
public class EfluidAuditTransformerConfigUpgrade implements UpgradeProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeService.class);

    @Autowired
    private TransformerDefRepository transformerDefRepository;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public boolean repeat() {
        return false;
    }

    @Override
    public int index() {
        return 2;
    }

    @Override
    public String name() {
        return "efluid-audit-transformer-upgrade";
    }

    @Override
    public void runUpgrade() throws ApplicationException {

        AtomicInteger current = new AtomicInteger(1);

        // Get the def to upgrade
        List<TransformerDef> toUpgrade = this.transformerDefRepository.findAll().stream()
                .filter(d -> d.getType().equals(EfluidAuditDataTransformer.class.getSimpleName())).collect(Collectors.toList());

        // Upgrade all directly
        toUpgrade.forEach(d -> {
            this.transformerDefRepository.save(migrate(d));
            LOGGER.info("[UPGRADE] Upgraded efluid audit transformers {}/{}", current.getAndIncrement(), toUpgrade.size());
        });

        this.transformerDefRepository.flush();
    }

    private TransformerDef migrate(TransformerDef def) {

        try {
            OldConfigFormat old = this.mapper.readValue(def.getConfiguration(), OldConfigFormat.class);
            EfluidAuditDataTransformer.Config newconfig = new EfluidAuditDataTransformer.Config();
            // Migrate actor updates
            if (old.getActorUpdates() != null) {
                newconfig.setActorUpdates(old.getActorUpdates().entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new EfluidAuditDataTransformer.Config.ApplicationSpec(e.getValue(), IndexAction.values())
                )));
            }
            // Migrate date updates
            if (old.getDateUpdates() != null) {
                newconfig.setDateUpdates(old.getDateUpdates().entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new EfluidAuditDataTransformer.Config.ApplicationSpec(e.getValue(), IndexAction.values())
                )));
            }

            // Other properties are not modified
            newconfig.setTablePattern(old.getTablePattern());
            newconfig.setAppliedKeyPatterns(old.getAppliedKeyPatterns());
            newconfig.setAppliedValueFilterPatterns(old.getAppliedValueFilterPatterns());
            def.setConfiguration(this.mapper.writeValueAsString(newconfig));
            return def;
        } catch (IOException e) {
            throw new ApplicationException(ErrorType.JSON_READ_ERROR, "Invalid configuration format in transformer - cannot migrate", e);
        }
    }

    public static class OldConfigFormat extends Transformer.TransformerConfig {

        private List<String> appliedKeyPatterns;

        private Map<String, String> appliedValueFilterPatterns;

        private Map<String, String> dateUpdates;

        private Map<String, String> actorUpdates;

        public OldConfigFormat() {
            super();
        }

        public List<String> getAppliedKeyPatterns() {
            return this.appliedKeyPatterns;
        }

        public void setAppliedKeyPatterns(List<String> appliedKeyPatterns) {
            this.appliedKeyPatterns = appliedKeyPatterns;
        }

        public Map<String, String> getAppliedValueFilterPatterns() {
            return this.appliedValueFilterPatterns;
        }

        public void setAppliedValueFilterPatterns(Map<String, String> appliedValueFilterPatterns) {
            this.appliedValueFilterPatterns = appliedValueFilterPatterns;
        }

        public Map<String, String> getDateUpdates() {
            return this.dateUpdates;
        }

        public void setDateUpdates(Map<String, String> dateUpdates) {
            this.dateUpdates = dateUpdates;
        }

        public Map<String, String> getActorUpdates() {
            return this.actorUpdates;
        }

        public void setActorUpdates(Map<String, String> actorUpdates) {
            this.actorUpdates = actorUpdates;
        }

    }
}
