package fr.uem.efluid.system.tests.fixtures;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import fr.uem.efluid.services.CommitService;
import fr.uem.efluid.services.PilotableCommitPreparationService;
import fr.uem.efluid.services.types.*;
import fr.uem.efluid.system.common.SystemTest;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

import static fr.uem.efluid.services.types.PilotedCommitStatus.NOT_LAUNCHED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
public class CommitFixtures extends SystemTest {

    @Autowired
    private PilotableCommitPreparationService prep;

    @Autowired
    private CommitService commit;


    @Then("^the commit \"(.*)\" is added to commit list for current project$")
    public void then_commit_is_added_with_comment(String comment) {

        CommitDetails commit = getSavedCommit();

        assertThat(commit).isNotNull();
        assertThat(commit.getComment()).isEqualTo(comment);
    }

    private CommitDetails getSavedCommit() {

        assertRequestWasOk();

        UUID savedCommitUUID = (UUID) currentAction.andReturn()
                .getModelAndView().getModel().get("createdUUID");

        assertThat(savedCommitUUID).isNotNull();

        return this.commit.getExistingCommitDetails(savedCommitUUID);
    }

}
