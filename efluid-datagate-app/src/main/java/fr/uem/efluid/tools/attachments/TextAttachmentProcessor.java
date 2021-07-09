package fr.uem.efluid.tools.attachments;

import fr.uem.efluid.utils.DisplayContentUtils;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
public class TextAttachmentProcessor extends AttachmentProcessor {

    /**
     *
     */
    public TextAttachmentProcessor() {
        super();
    }

    /**
     * @param att
     * @return
     * @see AttachmentProcessor#formatForDisplay(AttachmentProcessor.Compliant)
     */
    @Override
    protected String formatForDisplay(Compliant att) {
        // Basic text line formating. Nothing more
        return DisplayContentUtils.renderTxt(att.getData());
    }

}
