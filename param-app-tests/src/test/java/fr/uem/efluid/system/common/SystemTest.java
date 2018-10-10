package fr.uem.efluid.system.common;

import static fr.uem.efluid.ColumnType.PK_STRING;
import static fr.uem.efluid.ColumnType.STRING;
import static fr.uem.efluid.system.stubs.ManagedDatabaseAccess.TABLE_ONE;
import static fr.uem.efluid.system.stubs.ManagedDatabaseAccess.TABLE_THREE;
import static fr.uem.efluid.system.stubs.ManagedDatabaseAccess.TABLE_TWO;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.entities.Version;
import fr.uem.efluid.security.UserHolder;
import fr.uem.efluid.services.ApplicationDetailsService;
import fr.uem.efluid.services.ProjectManagementService;
import fr.uem.efluid.system.stubs.ManagedDatabaseAccess;
import fr.uem.efluid.system.stubs.ModelDatabaseAccess;
import fr.uem.efluid.system.stubs.TweakedAsyncDriver;
import fr.uem.efluid.system.stubs.TweakedDatabaseIdentifier;
import fr.uem.efluid.utils.Associate;
import fr.uem.efluid.utils.DataGenerationUtils;
import junit.framework.AssertionFailedError;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@SpringBootTest(classes = { SystemTestConfig.class }, webEnvironment = WebEnvironment.DEFINED_PORT)
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

	private static final String DEFAULT_WHERE = "1=1";

	protected static ResultActions currentAction;

	protected static String currentStartPage;

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	private ManagedDatabaseAccess managed;

	@Autowired
	private ModelDatabaseAccess model;

	@Autowired
	private ApplicationDetailsService dets;

	@Autowired
	private UserHolder userHolder;

	@Autowired
	private ProjectManagementService projectMgmt;

	@Autowired
	private Config securityConfig;

	@Autowired
	private TweakedAsyncDriver asyncDriver;

	@Autowired
	private TweakedDatabaseIdentifier databaseIdentifier;

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

		modelDatabase().initWizzardData(user, newProject, Arrays.asList(newDomain));

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
	protected void initCompleteDictionaryWith3Tables() {

		resetAsyncProcess();
		resetDatabaseIdentifier();

		User user = initDefaultUser();
		Project newProject = initDefaultProject();
		FunctionalDomain newDomain = initDefaultDomain(newProject);

		modelDatabase().initWizzardData(user, newProject, Arrays.asList(newDomain));

		this.dets.completeWizzard();

		modelDatabase().initDictionary(initDefaultTables(newDomain), initDefaultVersion(newProject));
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
	 * @param params
	 * @throws Exception
	 */
	protected final void get(String url, Object... args) throws Exception {

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(url, args);

		if (currentStartPage != null) {
			builder.header("Referer", currentStartPage);
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
	private final void post(String url, Collection<Associate<String, String>> params) throws Exception {

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
	private static List<DictionaryEntry> initDefaultTables(FunctionalDomain domain) {
		return Arrays.asList(
				table(DEFAULT_TABLE_ONE, TABLE_ONE, domain, "\"PRESET\", \"SOMETHING\"", DEFAULT_WHERE, "VALUE", STRING),
				table(DEFAULT_TABLE_TWO, TABLE_TWO, domain, "\"VALUE\", \"OTHER\"", DEFAULT_WHERE, "KEY", PK_STRING),
				table(DEFAULT_TABLE_THREE, TABLE_THREE, domain, "\"OTHER\"", DEFAULT_WHERE, "VALUE", STRING));
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
	protected static Associate<String, String> p(String name, String value) {
		return Associate.of(name, value);
	}

	protected static PostParamSet postParams() {
		return new PostParamSet();
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
		private static final String propName(Method method) {
			if (method.getName().startsWith("get")) {
				return method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
			}
			return method.getName().substring(2, 3).toLowerCase() + method.getName().substring(3);

		}
	}
}
