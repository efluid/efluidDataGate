package fr.uem.efluid.cucumber.stubs;

import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.model.repositories.*;
import fr.uem.efluid.security.UserHolder;
import fr.uem.efluid.services.types.ProjectData;
import fr.uem.efluid.services.types.TransformerDefDisplay;
import fr.uem.efluid.tools.Transformer;
import org.pac4j.core.credentials.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
@Transactional
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

    @Autowired
    private TransformerDefRepository transformerDefs;

    /**
     * Init all "similar to a wizard init"
     *
     * @param user        created user in wizard
     * @param initProject created project in wizard
     * @param addDomains  all created domains in wizard
     */
    public void initWizardData(final User user, final Project initProject, final List<FunctionalDomain> addDomains) {

        LOGGER.info("[MODEL-INIT] Setup wizard resulting data");

        Project project = this.projects.save(initProject);
        this.projects.flush();

        Set<Project> prefered = new HashSet<>();
        prefered.add(project);
        // Like wizard, preset default project
        user.setPreferedProjects(prefered);
        user.setSelectedProject(project);
        user.setCreatedTime(LocalDateTime.now());

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
     * For data cleanup without transaction management
     */
    public void dropModel() {
        this.transformerDefs.deleteAll();
        this.versions.deleteAll();
        this.links.deleteAll();
        this.entries.deleteAll();
        this.domains.deleteAll();

        // Do not drop technical user
        this.users.findAll().stream()
                .filter(u -> !u.getLogin().equals(UserHolder.TECHNICAL_USER))
                .forEach(this.users::delete);

        // And reset technical user projects
        this.users.findByLogin(UserHolder.TECHNICAL_USER).ifPresent(
               user -> {
                   user.setPreferedProjects(new HashSet<>());
                   this.users.save(user);
               }
        );

        this.projects.deleteAll();

        this.transformerDefs.flush();
        this.versions.flush();
        this.links.flush();
        this.entries.flush();
        this.domains.flush();
        this.users.flush();
        this.projects.flush();
    }

    /**
     * Init a complete dictionary
     *
     * @param tables     DictionaryEntry to init in a list
     * @param tableLinks TableLink to init in a list
     * @param version    Version to init
     */
    public void initDictionary(List<DictionaryEntry> tables, List<TableLink> tableLinks, Version version) {

        LOGGER.info("[MODEL-INIT] Setup some dictionary data");

        this.entries.saveAll(tables);
        this.links.saveAll(tableLinks);
        this.versions.save(version);

        this.entries.flush();
        this.versions.flush();
    }

    public void updateDictionaryFilterClause(String tableName, String filterclause) {
        DictionaryEntry dict = this.entries.findByTableName(tableName);
        dict.setWhereClause(filterclause);
        this.entries.saveAndFlush(dict);
    }

    /**
     * <p>
     * Set versions. Will create them at startDaysBefore days before now. If more than
     * one, each will be created one day after previous, starting to startDaysBefore. So
     * for 3 versions, if startDaysBefore = 10, first is created 10 days ago, second 9
     * days ago, third 8 days ago
     * </p>
     *
     * @param project         associated project
     * @param versionNames    version names to init
     * @param startDaysBefore int value of days before for version init
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
     * <p>
     * Reset some versions for dictionary init in "destination" environment
     * </p>
     *
     * @param project     associated project
     * @param versionName last version to keep from source environment
     */
    public void dropVersionsAfter(Project project, String versionName) {

        AtomicInteger idx = new AtomicInteger(0);

        List<Version> existingVersions = this.versions.findByProject(project);

        boolean noMoreToKeep = false;

        for (Version version : existingVersions) {

            // Drop everything before specified version
            if (noMoreToKeep) {
                this.versions.delete(version);
            } else if (version.getName().equals(versionName)) {
                noMoreToKeep = true;
            }
        }

        this.versions.flush();
    }

    /**
     * Create a valid transformer. Config is NOT validated
     *
     * @param project     associated project
     * @param name        transformer def name
     * @param transformer identified transformer to describe
     * @param config      config to store
     * @param priority    int priority
     * @return saved transformer
     */
    public TransformerDefDisplay initTransformer(Project project, String name, Transformer<?, ?> transformer, String config, int priority) {
        TransformerDef def = new TransformerDef();
        def.setProject(project);
        def.setUuid(UUID.randomUUID());
        def.setType(transformer.getClass().getSimpleName());
        def.setName(name);
        def.setConfiguration(config);
        def.setCreatedTime(LocalDateTime.now().minusDays(1));
        def.setUpdatedTime(def.getCreatedTime());
        def.setPriority(priority);

        def = this.transformerDefs.save(def);
        this.transformerDefs.flush();
        return new TransformerDefDisplay(def, transformer.getName());
    }

    public Project findProjectByName(String name) {
        return this.projects.findByName(name);
    }

    public Project getAllProjects() {
        return this.projects.getAllProjects();
    }

    public Optional<TransformerDef> findTransformerDefByProjectAndNameAndType(Project pro, String name, Transformer<?, ?> tran) {
        return this.transformerDefs.findByProjectAndNameAndType(pro, name, tran.getClass().getSimpleName());
    }

    /**
     * @param project Corresponding Project
     * @param name    requested version name
     * @return found version
     */
    public Version findVersionByProjectAndName(Project project, String name) {
        return this.versions.findByNameAndProject(name.trim(), project);
    }

    /**
     * @param project Corresponding Project
     * @param name    requested domain name
     * @return found FunctionalDomain
     */
    public FunctionalDomain findDomainByProjectAndName(Project project, String name) {
        return this.domains.findByProjectAndName(project, name);
    }

    public DictionaryEntry findDictionaryEntryByProjectAndTableName(Project project, String tablename) {
        return this.entries.findByDomainProject(project).stream().filter(e -> e.getTableName().equals(tablename)).findFirst()
                .orElseThrow(() -> new AssertionError("Cannot find entry for table name " + tablename));
    }

    public void forceUpdateVersion(Project project) {
        Version lastVersion = findLastVersionForProject(project);

        Version newVersion = new Version();
        newVersion.setName(lastVersion.getName() + "_UPD");
        newVersion.setUpdatedTime(LocalDateTime.now());
        newVersion.setUuid(UUID.randomUUID());
        newVersion.setProject(project);
        newVersion.setModelIdentity("1234");
        newVersion.setCreatedTime(LocalDateTime.now());

        this.versions.delete(lastVersion);
        this.versions.save(newVersion);
    }

    public Version findLastVersionForProject(Project proj) {
        return this.versions.getLastVersionForProject(proj);
    }
}
