package fr.uem.efluid.system.common;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class InternalTestException extends RuntimeException {

	private static final long serialVersionUID = 20181127;

	/**
	 * @param message
	 * @param cause
	 */
	public InternalTestException(String message, Throwable cause) {
		super(message, cause);
	}

}
