package fr.uem.efluid.cucumber.stubs;

import fr.uem.efluid.services.PilotableCommitPreparationService;
import fr.uem.efluid.services.types.PilotedCommitPreparation;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.services.types.PreparedMergeIndexEntry;

import java.time.LocalDateTime;
import java.util.UUID;

public class TweakedPreparationUpdater implements PilotableCommitPreparationService.PreparationUpdater {

    // In some tests we want to make it like if the imported package was created AFTER the last "destination" environment commit
    private LocalDateTime postponedImportedPackageTime;

    @Override
    public void completeForDiff(PilotedCommitPreparation<PreparedIndexEntry> preparation, UUID projectUUID) {

    }

    @Override
    public void completeForMerge(PilotedCommitPreparation<PreparedMergeIndexEntry> preparation, UUID projectUUID) {
        if (this.postponedImportedPackageTime != null) {
            preparation.getCommitData().setRangeStartTime(LocalDateTime.now().plusMinutes(1));
        }
    }

    // Put null to disable
    public void setPostponeImportedPackageTime(LocalDateTime time) {
        this.postponedImportedPackageTime = time;
    }
}
