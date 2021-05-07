package fr.uem.efluid.services.types;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import fr.uem.efluid.model.entities.LobProperty;
import fr.uem.efluid.utils.SharedOutputInputUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class LobPropertyPackage extends SharedPackage<LobProperty> {

	private List<Path> lobFiles = new ArrayList<>();

	/**
	 * @param name
	 * @param exportDate
	 */
	public LobPropertyPackage(String name, LocalDateTime exportDate) {
		super(name, exportDate);
	}

	/**
	 * @return
	 */
	@Override
	public String getVersion() {
		return "1";
	}

	/**
	 * @return
	 */
	@Override
	protected LobProperty initContent() {
		return new LobProperty();
	}

	/**
	 * @param rawContent
	 * @return
	 * @see fr.uem.efluid.services.types.SharedPackage#deserializeOne(java.lang.String)
	 */
	@Override
	public LobProperty deserializeOne(String rawContent) {
		// Update raw relative path to absolute path
		return super.deserializeOne(getUncompressPath().toString() + "/" + rawContent);
	}

	/**
	 * @param content
	 * @return
	 * @see fr.uem.efluid.services.types.SharedPackage#serializeOne(fr.uem.efluid.model.Shared)
	 */
	@Override
	protected String serializeOne(LobProperty content) {
		String filename = super.serializeOne(content);
		// Move generated file to TMP folder for inclusion in zip
		this.lobFiles.add(SharedOutputInputUtils.repatriateTmpFile(filename, getUncompressPath()));
		return filename;
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.SharedPackage#getComplementaryFiles()
	 */
	@Override
	public List<Path> getComplementaryFiles() {
		return this.lobFiles;
	}
}
