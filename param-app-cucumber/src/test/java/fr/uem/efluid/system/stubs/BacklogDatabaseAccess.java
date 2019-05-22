package fr.uem.efluid.system.stubs;

import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.LobProperty;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.repositories.CommitRepository;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.model.repositories.LobPropertyRepository;
import fr.uem.efluid.services.types.CommitDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BacklogDatabaseAccess {

    @Autowired
    private CommitRepository commits;

    @Autowired
    private IndexRepository index;

    @Autowired
    private LobPropertyRepository lobs;

    public List<LobProperty> loadCommitLobs(CommitDetails details) {
        return this.lobs.findByCommit(new Commit(details.getUuid()));
    }

    public Commit searchCommitWithName(Project project, String name) {
        return this.commits.findByProject(project).stream()
                .filter(c -> c.getComment().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No commit found for name " + name + " in specified current project"));
    }
}
