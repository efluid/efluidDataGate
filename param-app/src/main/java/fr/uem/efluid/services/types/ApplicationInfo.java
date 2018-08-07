package fr.uem.efluid.services.types;

/**
 * <p>
 * Holds some details on running application for easy rendering
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class ApplicationInfo {

	private final String version;
	private final String instanceName;

	/**
	 * @param version
	 * @param instanceName
	 */
	public ApplicationInfo(String version, String instanceName) {
		super();
		this.version = version;
		this.instanceName = instanceName;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return this.version;
	}

	/**
	 * @return the instanceName
	 */
	public String getInstanceName() {
		return this.instanceName;
	}
}
