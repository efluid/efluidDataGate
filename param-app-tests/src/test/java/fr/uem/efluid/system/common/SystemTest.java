package fr.uem.efluid.system.common;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.services.ApplicationDetailsService;
import fr.uem.efluid.services.SecurityService;
import fr.uem.efluid.system.stubs.ModelDatabaseInitializer;
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

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	private ModelDatabaseInitializer model;

	@Autowired
	private SecurityService sec;

	@Autowired
	private ApplicationDetailsService dets;
	
	/**
	 * 
	 */
	protected void initMinimalWizzardData() {

		User user = initNewUser("any");
		Project newProject = initNewProject("Default");
		FunctionalDomain newDomain = initNewDomain("Test domain", newProject);

		this.model.initWizzardData(user, Arrays.asList(newProject), newDomain);
		
		this.dets.completeWizzard();
	}

	/**
	 * @return
	 */
	protected String getCurrentUserLogin() {
		return this.sec.getCurrentUserDetails().getLogin();
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

		String link = DataSetHelper.REV_SITEMAP.get(pageName);

		if (link == null) {
			throw new AssertionFailedError("No specified link for page name " + pageName + ". Check fixtures / gherkin");
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
	protected static User initNewUser(String login) {

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
	protected static FunctionalDomain initNewDomain(String domain, Project project) {
		return DataGenerationUtils.domain(domain, project);
	}

	/**
	 * @param project
	 * @return
	 */
	protected static Project initNewProject(String project) {
		return DataGenerationUtils.project(project);
	}

	protected static String cleanUserParameter(String param) {
		String cleaned = param.replace("user", "").trim();

		if (cleaned.equals("any unauthenticated")) {
			return null;
		}

		return cleaned;
	}
}
