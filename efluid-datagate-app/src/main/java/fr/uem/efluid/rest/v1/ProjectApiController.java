package fr.uem.efluid.rest.v1;

import fr.uem.efluid.rest.v1.model.ProjectDetailView;
import fr.uem.efluid.services.ProjectManagementService;
import fr.uem.efluid.services.types.ProjectData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author elecomte
 * @version 1
 * @since v0.2.0
 */
@RestController
public class ProjectApiController implements ProjectApi {

    @Autowired
    private ProjectManagementService projectService;

    /**
     * @see fr.uem.efluid.rest.v1.ProjectApi#getAvailableProjectDetails()
     */
    @Override
    public List<ProjectDetailView> getAvailableProjectDetails() {
        return this.projectService.getPreferedProjectsForCurrentUser().stream()
                .map(ProjectData::toView)
                .collect(Collectors.toList());
    }

    /**
     * @see fr.uem.efluid.rest.v1.ProjectApi#getCurrentActiveProject()
     */
    @Override
    public ProjectDetailView getCurrentActiveProject() {
        return ProjectData.toView(this.projectService.getCurrentSelectedProject());
    }

    /**
     * @see fr.uem.efluid.rest.v1.ProjectApi#setCurrentActiveProject(String)
     */
    @Override
    public void setCurrentActiveProject(String projectUuid) {
        this.projectService.selectProject(UUID.fromString(projectUuid));
    }

}
