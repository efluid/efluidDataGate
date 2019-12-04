package fr.uem.efluid.rest.v1;

import fr.uem.efluid.model.metas.ManagedModelDescription;
import fr.uem.efluid.rest.v1.model.ApplicationInfoView;
import fr.uem.efluid.rest.v1.model.AsyncProcessView;
import fr.uem.efluid.services.ApplicationDetailsService;
import fr.uem.efluid.services.types.ApplicationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@RestController
public class ApplicationApiController implements ApplicationApi {

    @Autowired
    private ApplicationDetailsService applicationDetailsService;

    /**
     * @see fr.uem.efluid.rest.v1.ApplicationApi#getCurrentInfo()
     */
    @Override
    public ApplicationInfoView getCurrentInfo() {

        ApplicationInfo info = this.applicationDetailsService.getInfo();
        ManagedModelDescription desc = this.applicationDetailsService.getCurrentModelId();
        return new ApplicationInfoView(info.getVersion(), info.getInstanceName(), desc != null ? desc.getIdentity() : null);
    }

    /**
     * @see fr.uem.efluid.rest.v1.ApplicationApi#getCurrentState()
     */
    @Override
    public String getCurrentState() {
        return "RUNNING";
    }

    @Override
    public List<AsyncProcessView> getCurrentProcesses() {
        return this.applicationDetailsService.getActiveAsyncProcess().stream()
                .map(p -> new AsyncProcessView(p.getIdentifier(), p.getDescription(), p.getCreatedTime(), p.hasSourceFailure(), p.getPercentDone()))
                .collect(Collectors.toList());
    }

    @Override
    public void killActiveProcess(String processUUID) {
        this.applicationDetailsService.killActiveAsyncProcess(UUID.fromString(processUUID));
    }
}
