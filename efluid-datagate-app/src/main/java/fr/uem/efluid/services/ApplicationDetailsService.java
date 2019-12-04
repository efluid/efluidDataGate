package fr.uem.efluid.services;

import fr.uem.efluid.model.metas.ManagedModelDescription;
import fr.uem.efluid.model.repositories.*;
import fr.uem.efluid.services.types.ApplicationDetails;
import fr.uem.efluid.services.types.ApplicationInfo;
import fr.uem.efluid.services.types.ProjectData;
import fr.uem.efluid.tools.AsyncDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@Service
public class ApplicationDetailsService {

    private static final long INDEX_ENTRY_ESTIMATED_SIZE = 500;
    private static final long LOB_ESTIMATED_SIZE = 2000;

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationDetailsService.class);

    @Autowired
    private CommitRepository commits;

    @Autowired
    private IndexRepository index;

    @Autowired
    private FunctionalDomainRepository domains;

    @Autowired
    private DictionaryRepository dictionary;

    @Autowired
    private LobPropertyRepository lobs;

    @Autowired
    private UserRepository users;

    @Autowired
    private ProjectRepository projects;

    @Autowired
    private VersionRepository versions;

    @Autowired
    private ProjectManagementService projectService;

    @Autowired
    private ManagedModelDescriptionRepository modelDescs;

    @Autowired
    private ApplicationInfo info;

    @Autowired
    private AsyncDriver asyncDriver;

    @Value("${datagate-efluid.managed-datasource.url}")
    private String managedDbUrl;

    // If wizard started, cannot quit
    private boolean wizardCompleted;

    /**
     * @return true if must route to wizard page
     */
    public boolean isNeedWizard() {
        // Until a wizard is completed (or data is complete), it is not possible to avoid
        // the wizard
        return !this.wizardCompleted;
    }

    /**
     * @return all active running async process
     */
    public Collection<AsyncDriver.AsyncSourceProcess> getActiveAsyncProcess() {
        return this.asyncDriver.listCurrentInSurvey();
    }

    /**
     * @param identifier identifier for a process under survey
     */
    public void killActiveAsyncProcess(UUID identifier) {
        this.asyncDriver.kill(identifier);
    }

    /**
     * Heavy load
     * @return current App details with many count.
     */
    @Cacheable("details")
    public ApplicationDetails getCurrentDetails() {

        LOGGER.debug("Loading new details");

        ApplicationDetails details = new ApplicationDetails();

        details.setInfo(getInfo());
        details.setCommitsCount(this.commits.count());
        details.setDbUrl(this.managedDbUrl);
        details.setIndexCount(this.index.count());
        details.setDomainsCount(this.domains.count());
        details.setDictionaryCount(this.dictionary.count());
        details.setLobsCount(this.lobs.count());
        details.setIndexSize(getEstimatedIndexSize());
        details.setProjectsCount(this.projects.count());
        details.setVersionsCount(this.versions.count());
        ProjectData project = this.projectService.getCurrentSelectedProject();

        if (project != null) {
            details.setDomainsCountForProject(this.domains.countForProject(project.getUuid()));
            details.setVersionsCountForProject(this.versions.countVersionsForProject(project.getUuid()));
        }

        // Can be null / empty
        if (this.modelDescs.hasToCheckDescriptions()) {
            List<ManagedModelDescription> descs = this.modelDescs.getModelDescriptions();
            if (descs.size() > 0) {
                details.setModelDesc(descs.get(descs.size() - 1));
            }
        }

        return details;
    }

    /**
     * @return null if not found / not enabled
     */
    public ManagedModelDescription getCurrentModelId() {

        if (this.modelDescs.hasToCheckDescriptions()) {
            List<ManagedModelDescription> descs = this.modelDescs.getModelDescriptions();
            if (descs.size() > 0) {
                return descs.get(descs.size() - 1);
            }
        }

        return null;
    }

    @PostConstruct
    public void completeWizard() {

        this.wizardCompleted = this.users.count() > 0 && this.domains.count() > 0;

        if (!this.wizardCompleted) {
            LOGGER.info("Application is started in wizard mode : no data found locally");
        }
    }

    /**
     * @return fixed app info to display
     */
    public ApplicationInfo getInfo() {
        return this.info;
    }

    private String getEstimatedIndexSize() {

        long size = this.index.count() * INDEX_ENTRY_ESTIMATED_SIZE;
        long lobSize = this.lobs.count() * LOB_ESTIMATED_SIZE;
        BigDecimal estim = new BigDecimal((size + lobSize) / (1024 * 1024))
                .setScale(1, RoundingMode.HALF_UP);

        LOGGER.debug("Checking estimated index size. Found {} items and {} lobs, for a an estimated total size of {} Mb",
                size, lobSize, estim);

        return estim.toPlainString() + " Mb";
    }
}
