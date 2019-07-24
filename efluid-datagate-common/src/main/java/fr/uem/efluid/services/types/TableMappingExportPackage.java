package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.shared.ExportAwareTableMapping;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public abstract class TableMappingExportPackage<D extends ExportAwareTableMapping<?>> extends SharedPackage<D> {

	public static final String MAPPINGS_EXPORT = "full-mappings";
	public static final String PARTIAL_MAPPINGS_EXPORT = "partial-mappings";

	/**
	 * @param name
	 * @param exportDate
	 */
	public TableMappingExportPackage(String name, LocalDateTime exportDate) {
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
