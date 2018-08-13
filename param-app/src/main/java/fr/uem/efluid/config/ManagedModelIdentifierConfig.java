package fr.uem.efluid.config;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class ManagedModelIdentifierConfig {

	public static final class ManagedModelIdentifierProperties {

		private boolean enabled;

		private String identifierClassName;

		/**
		 * 
		 */
		public ManagedModelIdentifierProperties() {
			super();
		}

		/**
		 * @return the enabled
		 */
		public boolean isEnabled() {
			return this.enabled;
		}

		/**
		 * @param enabled
		 *            the enabled to set
		 */
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		/**
		 * @return the identifierClassName
		 */
		public String getIdentifierClassName() {
			return this.identifierClassName;
		}

		/**
		 * @param identifierClassName
		 *            the identifierClassName to set
		 */
		public void setIdentifierClassName(String identifierClassName) {
			this.identifierClassName = identifierClassName;
		}
	}
}
