package fr.uem.efluid.rest.v1;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import fr.uem.efluid.rest.v1.model.CommitCreatedResultView;
import fr.uem.efluid.rest.v1.model.CommitPrepareDetailsView;
import fr.uem.efluid.rest.v1.model.CommitPrepareDetailsView.CommitPrepareTableView;
import fr.uem.efluid.services.PilotableCommitPreparationService;
import fr.uem.efluid.services.types.PilotedCommitPreparation;
import fr.uem.efluid.services.types.PilotedCommitStatus;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@RestController
public class BacklogApiController implements BacklogApi {

	@Autowired
	private PilotableCommitPreparationService pilotableCommitService;

	/**
	 * @return
	 * @see fr.uem.efluid.rest.v1.BacklogApi#initPreparedCommit()
	 */
	@Override
	public PilotedCommitStatus initPreparedCommit() {

		return this.pilotableCommitService.startLocalCommitPreparation(false).getStatus();
	}

	/**
	 * @return
	 * @see fr.uem.efluid.rest.v1.BacklogApi#cancelPreparedCommit()
	 */
	@Override
	public PilotedCommitStatus cancelPreparedCommit() {

		// Update current preparation as canceled
		if (this.pilotableCommitService.getCurrentCommitPreparation() != null) {
			this.pilotableCommitService.cancelCommitPreparation();
		}

		return PilotedCommitStatus.CANCEL;
	}

	/**
	 * @return
	 * @see fr.uem.efluid.rest.v1.BacklogApi#getCurrentPreparedCommitStatus()
	 */
	@Override
	public PilotedCommitStatus getCurrentPreparedCommitStatus() {
		return this.pilotableCommitService.getCurrentCommitPreparationStatus();
	}

	/**
	 * @return
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
	 * @param commitComment
	 * @return
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
