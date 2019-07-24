package fr.uem.efluid.rest.v1.model;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class ApplicationInfoView {

	private final String version;
	private final String instanceName;
	private final String modelId;

	/**
	 * @param version
	 * @param instanceName
	 */
	public ApplicationInfoView(String version, String instanceName, String modelId) {
		super();
		this.version = version;
		this.instanceName = instanceName;
		this.modelId = modelId != null ? modelId : " - not available - ";
	}

	/**
	 * @return the modelId
	 */
	public String getModelId() {
		return this.modelId;
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
