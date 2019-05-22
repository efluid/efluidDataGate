package fr.uem.efluid.system.stubs;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.model.repositories.*;
import org.pac4j.core.credentials.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.uem.efluid.security.UserHolder;

/**
 * <p>
 * A common component for init and use of app model (the database used for behaviors of
 * the app)
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@Component
@SuppressWarnings("unused")
public class ModelDatabaseAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelDatabaseAccess.class);

    @Autowired
    private UserRepository users;

    @Autowired
    private UserHolder holder;

    @Autowired
    private ProjectRepository projects;

    @Autowired
    private FunctionalDomainRepository domains;

    @Autowired
    private DictionaryRepository entries;

    @Autowired
    private TableLinkRepository links;

    @Autowired
    private VersionRepository versions;

    @Autowired
    private PasswordEncoder encoder;

    /**
     * @param user
     * @param initProject
     * @param addDomains
     */
    public void initWizzardData(final User user, final Project initProject, final List<FunctionalDomain> addDomains) {

        LOGGER.info("[MODEL-INIT] Setup wizzard resulting data");

        Project project = this.projects.save(initProject);
        this.projects.flush();

        Set<Project> prefered = new HashSet<>();
        prefered.add(project);
        // Like wizzard, preset default project
        user.setPreferedProjects(prefered);
        user.setSelectedProject(project);

        // User pwd is encoded
        user.setPassword(this.encoder.encode(user.getPassword()));

        this.holder.setWizzardUser(this.users.save(user));
        this.users.flush();

        addDomains.forEach(d -> {
            d.setProject(project);
            this.domains.save(d);
        });
        this.domains.flush();
    }

    /**
     * @param tables
     * @param version
     */
    public void initDictionary(List<DictionaryEntry> tables, List<TableLink> tableLinks, Version version) {

        LOGGER.info("[MODEL-INIT] Setup some dictionary data");

        this.entries.saveAll(tables);
        this.links.saveAll(tableLinks);
        this.versions.save(version);

        this.entries.flush();
        this.versions.flush();
    }

    /**
     * <p>
     * Set versions. Will create them at startDaysBefore days before now. If more than
     * one, each will be created one day after previous, starting to startDaysBefore. So
     * for 3 versions, if startDaysBefore = 10, first is created 10 days ago, second 9
     * days ago, third 8 days ago
     * </p>
     *
     * @param project
     * @param versionNames
     * @param startDaysBefore
     */
    public void initVersions(Project project, Collection<String> versionNames, int startDaysBefore) {

        AtomicInteger idx = new AtomicInteger(0);

        this.versions.saveAll(versionNames.stream().map(n -> {
            Version ver = new Version();
            ver.setCreatedTime(LocalDateTime.now().minusDays(startDaysBefore - idx.getAndIncrement()));
            ver.setUpdatedTime(ver.getCreatedTime());
            ver.setUuid(UUID.randomUUID());
            ver.setModelIdentity("iii");
            ver.setName(n.trim());
            ver.setProject(project);
            return ver;
        }).collect(Collectors.toList()));
        this.versions.flush();
    }

    /**
     * @param project
     * @param name
     * @return
     */
    public Version findVersionByProjectAndName(Project project, String name) {
        return this.versions.findByNameAndProject(name.trim(), project);
    }

    /**
     * @param project
     * @param name
     * @return
     */
    public FunctionalDomain findDomainByProjectAndName(Project project, String name) {
        return this.domains.findByProjectAndName(project, name);
    }

    /**
     * @param project
     * @param tablename
     * @return
     */
    public DictionaryEntry findDictionaryEntryByProjectAndTableName(Project project, String tablename) {
        return this.entries.findByDomainProject(project).stream().filter(e -> e.getTableName().equals(tablename)).findFirst()
                .orElseThrow(() -> new AssertionError("Cannot find entry for table name " + tablename));
    }


}
