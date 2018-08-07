package fr.uem.efluid.rest.v1.model;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class ApplicationInfoView {

	private final String version;
	private final String instanceName;

	/**
	 * @param version
	 * @param instanceName
	 */
	public ApplicationInfoView(String version, String instanceName) {
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
