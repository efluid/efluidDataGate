package fr.uem.efluid.rest.v1;

import org.springframework.web.bind.annotation.RestController;

import fr.uem.efluid.rest.v1.model.CommitCreatedResultView;
import fr.uem.efluid.services.types.PilotedCommitStatus;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@RestController
public class BacklogApiController implements BacklogApi {

	/**
	 * @return
	 * @see fr.uem.efluid.rest.v1.BacklogApi#initPreparedCommit()
	 */
	@Override
	public PilotedCommitStatus initPreparedCommit() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return
	 * @see fr.uem.efluid.rest.v1.BacklogApi#getCurrentPreparedCommitStatus()
	 */
	@Override
	public PilotedCommitStatus getCurrentPreparedCommitStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param commitComment
	 * @return
	 * @see fr.uem.efluid.rest.v1.BacklogApi#validateCurrentPreparedCommit(java.lang.String)
	 */
	@Override
	public CommitCreatedResultView validateCurrentPreparedCommit(String commitComment) {

		// TODO Auto-generated method stub
		return null;
	}

}
