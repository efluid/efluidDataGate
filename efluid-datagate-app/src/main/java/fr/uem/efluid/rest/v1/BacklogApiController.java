package fr.uem.efluid.rest.v1;

import fr.uem.efluid.rest.v1.model.CommitCreatedResultView;
import fr.uem.efluid.rest.v1.model.CommitPrepareDetailsView;
import fr.uem.efluid.rest.v1.model.CommitPrepareDetailsView.CommitPrepareTableView;
import fr.uem.efluid.rest.v1.model.StartedMergeView;
import fr.uem.efluid.services.CommitService;
import fr.uem.efluid.services.PilotableCommitPreparationService;
import fr.uem.efluid.services.types.*;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import fr.uem.efluid.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

/**
 * @author elecomte
 * @version 2
 * @since v0.0.1
 */
@RestController
public class BacklogApiController implements BacklogApi {

    @Autowired
    private PilotableCommitPreparationService pilotableCommitService;

    /**
     * @see fr.uem.efluid.rest.v1.BacklogApi#initPreparedCommit()
     */
    @Override
    public PreparationState initPreparedCommit() {

        PilotedCommitPreparation<?> prep = this.pilotableCommitService.startLocalCommitPreparation(false);
        return new PreparationState(prep.getStatus(), prep.getPercentDone());
    }

    /**
     * @see fr.uem.efluid.rest.v1.BacklogApi#uploadAndInitPreparedCommit(MultipartFile) ()
     */
    @Override
    public StartedMergeView uploadAndInitPreparedCommit(@RequestParam("file") MultipartFile file) throws ApplicationException {

        ExportImportResult<PilotedCommitPreparation<MergePreparedDiff>> result =
                this.pilotableCommitService.startMergeCommitPreparation(WebUtils.inputExportImportFile(file));

        ExportImportResult.ItemCount packgs = result.getCounts().get(CommitService.PCKG_ALL);

        return new StartedMergeView(
                packgs.getAdded(),
                new PreparationState(result.getResult().getStatus(), result.getResult().getPercentDone()),
                result.getResult().getCommitData().getAttachments().size(),
                result.getResult().getCommitData().getAttachments().stream()
                        .map(a -> new StartedMergeView.AttachementView(
                                a.getName(),
                                a.getType().name(),
                                a.getData().length)
                        ).collect(Collectors.toList())
        );
    }

    /**
     * @see fr.uem.efluid.rest.v1.BacklogApi#cancelPreparedCommit()
     */
    @Override
    public PreparationState cancelPreparedCommit() {

        int currentPercent = 0;
        PilotedCommitPreparation<?> prep =  this.pilotableCommitService.getCurrentCommitPreparation();

        // Update current preparation as canceled
        if (prep != null) {

            // But display percent done will canceling
            currentPercent = prep.getPercentDone();
            this.pilotableCommitService.cancelCommitPreparation();
        }

        return new PreparationState(PilotedCommitStatus.CANCEL, currentPercent) ;
    }

    /**
     * @see fr.uem.efluid.rest.v1.BacklogApi#getCurrentPreparedCommitState()
     */
    @Override
    public PreparationState getCurrentPreparedCommitState() {
        return this.pilotableCommitService.getCurrentCommitPreparationState();
    }

    /**
     * @see fr.uem.efluid.rest.v1.BacklogApi#getCurrentPreparedCommitDetails()
     */
    @Override
    public CommitPrepareDetailsView getCurrentPreparedCommitDetails() {

        PilotedCommitPreparation<?> preparation = this.pilotableCommitService.getCurrentCommitPreparation();

        if (preparation == null || preparation.getStatus() != PilotedCommitStatus.COMMIT_CAN_PREPARE) {
            return null;
        }

        CommitPrepareDetailsView result = new CommitPrepareDetailsView();

        // Details on content by table
        if (preparation.getDomains() != null) {
            result.setDetails(
                    preparation.getDomains().stream()
                            .filter(d -> d.getPreparedContent() != null)
                            .flatMap(d -> d.getPreparedContent().stream()
                                    .map(c -> {
                                        CommitPrepareTableView table = new CommitPrepareTableView();
                                        table.setDomain(d.getDomainName());
                                        table.setTable(c.getDictionaryEntryTableName());
                                        table.setIndexRowCount(c.getDiff().size());
                                        table.setParameter(c.getDictionaryEntryName());
                                        return table;
                                    }))
                            .collect(Collectors.toList()));
        }

        // Total count for quick checking
        result.setIndexRowCount(preparation.getTotalCount());

        return result;
    }

    /**
     * @see fr.uem.efluid.rest.v1.BacklogApi#validateCurrentPreparedCommit(java.lang.String)
     */
    @Override
    public CommitCreatedResultView validateCurrentPreparedCommit(String commitComment) {

        PilotedCommitPreparation<?> preparation = this.pilotableCommitService.getCurrentCommitPreparation();

        if (preparation == null || preparation.getStatus() != PilotedCommitStatus.COMMIT_CAN_PREPARE) {
            throw new ApplicationException(ErrorType.PREPARATION_NOT_READY, "Preparation of active commit is not ready");
        }

        // Finalize preparation
        this.pilotableCommitService.finalizeWizzardCommitPreparation(commitComment);

        // Complete and gather result data
        CommitCreatedResultView result = new CommitCreatedResultView();
        result.setCommitDomainNames(preparation.getSelectedFunctionalDomainNames());
        result.setIndexRowCount(preparation.getTotalCount());
        result.setCommitUuid(this.pilotableCommitService.saveCommitPreparation());

        return result;
    }
}
