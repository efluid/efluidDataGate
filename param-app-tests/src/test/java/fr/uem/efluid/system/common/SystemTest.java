package fr.uem.efluid.system.common;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
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

import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.security.UserHolder;
import fr.uem.efluid.services.ApplicationDetailsService;
import fr.uem.efluid.services.ProjectManagementService;
import fr.uem.efluid.system.stubs.ModelDatabaseInitializer;
import fr.uem.efluid.utils.Associate;
import fr.uem.efluid.utils.DataGenerationUtils;
import junit.framework.AssertionFailedError;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@SpringBootTest(classes = { SystemTestConfig.class }, webEnvironment = WebEnvironment.DEFINED_PORT)
@ContextConfiguration
@AutoConfigureMockMvc
public abstract class SystemTest {

	protected static ResultActions currentAction;

	protected static String currentStartPage;

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	private ModelDatabaseInitializer model;

	@Autowired
	private ApplicationDetailsService dets;

	@Autowired
	private UserHolder userHolder;

	@Autowired
	private ProjectManagementService projectMgmt;

	/**
	 * 
	 */
	@Before
	public void setup() {

		currentAction = null;
		currentStartPage = null;

		resetAuthentication();
	}

	/**
	 * 
	 */
	protected void initMinimalWizzardData() {

		User user = user("any");
		Project newProject = project("Default");
		FunctionalDomain newDomain = domain("Test domain", newProject);

		this.model.initWizzardData(user, Arrays.asList(newProject), Arrays.asList(newDomain));

		this.dets.completeWizzard();
	}

	/**
	 * 
	 */
	protected void initMinimalWizzardDataWithDomains(List<String> domainNames) {

		User user = user("any");
		Project newProject = project("Default");

		this.model.initWizzardData(user, Arrays.asList(newProject),
				domainNames.stream().map(n -> domain(n, newProject)).collect(Collectors.toList()));

		this.dets.completeWizzard();
	}

	/**
	 * 
	 */
	protected void implicitlyAuthenticatedAndOnPage(String page) {

		// Initialized
		initMinimalWizzardData();

		// Authenticated as "any"
		this.userHolder.setCurrentUser(user("any"));

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

	protected void resetAuthentication() {

		// Reset auth
		this.userHolder.setCurrentUser(null);
		
		this.getCurrentUserLogin();
		this.userHolder.setWizzardUser(null);
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
	 * @param domain
	 * @param project
	 * @return
	 */
	protected static FunctionalDomain domain(String domain, Project project) {
		return DataGenerationUtils.domain(domain, project);
	}

	/**
	 * @param project
	 * @return
	 */
	protected static Project project(String project) {
		return DataGenerationUtils.project(project);
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

}
