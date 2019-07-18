package fr.uem.efluid.tools;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
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
	 * @see fr.uem.efluid.tools.AttachmentProcessor#formatForDisplay(fr.uem.efluid.tools.AttachmentProcessor.Compliant)
	 */
	@Override
	protected String formatForDisplay(Compliant att) {
		// Basic text line formating. Nothing more
		return super.formatForDisplay(att).replaceAll("\n", "<br/>");
	}

}
