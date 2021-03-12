package fr.uem.efluid.model;

/**
 * Content identified with its payloads
 */
public interface DiffPayloads extends ContentLine {

    /**
     * Previous content managed before the content payload
     *
     * @return
     */
    String getPrevious();

}
