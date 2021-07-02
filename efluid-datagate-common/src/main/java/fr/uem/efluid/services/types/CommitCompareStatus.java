package fr.uem.efluid.services.types;

/**
 * State for a commit compare process
 *
 * @author elecomte
 * @version 1
 * @since v3.1.11
 */
public enum CommitCompareStatus {

    NOT_LAUNCHED,
    COMPARE_RUNNING,
    COMPLETED,
    FAILED;

}