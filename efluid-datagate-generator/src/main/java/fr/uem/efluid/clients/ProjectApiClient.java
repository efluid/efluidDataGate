package fr.uem.efluid.clients;

import fr.uem.efluid.rest.v1.ProjectApi;
import fr.uem.efluid.rest.v1.model.ProjectDetailView;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

public class ProjectApiClient extends AbstractApiClient implements ProjectApi {

    /**
     * @param uri   associated entry point
     * @param token user technical token
     */
    public ProjectApiClient(String uri, String token) {
        super(uri, token);
    }

    @Override
    public List<ProjectDetailView> getAvailableProjectDetails() {
        return Arrays.asList(get("/projects/all", ProjectDetailView[].class));
    }

    @Override
    public ProjectDetailView getCurrentActiveProject() {
        return get("/projects/active", ProjectDetailView.class);
    }

    @Override
    public void setCurrentActiveProject(String projectUuid) {
        post("/projects/active?projectUuid=" + projectUuid, null, Void.class);
    }
}
