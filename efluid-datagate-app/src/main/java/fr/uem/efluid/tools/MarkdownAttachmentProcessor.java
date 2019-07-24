package fr.uem.efluid.tools;

import com.github.rjeschke.txtmark.Processor;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
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
	 * @see fr.uem.efluid.tools.AttachmentProcessor#formatForDisplay(fr.uem.efluid.tools.AttachmentProcessor.Compliant)
	 */
	@Override
	protected String formatForDisplay(Compliant att) {
		// Using a markdown processor
		return Processor.process(super.formatForDisplay(att));
	}

}
