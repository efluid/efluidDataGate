package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.shared.ExportAwareProject;
import fr.uem.efluid.model.shared.ExportAwareVersion;

/**
 * <p>
 * For packaged exported dictionary version
 * </p>
 * 
 * @author elecomte
 * @since v0.2.0
 * @version 1
 */
public abstract class VersionExportPackage<D extends ExportAwareVersion<? extends ExportAwareProject>>
		extends SharedPackage<D> {

	public static final String VERSIONS_EXPORT = "full-versions";
	public static final String PARTIAL_VERSIONS_EXPORT = "partial-versions";

	/**
	 * @param name
	 * @param exportDate
	 */
	public VersionExportPackage(String name, LocalDateTime exportDate) {
		super(name, exportDate);
	}

	/**
	 * @return
	 */
	@Override
	public final String getVersion() {
		return "1";
	}

}
