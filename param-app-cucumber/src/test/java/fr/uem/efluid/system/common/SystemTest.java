package fr.uem.efluid.system.common;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.model.repositories.DatabaseDescriptionRepository;
import fr.uem.efluid.security.UserHolder;
import fr.uem.efluid.services.*;
import fr.uem.efluid.services.types.CommitDetails;
import fr.uem.efluid.system.stubs.*;
import fr.uem.efluid.utils.Associate;
import fr.uem.efluid.utils.DataGenerationUtils;
import junit.framework.AssertionFailedError;
import org.junit.Assert;
import org.junit.Before;
import org.pac4j.core.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fr.uem.efluid.ColumnType.*;
import static fr.uem.efluid.system.stubs.ManagedDatabaseAccess.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@SpringBootTest(classes = {SystemTestConfig.class}, webEnvironment = WebEnvironment.DEFINED_PORT)
@ContextConfiguration
@AutoConfigureMockMvc
public abstract class SystemTest {

    private static final String DEFAULT_USER = "any";

    private static final String DEFAULT_PROJECT = "Default";

    private static final String DEFAULT_DOMAIN = "Test domain";

    private static final String DEFAULT_VERSION = "vDefault";

    private static final String DEFAULT_TABLE_ONE = "Table One";
    private static final String DEFAULT_TABLE_TWO = "Table Two";
    private static final String DEFAULT_TABLE_THREE = "Table Three";
    private static final String DEFAULT_TABLE_FOUR = "Table Four";
    private static final String DEFAULT_TABLE_FIVE = "Table Five";
    private static final String DEFAULT_TABLE_SIX = "Table Six";

    private static final String DEFAULT_WHERE = "1=1";

    protected static ResultActions currentAction;

    protected static String currentStartPage;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private DatabaseDescriptionRepository desc;

    @Autowired
    private ManagedDatabaseAccess managed;

    @Autowired
    private ModelDatabaseAccess model;

    @Autowired
    private BacklogDatabaseAccess backlog;

    @Autowired
    protected ApplicationDetailsService dets;

    @Autowired
    private UserHolder userHolder;

    @Autowired
    protected ProjectManagementService projectMgmt;

    @Autowired
    private Config securityConfig;

    @Autowired
    private TweakedAsyncDriver asyncDriver;

    @Autowired
    private TweakedDatabaseIdentifier databaseIdentifier;

    @Autowired
    protected PilotableCommitPreparationService prep;

    @Autowired
    protected CommitService commitService;

    @Autowired
    protected ExportImportService exportImportService;

    /**
     *
     */
    @Before
    public void setup() {

        currentAction = null;
        currentStartPage = null;

        resetAuthentication();
        resetAsyncProcess();
        resetDatabaseIdentifier();
    }

    /**
     * <p>
     * Entry point for backlog database (preloaded queries ...)
     * </p>
     *
     * @return
     */
    protected final BacklogDatabaseAccess backlogDatabase() {
        return this.backlog;
    }

    /**
     * <p>
     * Entry point for model database (preloaded queries ...)
     * </p>
     *
     * @return
     */
    protected final ModelDatabaseAccess modelDatabase() {
        return this.model;
    }

    /**
     * @return
     */
    protected final ManagedDatabaseAccess managedDatabase() {
        return this.managed;
    }

    /**
     *
     */
    protected void initMinimalWizzardData() {

        resetAsyncProcess();
        resetDatabaseIdentifier();

        User user = initDefaultUser();
        Project newProject = initDefaultProject();
        FunctionalDomain newDomain = initDefaultDomain(newProject);

        modelDatabase().initWizzardData(user, newProject, Collections.singletonList(newDomain));

        this.dets.completeWizzard();
    }

    /**
     *
     */
    protected void initMinimalWizzardDataWithDomains(List<String> domainNames) {

        resetAsyncProcess();
        resetDatabaseIdentifier();

        User user = initDefaultUser();
        Project newProject = initDefaultProject();

        modelDatabase().initWizzardData(user, newProject,
                domainNames.stream().map(n -> domain(n, newProject)).collect(Collectors.toList()));

        this.dets.completeWizzard();
    }

    /**
     *
     */
    protected void initCompleteDictionaryWith5Tables() {

        resetAsyncProcess();
        resetDatabaseIdentifier();

        User user = initDefaultUser();
        Project newProject = initDefaultProject();
        FunctionalDomain newDomain = initDefaultDomain(newProject);

        // Force cache
        this.desc.getTables();

        modelDatabase().initWizzardData(user, newProject, Collections.singletonList(newDomain));

        this.dets.completeWizzard();

        initDictionaryForDefaultVersionWithTables(newDomain, newProject, TABLE_ONE, TABLE_TWO, TABLE_THREE, TABLE_FIVE, TABLE_SIX);

    }


    protected void initDictionaryForDefaultVersionWithTables(FunctionalDomain domain, Project project, String... tableNames) {

        List<DictionaryEntry> tables = new ArrayList<>();
        List<TableLink> links = new ArrayList<>();

        // Prepare dictionary
        initDefaultTables(domain, tables, links, tableNames);

        modelDatabase().initDictionary(tables, links, initDefaultVersion(project));
    }


    /**
     *
     */
    protected void implicitlyAuthenticatedAndOnPage(String page) {

        // Authenticated as "any"
        this.securityConfig.setProfileManagerFactory(c -> new TestableProfileManager(c, DEFAULT_USER));

        // On home page
        currentStartPage = getCorrespondingLinkForPageName(page);
    }

    /**
     * @return
     */
    protected String getCurrentUserLogin() {

        User user = this.userHolder.getCurrentUser();

        return user != null ? user.getLogin() : null;
    }

    /**
     * @return
     */
    protected String getCurrentUserApiToken() {

        User user = this.userHolder.getCurrentUser();

        return user != null ? user.getToken() : null;
    }

    /**
     * @return
     */
    protected Project getCurrentUserProject() {
        return new Project(this.projectMgmt.getCurrentSelectedProject().getUuid());
    }

    /**
     * @return
     */
    protected FunctionalDomain getDefaultDomainFromCurrentProject() {
        return modelDatabase().findDomainByProjectAndName(getCurrentUserProject(), DEFAULT_DOMAIN);
    }

    /**
     *
     */
    protected void resetAuthentication() {

        // Reset auth
        this.userHolder.setCurrentUser(null);

        this.getCurrentUserLogin();
        this.userHolder.setWizzardUser(null);

        this.securityConfig.setProfileManagerFactory(c -> new TestableProfileManager(c, null));
    }

    /**
     *
     */
    protected void resetAsyncProcess() {
        this.asyncDriver.reset();
    }

    protected void resetDatabaseIdentifier() {
        this.databaseIdentifier.reset();
    }

    /**
     * <p>
     * For test on async process, allows to make the run "perpetual". Can be used for
     * example when checking the Diff generation, for validation of "exclusivity" of a
     * diff run
     * </p>
     */
    protected void mockEternalAsyncProcess() {
        this.asyncDriver.setLocked();
    }

    /**
     * <p>
     * Fix for testing the valid version provided by the database identifier. Allows also
     * to specify if the identifier table have an history or not
     * </p>
     *
     * @param version
     */
    protected void mockDatabaseIdentifierWithVersion(String version, boolean hasHistory) {
        this.databaseIdentifier.setFixedVersion(version);
        this.databaseIdentifier.setHasHistory(hasHistory);
    }

    /**
     * <p>
     * Simplified post process with common rules :
     * <ul>
     * <li>Set the currentAction</li>
     * <li>Take care of currentStartPage if any is set</li>
     * </ul>
     * </p>
     *
     * @param url
     * @param args
     * @throws Exception
     */
    protected final void get(String url, Object... args) throws Exception {

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(url, args);

        if (currentStartPage != null) {
            builder.header("Referer", currentStartPage);
        }

        // Add user token anyway
        if (url.startsWith("/rest/")) {
            builder.param("token", getCurrentUserApiToken());
        }

        builder.accept(MediaType.APPLICATION_JSON_UTF8);

        currentAction = this.mockMvc.perform(builder);
    }

    /**
     * <p>
     * Simplified post process with common rules :
     * <ul>
     * <li>Set the currentAction</li>
     * <li>Take care of currentStartPage if any is set</li>
     * </ul>
     * </p>
     *
     * @param url
     * @param params
     * @throws Exception
     */
    @SafeVarargs
    protected final void post(String url, final Associate<String, String>... params) throws Exception {
        post(url, Arrays.asList(params));
    }

    /**
     * @param url
     * @param params
     * @throws Exception
     */
    protected final void post(String url, final PostParamSet params) throws Exception {
        post(url, params.getParams());
    }

    /**
     * @param url
     * @param params
     * @throws Exception
     */
    private void post(String url, Collection<Associate<String, String>> params) throws Exception {

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(url);

        for (Associate<String, String> param : params) {
            builder.param(param.getOne(), param.getTwo());
        }

        if (currentStartPage != null) {
            builder.header("Referer", currentStartPage);
        }

        builder.accept(MediaType.APPLICATION_JSON_UTF8);

        currentAction = this.mockMvc.perform(builder);
    }

    /**
     * @param url
     * @param objects
     * @throws Exception
     */
    @SafeVarargs
    protected final void postObject(String url, Associate<String, Object>... objects) throws Exception {

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(url);

        // Add attribute
        for (Associate<String, Object> object : objects) {
            builder.requestAttr(object.getOne(), object.getTwo());
        }

        if (currentStartPage != null) {
            builder.header("Referer", currentStartPage);
        }

        builder.contentType(MediaType.APPLICATION_FORM_URLENCODED);

        currentAction = this.mockMvc.perform(builder);
    }


    /**
     * <p>
     * Real link for a page name
     * </p>
     *
     * @param pageName
     * @return
     */
    protected static String getCorrespondingLinkForPageName(String pageName) {

        String cleaned = pageName.replace("the ", "").trim();

        String link = DataSetHelper.REV_SITEMAP.get(cleaned);

        if (link == null) {
            throw new AssertionFailedError("No specified link for page name " + cleaned + ". Check fixtures / gherkin");
        }

        return link;
    }

    /**
     * <p>
     * Real link for a template
     * </p>
     *
     * @param name
     * @return
     */
    protected static String getCorrespondingTemplateForName(String name) {

        String template = DataSetHelper.REV_TEMPLATES.get(name);

        if (template == null) {
            throw new AssertionFailedError("No specified template for name " + name + ". Check fixtures / gherkin");
        }

        return template;
    }

    /**
     * <p>
     * Completed user for a dataset name
     * </p>
     *
     * @param login
     * @return
     */
    protected static User user(String login) {

        DataSetHelper.UserDef def = DataSetHelper.USERDEF.get(login);

        if (def == null) {
            throw new AssertionFailedError("No specified def for user login " + login + ". Check fixtures / gherkin");
        }

        User user = DataGenerationUtils.user(def.getLogin());

        user.setEmail(def.getEmail());
        user.setPassword(def.getPassword());

        return user;
    }

    /**
     * @return
     */
    private static User initDefaultUser() {
        return user(DEFAULT_USER);
    }

    /**
     * @return
     */
    private static Project initDefaultProject() {
        return project(DEFAULT_PROJECT);
    }

    /**
     * @param newProject
     * @return
     */
    private static FunctionalDomain initDefaultDomain(Project newProject) {
        return domain(DEFAULT_DOMAIN, newProject);
    }

    /**
     * @param newProject
     * @return
     */
    private static Version initDefaultVersion(Project newProject) {
        return version(DEFAULT_VERSION, newProject);
    }

    /**
     * @param domain
     * @return
     */
    private static void initDefaultTables(FunctionalDomain domain, List<DictionaryEntry> tables, List<TableLink> links, String... tableNames) {
        for (String tableName : tableNames) {
            switch (tableName) {
                case TABLE_ONE:
                    tables.add(table(DEFAULT_TABLE_ONE, TABLE_ONE, domain, "cur.\"PRESET\", cur.\"SOMETHING\"", DEFAULT_WHERE, "VALUE", STRING));
                    break;
                case TABLE_TWO:
                    tables.add(table(DEFAULT_TABLE_TWO, TABLE_TWO, domain, "cur.\"VALUE\", cur.\"OTHER\"", DEFAULT_WHERE, "KEY", PK_STRING));
                    break;
                case TABLE_THREE:
                    tables.add(table(DEFAULT_TABLE_THREE, TABLE_THREE, domain, "cur.\"OTHER\"", DEFAULT_WHERE, "VALUE", STRING));
                    break;
                case TABLE_FOUR:
                    DictionaryEntry tableFour = table(DEFAULT_TABLE_FOUR, TABLE_FOUR, domain, "cur.\"CONTENT_TIME\", cur.\"CONTENT_INT\", ln1.\"VALUE\" as ln_OTHER_TABLE_KEY", DEFAULT_WHERE, "KEY", STRING);
                    tables.add(tableFour);
                    links.add(DataGenerationUtils.link(tableFour, "OTHER_TABLE_KEY", "KEY", TABLE_ONE));
                    break;
                case TABLE_FIVE:
                    tables.add(table(DEFAULT_TABLE_FIVE, TABLE_FIVE, domain, "cur.\"DATA\", cur.\"SIMPLE\"", DEFAULT_WHERE, "KEY", PK_STRING));
                    break;
                case TABLE_SIX:
                    tables.add(table(DEFAULT_TABLE_SIX, TABLE_SIX, domain, "cur.\"TEXT\", cur.\"DATE\"", DEFAULT_WHERE, "IDENTIFIER", PK_ATOMIC));
                    break;
                default:
                    throw new AssertionError("Unsupported table name " + tableName);
            }
        }
    }

    /**
     * @param domain
     * @param project
     * @return
     */
    protected static FunctionalDomain domain(String domain, Project project) {
        return DataGenerationUtils.domain(domain, project);
    }

    /**
     * @param name
     * @param project
     * @return
     */
    protected static Version version(String name, Project project) {
        return DataGenerationUtils.version(name, project);
    }

    /**
     * @param project
     * @return
     */
    protected static Project project(String project) {
        return DataGenerationUtils.project(project);
    }

    protected CommitDetails getSavedCommit() {

        assertRequestWasOk();

        UUID savedCommitUUID = (UUID) Objects.requireNonNull(currentAction.andReturn()
                .getModelAndView()).getModel().get("createdUUID");

        assertThat(savedCommitUUID).isNotNull();

        return this.commitService.getExistingCommitDetails(savedCommitUUID);
    }

    /**
     * @param name
     * @param tableName
     * @param domain
     * @param select
     * @param where
     * @param key
     * @param keyType
     * @return
     */
    protected static DictionaryEntry table(String name, String tableName, FunctionalDomain domain, String select, String where, String key,
                                           ColumnType keyType) {
        return DataGenerationUtils.entry(name, domain, select, tableName, where, key, keyType);
    }

    /**
     * <p>
     * Get the cleaned parameter where a user spec can be specified. Remove some key
     * words, to make it available to detect the login name if any, or null.
     * </p>
     * <p>
     * <b>Here some supported use-cases for this method</b> :
     * <ul>
     * <li>"the user" => null</li>
     * <li>"any unauthenticated user" => null</li>
     * <li>"the user toto" => "toto"</li>
     * <li>"any user" => "any"</li>
     * </ul>
     * </p>
     *
     * @param param
     * @return
     */
    protected static String cleanUserParameter(String param) {
        String cleaned = param.replace("the ", "").replace("user ", "").trim();

        if (cleaned.equals("any unauthenticated")) {
            return null;
        }

        // Nullify empty
        return cleaned.length() == 0 ? null : cleaned;
    }

    /**
     * <p>
     * Shortcut for post param init
     * </p>
     *
     * @param name
     * @param value
     * @return
     */
    protected static <T> Associate<String, T> p(String name, T value) {
        return Associate.of(name, value);
    }

    protected static PostParamSet postParams() {
        return new PostParamSet();
    }

    protected static void assertRequestWasOk() {
        try {
            currentAction.andExpect(status().isOk());
        } catch (Throwable e) {
            throw new AssertionError("Request process failed. Expect code 200, get " +
                    currentAction.andReturn().getResponse().getStatus(),
                    currentAction.andReturn().getResolvedException());
        }
    }

    /**
     * @param property
     * @param size
     * @param propertyAccess
     * @param properties
     */
    protected static <T, K> void assertModelIsSpecifiedListWithProperties(
            String property,
            int size,
            Function<T, K> propertyAccess,
            Collection<K> properties) {

        @SuppressWarnings("unchecked")
        Collection<T> datas = (Collection<T>) Objects.requireNonNull(currentAction.andReturn().getModelAndView()).getModel().get(property);

        Assert.assertNotNull(datas);
        Assert.assertEquals(size, datas.size());
        Assert.assertTrue(datas.stream().map(propertyAccess).allMatch(properties::contains));
    }


    protected static final class PostParamSet {

        private List<Associate<String, String>> params = new ArrayList<>();

        /**
         * <p>
         * Can convert a property of type list to a clean HTTP POST compliant attribute
         * </p>
         *
         * @param toParams
         * @throws InvocationTargetException
         * @throws IllegalArgumentException
         * @throws Exception
         */
        public PostParamSet with(String propertyName, List<?> toParams) throws InternalTestException {
            int pos = 0;
            for (Object item : toParams) {
                Method[] methods = item.getClass().getMethods();

                for (Method method : methods) {
                    if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
                        try {
                            Object res = method.invoke(item);
                            String partName = propertyName + "[" + pos + "]." + propName(method);
                            if (res instanceof List) {
                                // Inner list
                                with(partName, (List<?>) res);
                            } else {
                                with(partName, res);
                            }
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new InternalTestException("Cannot extract data from method " + method.getName() + " in object of type "
                                    + item.getClass().getName(), e);
                        }
                    }
                }
                pos++;
            }

            return this;
        }

        /**
         * @param propertyName
         * @param toParam
         */
        public PostParamSet with(String propertyName, Object toParam) {
            this.params.add(Associate.of(propertyName, toParam != null ? toParam.toString() : null));
            return this;
        }

        /**
         * @return the params
         */
        List<Associate<String, String>> getParams() {
            return this.params;
        }

        /**
         * <p>
         * Get corresponding property name from a method
         * </p>
         *
         * @param method
         * @return
         */
        private static String propName(Method method) {
            if (method.getName().startsWith("get")) {
                return method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
            }
            return method.getName().substring(2, 3).toLowerCase() + method.getName().substring(3);

        }
    }
}
