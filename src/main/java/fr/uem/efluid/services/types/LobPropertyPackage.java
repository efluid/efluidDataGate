package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.entities.LobProperty;
import fr.uem.efluid.services.ExportImportService.ExportImportPackage;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class LobPropertyPackage extends ExportImportPackage<LobProperty> {

	/**
	 * @param name
	 * @param exportDate
	 */
	public LobPropertyPackage(String name, LocalDateTime exportDate) {
		super(name, exportDate);
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.ExportImportPackage#getVersion()
	 */
	@Override
	public String getVersion() {
		return "1";
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.ExportImportPackage#initContent()
	 */
	@Override
	protected LobProperty initContent() {
		return new LobProperty();
	}

	/**
	 * @param rawContent
	 * @return
	 * @see fr.uem.efluid.services.ExportImportService.ExportImportPackage#deserializeOne(java.lang.String)
	 */
	@Override
	protected LobProperty deserializeOne(String rawContent) {
		// Update raw relative path to absolute path
		return super.deserializeOne(getUncompressPath().toString() + "/" + rawContent);
	}
}
