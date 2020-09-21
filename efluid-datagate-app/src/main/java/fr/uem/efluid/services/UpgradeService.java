package fr.uem.efluid.services;

import fr.uem.efluid.model.entities.Upgrade;
import fr.uem.efluid.model.repositories.UpgradeRepository;
import fr.uem.efluid.upgrades.UpgradeProcess;
import fr.uem.efluid.utils.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Run all upgrade process automatically
 *
 * @author elecomte
 * @version 1
 * @since v2.0.19
 */
@Transactional
@Service
public class UpgradeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeService.class);

    private final UpgradeRepository upgrades;

    private final Collection<UpgradeProcess> processes;

    public UpgradeService(
            @Autowired UpgradeRepository upgrades,
            @Autowired(required = false) Collection<UpgradeProcess> processes) {
        this.upgrades = upgrades;
        this.processes = processes;
    }

    @PostConstruct
    public void applyUpgrades() {

        Collection<String> ran = this.upgrades.findRanUpgradeNames();

        if (this.processes != null && ran.size() < this.processes.size()) {

            LOGGER.info("[UPGRADE] Some upgrades mst be executed");
            this.processes.stream()
                    .filter(p -> !ran.contains(p.name()))
                    .sorted()
                    .forEach(this::runOneUpgradeProcess);
        }
    }

    private void runOneUpgradeProcess(UpgradeProcess p) {
        try {
            LOGGER.info("[UPGRADE] Run upgrade {} of type {}", p.name(), p.getClass().getSimpleName());
            p.runUpgrade();
            Upgrade upgrade = new Upgrade();
            upgrade.setIndex(p.index());
            upgrade.setName(p.name());
            upgrade.setRunTime(LocalDateTime.now());
            this.upgrades.save(upgrade);
            LOGGER.info("[UPGRADE] Upgrade {} completed with success!", p.name());
        } catch (ApplicationException a) {
            LOGGER.error("[UPGRADE] Cannot process upgrade " + p.name() + " : check result", a);
            throw a;
        }

    }
}
