package fr.uem.efluid.cucumber.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldif.LDIFReader;
import fr.uem.efluid.ColumnType;
import fr.uem.efluid.cucumber.stubs.*;
import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.model.repositories.DatabaseDescriptionRepository;
import fr.uem.efluid.security.UserHolder;
import fr.uem.efluid.services.*;
import fr.uem.efluid.services.types.*;
import fr.uem.efluid.tools.ManagedQueriesGenerator;
import fr.uem.efluid.tools.ManagedValueConverter;
import fr.uem.efluid.transformers.Transformer;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.Associate;
import fr.uem.efluid.utils.DataGenerationUtils;
import fr.uem.efluid.utils.DatasourceUtils;
import io.cucumber.datatable.DataTable;
import junit.framework.AssertionFailedError;
import org.assertj.core.api.ObjectAssert;
import org.junit.runner.RunWith;
import org.pac4j.core.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static fr.uem.efluid.ColumnType.*;
import static fr.uem.efluid.cucumber.stubs.ManagedDatabaseAccess.*;
import static fr.uem.efluid.model.entities.IndexAction.REMOVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author elecomte
 * @version 2
 * @since v0.0.8
 */
@SuppressWarnings("unused")
@RunWith(SpringRunner.class)
public abstract class CucumberStepDefs {

    private static final Logger LOGGER = LoggerFactory.getLogger(CucumberStepDefs.class);

    private static final String DEFAULT_USER = "any";

    private static final String DEFAULT_PROJECT = "Default";

    protected static final String DEFAULT_DOMAIN = "Test domain";

    private static final String DEFAULT_VERSION = "vDefault";

    private static final String DEFAULT_TABLE_ONE = "Table One";
    private static final String DEFAULT_TABLE_TWO = "Table Two";
    private static final String DEFAULT_TABLE_THREE = "Table Three";
    private static final String DEFAULT_TABLE_FOUR = "Table Four";
    private static final String DEFAULT_TABLE_FIVE = "Table Five";
    private static final String DEFAULT_TABLE_SIX = "Table Six";
    private static final String DEFAULT_TABLE_SEVEN = "Table Seven";
    private static final String DEFAULT_TABLE_NULLABLE = "Table Nullable";
    private static final String DEFAULT_TTEST1 = "Table EfluidTest1";
    private static final String DEFAULT_TTEST2 = "Table EfluidTest2";
    private static final String DEFAULT_EFLUIDTESTNUMBER = "Table EfluidTestNumber";
    private static final String DEFAULT_TTESTMULTIDATATYPE = "Table EfluidTestMultiDataType";
    private static final String DEFAULT_EFLUIDTESTPKCOMPOSITE = "Table EfluidTestPkComposite";
    private static final String DEFAULT_EFLUIDTESTAUDIT = "Table EfluidTestAudit";
    private static final String DEFAULT_TTESTNULLLINK_SRC = "Table T_NULL_LINK_DEMO_SRC";
    private static final String DEFAULT_TTESTNULLLINK_DEST = "Table T_NULL_LINK_DEMO_DEST";

    private static final String DEFAULT_WHERE = "1=1";

    protected static ResultActions currentAction;

    protected static Exception currentException;

    protected static String currentStartPage;

    protected static Map<String, ExportImportResult<ExportFile>> currentExports = new HashMap<>();

    protected static DiffContentSearch currentSearch = new DiffContentSearch();

    protected static long startupTime;

    protected static BasicProfiler profiler;

    @Autowired
    private TweakedPreparationUpdater preparationUpdater;

    @Autowired
    protected ObjectMapper mapper;

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

    @Autowired
    protected ManagedQueriesGenerator queryGenerator;

    @Autowired
    private TransformerService transformerService;

    @Autowired
    private ManagedQueriesGenerator.QueryGenerationRules defaultRules;

    @Autowired
    private SwitchableLdapAccountProvider accountProvider;

    @Autowired
    private InMemoryDirectoryServer ldapServer;

    @Autowired
    protected ManagedValueConverter valueConverter;

    @Autowired
    protected DictionaryManagementService dictService;

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

        modelDatabase().initWizardData(user, newProject, Collections.singletonList(newDomain));

        this.dets.completeWizard();
    }

    /**
     *
     */
    protected void initMinimalWizzardData(List<String> projects) {

        resetAsyncProcess();
        resetDatabaseIdentifier();

        User user = initDefaultUser();
        Project newProject = project(projects.get(0));
        FunctionalDomain newDomain = initDefaultDomain(newProject);

        modelDatabase().initWizardData(user, newProject, Collections.singletonList(newDomain));

        for (int i = 1; i < projects.size(); i++) {
            newDomain = initDefaultDomain(newProject);
            modelDatabase().addProject(project(projects.get(i)), Collections.singletonList(newDomain));
        }

        this.dets.completeWizard();
    }

    /**
     *
     */
    protected void initMinimalWizzardDataWithDomains(List<String> domainNames) {

        resetAsyncProcess();
        resetDatabaseIdentifier();

        User user = initDefaultUser();
        Project newProject = initDefaultProject();

        modelDatabase().initWizardData(user, newProject,
                domainNames.stream().map(n -> domain(n, newProject)).collect(Collectors.toList()));

        this.dets.completeWizard();
    }

    /**
     *
     */
    protected void initCompleteDictionaryWith8Tables() {

        resetAsyncProcess();
        resetDatabaseIdentifier();

        User user = initDefaultUser();
        Project newProject = initDefaultProject();
        FunctionalDomain newDomain = initDefaultDomain(newProject);

        // Force cache
        this.desc.getTables();

        modelDatabase().initWizardData(user, newProject, Collections.singletonList(newDomain));

        this.dets.completeWizard();

        initDictionaryForDefaultVersionWithTables(newDomain, newProject, TABLE_ONE, TABLE_TWO, TABLE_THREE, TABLE_FOUR, TABLE_FIVE, TABLE_SIX, TABLE_SEVEN, TABLE_ALL_NULLABLE);
    }

    /**
     *
     */
    protected void initCompleteDictionaryWithEfluidTestTables() {

        resetAsyncProcess();
        resetDatabaseIdentifier();

        User user = initDefaultUser();
        Project newProject = initDefaultProject();
        FunctionalDomain newDomain = initDefaultDomain(newProject);

        // Force cache
        this.desc.getTables();

        modelDatabase().initWizardData(user, newProject, Collections.singletonList(newDomain));

        this.dets.completeWizard();

        initDictionaryForDefaultVersionWithTables(newDomain, newProject, TTEST1, TTEST2, EFLUIDTESTNUMBER, TTESTMULTIDATATYPE, EFLUIDTESTPKCOMPOSITE, TTESTNULLLINK_SRC, TTESTNULLLINK_DEST, EFLUIDTESTAUDIT);
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
    protected String getCurrentUserEmail() {

        User user = this.userHolder.getCurrentUser();

        return user != null ? user.getEmail() : null;
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

    protected void disableLdap() {
        this.accountProvider.useDatabase();
    }

    protected void enableLdap(String ldifContent, String searchBase, String loginAttribute, String mailAttribute) {

        // Init embedded ldap with ldif content
        try {
            this.ldapServer.importFromLDIF(true, new LDIFReader(new ByteArrayInputStream(ldifContent.getBytes(StandardCharsets.UTF_8))));
        } catch (LDAPException e) {
            throw new AssertionError("Cannot import specified ldif content", e);
        }

        // And define accountProvider with specified properties
        this.accountProvider.useLdap(searchBase, loginAttribute, mailAttribute);
    }

    /**
     * Check for a specified error type (for managed exceptions only)
     *
     * @param expected ErrorType to check
     */
    protected static void assertErrorMessageType(String expected) {

        Exception ex = currentException != null ? currentException : currentAction.andReturn().getResolvedException();
        assertThat(ex).describedAs("An error message was not returned by the application").isNotNull();
        assertThat(ex).isInstanceOf(ApplicationException.class);

        ApplicationException apx = (ApplicationException) ex;

        assertThat(apx.getError().name()).isEqualTo(expected);
    }

    /**
     * Check for a specified error payload (for managed exceptions only)
     *
     * @param expected payload to search
     */
    protected static void assertErrorMessagePayload(String expected) {

        Exception ex = currentException != null ? currentException : currentAction.andReturn().getResolvedException();
        assertThat(ex).describedAs("An error message was not returned by the application").isNotNull();
        assertThat(ex).isInstanceOf(ApplicationException.class);

        ApplicationException apx = (ApplicationException) ex;

        assertThat(apx.getPayload()).contains(expected);
    }

    /**
     * Check for a specified error (from exception message / payload)
     *
     * @param expected content to search for
     */
    protected static void assertErrorMessageContent(String expected) {
        Exception ex = currentException != null ? currentException : currentAction.andReturn().getResolvedException();
        assertThat(ex).describedAs("An error message was not returned by the application").isNotNull();
        if (ex instanceof ApplicationException) {
            ApplicationException apx = (ApplicationException) ex;

            if (!(apx.getPayload() != null && apx.getPayload().contains(expected) || apx.getMessage().contains(expected))) {
                throw new AssertionError("Expected message not found in Datagate Application Exception." +
                        " Expected \"" + expected + "\" found " +
                        "message=\"" + apx.getMessage() + "\" / payload=\"" + (apx.getPayload() != null ? apx.getPayload() : "N/A") + "\"");
            }
        } else {
            assertThat(ex.getMessage()).contains(expected);
        }
    }

    /**
     * Output for a specified error (from exception message / payload)
     */
    protected static void outputErrorMessageContent() {
        Exception ex = currentException != null ? currentException : currentAction.andReturn().getResolvedException();
        assertThat(ex).describedAs("An error message was not returned by the application").isNotNull();

        if (ex instanceof ApplicationException) {
            ApplicationException apx = (ApplicationException) ex;
            LOGGER.warn("Get ApplicationException : " + apx.getMessage() + "\" / payload=\"" + (apx.getPayload() != null ? apx.getPayload() : "N/A") + "\"", apx);
        } else {
            LOGGER.warn("Get standard exception " + ex.getMessage(), ex);
        }
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
     * Helper for direct export call regarding selection rules
     *
     * @param type                export range type
     * @param specifiedCommitUuid selected commit (can be null for "ALL")
     * @return export result
     */
    protected ExportImportResult<ExportFile> processCommitExportWithoutTransformerCustomization(CommitExportEditData.CommitSelectType type, UUID specifiedCommitUuid) {

        // We edit the export
        CommitExportEditData exportEdit = this.commitService.initCommitExport(type, specifiedCommitUuid);

        // From the edited data, create an export without transformer customization ...
        CommitExportDisplay exportDisplay = this.commitService.saveCommitExport(exportEdit);

        // ... And start it to get its content
        return this.commitService.processCommitExport(exportDisplay.getUuid());
    }

    /**
     *
     */
    protected void resetAsyncProcess() {
        this.asyncDriver.reset();
    }

    protected void postponeImportedPackageTime(LocalDateTime time) {
        this.preparationUpdater.setPostponeImportedPackageTime(time);
    }

    protected void resetPreparationUpdater() {
        this.preparationUpdater.setPostponeImportedPackageTime(null);
    }

    protected void resetDatabaseIdentifier() {
        this.databaseIdentifier.reset();
    }

    /**
     * Restore generator with default rules (for hook use)
     */
    protected void resetQueryGenerator() {
        this.queryGenerator.update(this.defaultRules);
    }

    /**
     * Init some queryGeneration rules from the default one, as updatable rules
     *
     * @return instance of DatasourceUtils.CustomQueryGenerationRules initialized and updatable for queryGenerator update
     */
    protected DatasourceUtils.CustomQueryGenerationRules initUpdatableRules() {

        DatasourceUtils.CustomQueryGenerationRules rules = new DatasourceUtils.CustomQueryGenerationRules();

        rules.setColumnNamesProtected(this.defaultRules.isColumnNamesProtected());
        rules.setDatabaseDateFormat(this.defaultRules.getDatabaseDateFormat());
        rules.setTableNamesProtected(this.defaultRules.isTableNamesProtected());
        rules.setJoinOnNullableKeys(this.defaultRules.isJoinOnNullableKeys());

        return rules;
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

    protected Transformer<?, ?> getTransformerByName(String name) {
        String type = this.transformerService.getAllTransformerTypes().stream()
                .filter(t -> t.getName().equals(name)).findFirst()
                .orElseThrow(() -> new AssertionError("Invalid transformer name " + name))
                .getType();

        return this.transformerService.loadTransformerByType(type);
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
     * @param contentType loaded type
     * @return content loaded
     * @throws Exception
     */
    protected final <T> T getContent(String url, Class<T> contentType) throws Exception {

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(url);

        if (currentStartPage != null) {
            builder.header("Referer", currentStartPage);
        }

        // Add user token anyway
        if (url.startsWith("/rest/")) {
            builder.param("token", getCurrentUserApiToken());
        }

        builder.accept(MediaType.APPLICATION_JSON_UTF8);

        return this.mapper.readValue(this.mockMvc.perform(builder).andReturn().getResponse().getContentAsString(), contentType);
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
     * @param contentType loaded type
     * @return content loaded
     * @throws Exception
     */
    protected final <T> T postContent(String url, Object requestBody, Class<T> contentType) throws Exception {

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(url);

        if (currentStartPage != null) {
            builder.header("Referer", currentStartPage);
        }

        // Add user token anyway
        if (url.startsWith("/rest/")) {
            builder.param("token", getCurrentUserApiToken());
        }

        builder.content(this.mapper.writeValueAsString(requestBody));
        builder.contentType(MediaType.APPLICATION_JSON_UTF8);
        builder.accept(MediaType.APPLICATION_JSON_UTF8);

        return this.mapper.readValue(this.mockMvc.perform(builder).andReturn().getResponse().getContentAsString(), contentType);
    }

    /**
     * <p>
     * Simplified post process with common rules :
     * <ul>
     * <li>Set the currentAction</li>
     * <li>Apply the given body</li>
     * <li>Take care of currentStartPage if any is set</li>
     * </ul>
     * </p>
     *
     * @param url
     * @param requestBody post body
     * @param params
     * @throws Exception
     */
    @SafeVarargs
    protected final void postWithBody(String url, final Object requestBody, final Associate<String, String>... params) throws Exception {
        post(url, Arrays.asList(params), requestBody);
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
        post(url, Arrays.asList(params), null);
    }

    /**
     * @param url
     * @param params
     * @throws Exception
     */
    protected final void post(String url, final PostParamSet params) throws Exception {
        post(url, params.getParams(), null);
    }

    /**
     * @param url
     * @param params
     * @param requestBody optional
     * @throws Exception
     */
    private void post(String url, Collection<Associate<String, String>> params, @Nullable Object requestBody) throws Exception {

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(url);

        for (Associate<String, String> param : params) {
            builder.param(param.getOne(), param.getTwo());
        }

        if (currentStartPage != null) {
            builder.header("Referer", currentStartPage);
        }

        if (requestBody != null) {
            builder.content(this.mapper.writeValueAsString(requestBody));
            builder.contentType(MediaType.APPLICATION_JSON_UTF8);
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
    protected static void initDefaultTables(FunctionalDomain domain, List<DictionaryEntry> tables, List<TableLink> links, String... tableNames) {
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
                    DictionaryEntry tableFour = table(DEFAULT_TABLE_FOUR, TABLE_FOUR, domain, "ln1.\"VALUE\" as ln_OTHER_TABLE_KEY , cur.\"CONTENT_TIME\", cur.\"CONTENT_INT\"", DEFAULT_WHERE, "KEY", STRING);
                    tables.add(tableFour);
                    links.add(DataGenerationUtils.link(tableFour, "OTHER_TABLE_KEY", "KEY", TABLE_ONE));
                    break;
                case TABLE_FIVE:
                    tables.add(table(DEFAULT_TABLE_FIVE, TABLE_FIVE, domain, "cur.\"DATA\", cur.\"SIMPLE\"", DEFAULT_WHERE, "KEY", PK_STRING));
                    break;
                case TABLE_SIX:
                    tables.add(table(DEFAULT_TABLE_SIX, TABLE_SIX, domain, "cur.\"TEXT\", cur.\"DATE\"", DEFAULT_WHERE, "IDENTIFIER", PK_ATOMIC));
                    break;
                case TABLE_ALL_NULLABLE:
                    tables.add(table(DEFAULT_TABLE_NULLABLE, TABLE_ALL_NULLABLE, domain, "cur.\"SOMETHING\", cur.\"VALUE\"", DEFAULT_WHERE, "BUSINESS_KEY", STRING));
                    break;
                case TABLE_SEVEN:
                    DictionaryEntry tableSeven = table(DEFAULT_TABLE_SEVEN, TABLE_SEVEN, domain, "ln1.\"VALUE\" as ln_OTHER_TABLE_VALUE , cur.\"VALUE\", cur.\"ENABLED\"", DEFAULT_WHERE, "BUSINESS_KEY", STRING);
                    tables.add(tableSeven);
                    links.add(DataGenerationUtils.link(tableSeven, "OTHER_TABLE_VALUE", "VALUE", TABLE_THREE));
                    break;
                case TTEST1:
                    tables.add(table(DEFAULT_TTEST1, TTEST1, domain, "cur.\"COL1\"", DEFAULT_WHERE, "ID", STRING));
                    break;
                case TTEST2:
                    tables.add(table(DEFAULT_TTEST2, TTEST2, domain, "cur.\"COL1\"", DEFAULT_WHERE, "ID", STRING));
                    break;
                case EFLUIDTESTNUMBER:
                    tables.add(table(DEFAULT_EFLUIDTESTNUMBER, EFLUIDTESTNUMBER, domain, "cur.\"COL1\", cur.\"COL2\"", DEFAULT_WHERE, "ID", STRING));
                    break;
                case EFLUIDTESTAUDIT:
                    tables.add(table(DEFAULT_EFLUIDTESTAUDIT, EFLUIDTESTAUDIT, domain, "cur.\"VALUE\", cur.\"ETAT_OBJET\", cur.\"DATE_SUPPRESSION\", cur.\"DATE_MODIFICATION\", cur.\"DATE_CREATION\", cur.\"ACTEUR_SUPPRESSION\", cur.\"ACTEUR_MODIFICATION\", cur.\"ACTEUR_CREATION\"", DEFAULT_WHERE, "ID", STRING));
                    break;
                case TTESTMULTIDATATYPE:
                    tables.add(table(DEFAULT_TTESTMULTIDATATYPE, TTESTMULTIDATATYPE, domain, "cur.\"COL1\", cur.\"COL2\", cur.\"COL3\", cur.\"COL4\", cur.\"COL5\", cur.\"COL6\", cur.\"COL7\"", DEFAULT_WHERE, "ID", STRING));
                    break;
                case EFLUIDTESTPKCOMPOSITE:
                    tables.add(table(DEFAULT_EFLUIDTESTPKCOMPOSITE, EFLUIDTESTPKCOMPOSITE, domain, "cur.\"COL1\"", DEFAULT_WHERE, "ID", STRING, "ID2", STRING));
                    break;
                case TTESTNULLLINK_SRC:
                    DictionaryEntry tableNullableLinkSrc = table(DEFAULT_TTESTNULLLINK_SRC, TTESTNULLLINK_SRC, domain, "ln1.\"TECH_KEY\" as ln_DEST_BIZ_KEY , cur.\"VALUE\"", DEFAULT_WHERE, "ID", STRING);
                    tables.add(tableNullableLinkSrc);
                    links.add(DataGenerationUtils.link(tableNullableLinkSrc, "DEST_BIZ_KEY", "BIZ_KEY", TTESTNULLLINK_DEST));
                    break;
                case TTESTNULLLINK_DEST:
                    tables.add(table(DEFAULT_TTESTNULLLINK_DEST, TTESTNULLLINK_DEST, domain, "cur.\"BIZ_KEY\", cur.\"CODE\"", DEFAULT_WHERE, "TECH_KEY", STRING));
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

    protected UUID getSavedCommitUuid() {

        assertRequestWasOk();

        UUID savedCommitUUID = (UUID) Objects.requireNonNull(currentAction.andReturn()
                .getModelAndView()).getModel().get("createdUUID");

        assertThat(savedCommitUUID).isNotNull();

        return savedCommitUUID;
    }

    protected CommitDetails getSavedCommit() {
        return this.commitService.getExistingCommitDetails(getSavedCommitUuid(), true);
    }

    protected static String dataKey(Map<String, String> line) {

        String rawKey = line.get("Key").trim();

        if (rawKey.charAt(0) == '"') {
            rawKey = rawKey.substring(1, rawKey.length() - 1);
        }

        switch (rawKey) {
            case "-empty char-":
                return "";
            case "-space-":
                return " ";

            case "-null-":
                return " ";
            default:
                return rawKey;
        }
    }

    protected static void assertDiffContentIsCompliant(DiffContentHolder<?> holder, DataTable data) {

        assertThat(holder.getDiffContent().size()).isEqualTo(data.asMaps().size());

        data.asMaps().forEach(l -> {

            String dataKey = dataKey(l);
            DictionaryEntrySummary table = holder.getReferencedTables().values().stream()
                    .filter(t -> t.getTableName().equals(l.get("Table")))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No referenced table \"" + l.get("Table") + "\" found in diff content"));

            PreparedIndexEntry index = holder.getDiffContent().stream().filter(i -> i.getDictionaryEntryUuid().equals(table.getUuid()) && i.getKeyValue().equals(dataKey))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Cannot find corresponding diff for table \"" + l.get("Table") + "\" and key \"" + l.get("Key") + "\""));

            IndexAction action = IndexAction.valueOf(l.get("Action"));
            assertThat(index.getAction()).isEqualTo(action);

            // No need to check payload in delete
            if (action != REMOVE) {
                assertThat(index.getHrPayload()).isEqualTo(l.get("Payload"));
            }
        });
    }

    /**
     * Check on encoded payload + previous payload
     *
     * @param content          any form of index
     * @param referencedTables associated tables
     * @param data
     */
    protected void assertIndexIsTechnicallyCompliant(Collection<? extends DiffLine> content, Map<UUID, DictionaryEntrySummary> referencedTables, DataTable data) {

        assertThat(content.size()).isEqualTo(data.asMaps().size());

        data.asMaps().forEach(l -> {

            String dataKey = dataKey(l);
            DictionaryEntrySummary table = referencedTables.values().stream()
                    .filter(t -> t.getTableName().equals(l.get("Table")))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No referenced table \"" + l.get("Table") + "\" found in diff content"));

            DiffLine index = content.stream().filter(i -> i.getDictionaryEntryUuid().equals(table.getUuid()) && i.getKeyValue().equals(dataKey))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Cannot find corresponding diff for table \"" + l.get("Table") + "\" and key \"" + l.get("Key") + "\""));

            IndexAction action = IndexAction.valueOf(l.get("Action"));
            assertThat(index.getAction()).isEqualTo(action);

            // No need to check payload in delete
            if (action != REMOVE) {
                assertThat(this.valueConverter.convertToHrPayload(index.getPayload(), null)).isEqualTo(l.get("Current payload"));
            }
            assertThat(this.valueConverter.convertToHrPayload(index.getPrevious(), null)).isEqualTo(l.get("Previous payload"));
        });
    }

    protected static void assertDiffContentSelect(DiffContentHolder<?> holder, DataTable data) {

        assertThat(holder.getDiffContent().size()).isEqualTo(data.asMaps().size());

        data.asMaps().forEach(l -> {

            String dataKey = dataKey(l);

            DictionaryEntrySummary table = holder.getReferencedTables().values().stream()
                    .filter(t -> t.getTableName().equals(l.get("Table")))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No referenced table \"" + l.get("Table") + "\" found in diff content"));

            PreparedIndexEntry index = holder.getDiffContent().stream().filter(i -> i.getDictionaryEntryUuid().equals(table.getUuid()) && i.getKeyValue().equals(dataKey))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Cannot find corresponding diff for table \"" + l.get("Table") + "\" and key \"" + l.get("Key") + "\""));

            IndexAction action = IndexAction.valueOf(l.get("Action"));
            assertThat(index.getAction()).isEqualTo(action);

            switch (l.get("Selection")) {
                case "selected":
                    assertThat(index.isSelected()).as("Selected for line on table \"" + l.get("Table") + "\" and key \"" + l.get("Key") + "\"").isTrue();
                    assertThat(index.isRollbacked()).as("Rollbacked for line on table \"" + l.get("Table") + "\" and key \"" + l.get("Key") + "\"").isFalse();
                    break;
                case "rollbacked":
                    assertThat(index.isSelected()).as("Selected for line on table \"" + l.get("Table") + "\" and key \"" + l.get("Key") + "\"").isFalse();
                    assertThat(index.isRollbacked()).as("Rollbacked for line on table \"" + l.get("Table") + "\" and key \"" + l.get("Key") + "\"").isTrue();
                    break;
                case "ignored":
                default:
                    assertThat(index.isSelected()).as("Selected for line on table \"" + l.get("Table") + "\" and key \"" + l.get("Key") + "\"").isFalse();
                    assertThat(index.isRollbacked()).as("Rollbacked for line on table \"" + l.get("Table") + "\" and key \"" + l.get("Key") + "\"").isFalse();
                    break;
            }
        });
    }

    protected static void assertDiffContentIsCompliantOrdered(List<? extends PreparedIndexEntry> diffLines, DataTable data) {

        assertThat(diffLines).hasSize(data.height() - 1);

        int i = 0;
        for (Map<String, String> dataline : data.asMaps()) {
            PreparedIndexEntry diff = diffLines.get(i);
            assertThat(diff.getTableName()).isEqualTo(dataline.get("Table"));
            assertThat(diff.getKeyValue()).isEqualTo(dataKey(dataline));
            assertThat(diff.getAction().name()).isEqualTo(dataline.get("Action"));
            if (diff.getAction() != REMOVE) {
                assertThat(diff.getHrPayload()).isEqualTo(dataline.get("Payload"));
            }
            i++;
        }
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
     * 2 PK
     *
     * @param name
     * @param tableName
     * @param domain
     * @param select
     * @param where
     * @param key
     * @param keyType
     * @param key2
     * @param keyType2
     * @return
     */
    protected static DictionaryEntry table(String name, String tableName, FunctionalDomain domain, String select, String where,
                                           String key, ColumnType keyType, String key2, ColumnType keyType2) {
        return DataGenerationUtils.entry(name, domain, select, tableName, where, key, keyType, key2, keyType2);
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

        assertThat(datas).isNotNull();
        assertThat(datas).hasSize(properties.size());
        assertThat(datas).allMatch(i -> properties.contains(propertyAccess.apply(i)));
    }


    protected static ExportImportResult<ExportFile> getSingleCurrentExport() {

        if (currentExports.size() == 0) {
            throw new AssertionError("Cannot import available source package if none defined first");
        }
        if (currentExports.size() > 1) {
            throw new AssertionError("Cannot import \"the available source package\" if more than one are specified. Use name to identify it");
        }

        return currentExports.values().iterator().next();
    }

    protected static ExportImportResult<ExportFile> getNamedExportOrSingleCurrentOne(String name) {
        if (currentExports.size() == 1) {
            return getSingleCurrentExport();
        }
        return currentExports.get(name);
    }

    @SuppressWarnings("unchecked")
    protected static <K> K getCurrentSpecifiedProperty(String propertyName, Class<K> type) {

        Object data = Objects.requireNonNull(currentAction.andReturn().getModelAndView()).getModel().get(propertyName);
        assertThat(data).isNotNull();
        assertThat(data).isInstanceOf(type);

        return (K) data;
    }

    @SuppressWarnings("unchecked")
    protected static <K> List<K> getCurrentSpecifiedPropertyList(String propertyName, Class<K> type) {

        Object data = Objects.requireNonNull(currentAction.andReturn().getModelAndView()).getModel().get(propertyName);
        assertThat(data).isNotNull();
        assertThat(data).isInstanceOf(List.class);

        return (List<K>) data;
    }

    /**
     * @param propertyName
     * @param propertyMatch
     * @param <K>
     */
    @SuppressWarnings("unchecked")
    protected static <K> void assertModelIsSpecifiedProperty(
            String propertyName,
            Class<K> type,
            Predicate<K> propertyMatch) {
        assertThat(getCurrentSpecifiedProperty(propertyName, type)).matches(propertyMatch);
    }

    /**
     * Must be missing
     *
     * @param propertyName
     */
    protected static void assertModelHasNoSpecifiedProperty(String propertyName) {
        if (currentAction.andReturn().getModelAndView() != null) {
            assertThat(currentAction.andReturn().getModelAndView().getModel().get(propertyName))
                    .isNull();
        }
    }


    /**
     * Must be missing
     *
     * @param propertyName
     */
    protected static void assertModelHasSpecifiedProperty(String propertyName) {
        assertThat(Objects.requireNonNull(currentAction.andReturn().getModelAndView()).getModel().get(propertyName))
                .isNotNull();
    }

    /**
     * Control json ignoring formating
     *
     * @param rawOne a json value
     * @param rawTwo another json value
     * @return true if equals
     */
    protected boolean jsonEquals(String rawOne, String rawTwo) {

        if (rawOne == null && rawTwo == null) {
            return true;
        }

        if (rawOne == null) {
            return false;
        }

        if (rawTwo == null) {
            return false;
        }

        try {
            // Drop formatting
            Map<?, ?> one = this.mapper.readValue(rawOne, new TypeReference<Map<Object, Object>>() {
            });
            Map<?, ?> two = this.mapper.readValue(rawTwo, new TypeReference<Map<Object, Object>>() {
            });

            assertThat(this.mapper.writeValueAsString(one)).isEqualTo(this.mapper.writeValueAsString(two));
        } catch (IOException i) {
            throw new AssertionError("Invalid json content", i);
        }

        return true;

    }

    protected static void startProfiling() {
        if (profiler != null) {
            profiler.stop();
        }

        profiler = new BasicProfiler();
        profiler.start();
    }

    protected static List<BasicProfiler.Stats> stopProfilingAndGetStats() {
        profiler.stop();
        return profiler.getValues();
    }

    /**
     * @param propertyName
     * @param matchers     for chained checks with assertj error support
     * @param <K>
     */
    @SuppressWarnings("unchecked")
    protected static <K> void assertModelIsSpecifiedProperty(
            String propertyName,
            Class<K> type,
            Consumer<ObjectAssert<K>>... matchers) {

        @SuppressWarnings("unchecked")
        Object data = Objects.requireNonNull(currentAction.andReturn().getModelAndView()).getModel().get(propertyName);

        assertThat(data).isNotNull();
        assertThat(data).isInstanceOf(type);

        K matched = (K) data;

        for (Consumer<ObjectAssert<K>> matcher : matchers) {
            matcher.accept(assertThat(matched));
        }
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

    /**
     * Very simple profiling support for some tests
     */
    protected static class BasicProfiler implements Runnable {

        private Thread th;
        private List<Stats> values = new ArrayList<>();
        boolean run;

        void start() {
            this.run = true;
            this.th = new Thread(this, "test-profiler");
            this.th.start();
        }

        void stop() {
            this.run = false;
        }

        public void run() {

            while (this.run) {
                this.values.add(
                        new Stats(
                                Runtime.getRuntime().freeMemory(),
                                Runtime.getRuntime().maxMemory(),
                                Runtime.getRuntime().totalMemory()
                        ));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Cannot stat");
                }
            }
        }

        public List<Stats> getValues() {
            return this.values;
        }

        public static class Stats {

            private final Long free;
            private final Long max;
            private final Long total;

            private Stats(Long free, Long max, Long total) {
                this.free = free;
                this.max = max;
                this.total = total;
            }

            public Long getFree() {
                return free;
            }

            public Long getMax() {
                return max;
            }

            public Long getTotal() {
                return total;
            }
        }

    }
}
