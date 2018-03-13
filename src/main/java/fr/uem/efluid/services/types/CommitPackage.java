package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.services.ExportImportService.ExportImportPackage;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class CommitPackage extends ExportImportPackage<Commit> {

	/**
	 * @param name
	 * @param exportDate
	 */
	public CommitPackage(String name, LocalDateTime exportDate) {
		super(name, exportDate);
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.ExportImportPackage#getVersion()
	 */
	@Override
	public String getVersion() {
		return "2";
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.ExportImportPackage#initContent()
	 */
	@Override
	protected Commit initContent() {
		return new Commit();
	}

}
