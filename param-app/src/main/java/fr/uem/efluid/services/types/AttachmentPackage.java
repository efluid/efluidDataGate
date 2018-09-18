package fr.uem.efluid.services.types;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fr.uem.efluid.model.entities.Attachment;
import fr.uem.efluid.utils.SharedOutputInputUtils;

/**
 * <p>
 * Attachment uses a combined file + json data for export / import
 * </p>
 * 
 * @author elecomte
 * @since v2.0.0
 * @version 1
 */
public class AttachmentPackage extends SharedPackage<Attachment> {

	private List<Path> attFiles = new ArrayList<>();

	/**
	 * @param name
	 * @param exportDate
	 */
	public AttachmentPackage(String name, LocalDateTime exportDate) {
		super(name, exportDate);
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.SharedPackage#getVersion()
	 */
	@Override
	public String getVersion() {
		return "1";
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.SharedPackage#initContent()
	 */
	@Override
	protected Attachment initContent() {
		return new Attachment();
	}

	/**
	 * @param rawContent
	 * @return
	 * @see fr.uem.efluid.services.types.SharedPackage#deserializeOne(java.lang.String)
	 */
	@Override
	protected Attachment deserializeOne(String rawContent) {
		// For Attachment deser we use "mixed form" for content : the uncompress path
		// (where attachment files are uncompressed) is associated to content for inline
		// processing in deser process.
		return super.deserializeOne(SharedOutputInputUtils.mergeValues(getUncompressPath().toString(), rawContent));
	}

	/**
	 * @param content
	 * @return
	 * @see fr.uem.efluid.services.types.SharedPackage#serializeOne(fr.uem.efluid.model.Shared)
	 */
	@Override
	protected String serializeOne(Attachment content) {

		// Attachment serial. uses a mixed content result
		String[] mixedContent = SharedOutputInputUtils.splitValues(super.serializeOne(content));

		// Move generated file to TMP folder for inclusion in zip
		this.attFiles.add(SharedOutputInputUtils.repatriateTmpFile(mixedContent[0], getUncompressPath()));

		// In pak file will use only json part
		return mixedContent[1];
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.SharedPackage#getComplementaryFiles()
	 */
	@Override
	public List<Path> getComplementaryFiles() {
		return this.attFiles;
	}

	/**
	 * @return
	 */
	public List<AttachmentLine> toAttachmentLines() {
		return getContent().stream().map(c -> {
			AttachmentLine line = new AttachmentLine();
			line.setUuid(c.getUuid());
			line.setName(c.getName());
			line.setType(c.getType());
			line.setTmpPath(getUncompressPath() + "/" + c.getTmpPath());
			line.setImportedTime(LocalDateTime.now());
			return line;
		}).collect(Collectors.toList());
	}
}
