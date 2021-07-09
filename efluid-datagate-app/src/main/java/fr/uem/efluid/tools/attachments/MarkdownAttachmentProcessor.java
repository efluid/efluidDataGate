package fr.uem.efluid.tools.attachments;

import fr.uem.efluid.utils.DisplayContentUtils;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
public class MarkdownAttachmentProcessor extends AttachmentProcessor {

    /**
     *
     */
    public MarkdownAttachmentProcessor() {
        super();
    }

    /**
     * @param att
     * @return
     * @see AttachmentProcessor#formatForDisplay(AttachmentProcessor.Compliant)
     */
    @Override
    protected String formatForDisplay(Compliant att) {
        // Using a markdown processor
        return DisplayContentUtils.renderMarkdown(att.getData());
    }

}
