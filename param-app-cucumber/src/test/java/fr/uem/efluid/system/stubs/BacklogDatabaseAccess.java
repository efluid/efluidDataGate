package fr.uem.efluid.system.stubs;

import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.LobProperty;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.repositories.AttachmentRepository;
import fr.uem.efluid.model.repositories.CommitRepository;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.model.repositories.LobPropertyRepository;
import fr.uem.efluid.services.types.CommitDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@Transactional
public class BacklogDatabaseAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(BacklogDatabaseAccess.class);

    @Autowired
    private CommitRepository commits;

    @Autowired
    private IndexRepository index;

    @Autowired
    private AttachmentRepository attachs;

    @Autowired
    private LobPropertyRepository lobs;

    @Autowired
    private EntityManager em;

    public List<LobProperty> loadCommitLobs(CommitDetails details) {
        return this.lobs.findByCommitUuidIn(Collections.singletonList(details.getUuid()));
    }

    public UUID searchCommitWithName(Project project, String name) {
        Commit commit = this.commits.findByProject(project).stream()
                .filter(c -> c.getComment().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No commit found for name " + name + " in specified current project"));

        commit.getIndex().forEach(i -> i.getPayload());

        this.em.detach(commit);

        return commit.getUuid();
    }

    public void dropBacklog() {

        LOGGER.info("Begin backlog drop");

        Commit def = this.commits.findAll().get(0);

        this.index.findAll().forEach(i -> {i.setCommit(def); this.index.save(i);});

        this.lobs.deleteAll();
        this.attachs.deleteAll();
        this.index.deleteAll();
        this.commits.deleteAll();
    }
}