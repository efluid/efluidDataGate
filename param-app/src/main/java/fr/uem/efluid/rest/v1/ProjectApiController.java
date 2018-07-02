package fr.uem.efluid.rest.v1;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import fr.uem.efluid.rest.v1.model.ProjectDetailView;
import fr.uem.efluid.services.ProjectManagementService;
import fr.uem.efluid.services.types.ProjectData;

/**
 * @author elecomte
 * @since v0.2.0
 * @version 1
 */
@RestController
public class ProjectApiController implements ProjectApi {

	@Autowired
	private ProjectManagementService projectService;

	/**
	 * @return
	 * @see fr.uem.efluid.rest.v1.ProjectApi#getAvailableProjectDetails()
	 */
	@Override
	public List<ProjectDetailView> getAvailableProjectDetails() {
		return this.projectService.getPreferedProjectsForCurrentUser().stream()
				.map(ProjectData::toView)
				.collect(Collectors.toList());
	}

	/**
	 * @return
	 * @see fr.uem.efluid.rest.v1.ProjectApi#getCurrentActiveProject()
	 */
	@Override
	public ProjectDetailView getCurrentActiveProject() {
		return ProjectData.toView(this.projectService.getCurrentSelectedProject());
	}

	/**
	 * @param projectUuid
	 * @see fr.uem.efluid.rest.v1.ProjectApi#setCurrentActiveProject(java.util.UUID)
	 */
	@Override
	public void setCurrentActiveProject(String projectUuid) {
		this.projectService.selectProject(UUID.fromString(projectUuid));
	}

}
