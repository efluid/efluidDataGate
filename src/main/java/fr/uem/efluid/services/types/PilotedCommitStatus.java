package fr.uem.efluid.services.types;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public enum PilotedCommitStatus {

	NOT_LAUNCHED,
	DIFF_RUNNING,
	CANCEL,
	COMMIT_CAN_PREPARE,
	COMMIT_PREPARED;
}
