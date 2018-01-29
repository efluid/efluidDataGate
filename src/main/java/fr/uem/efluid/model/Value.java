package fr.uem.efluid.model;

/**
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface Value {

	byte[] getValue();

	// TODO : need to decide if typed or not
	boolean isString();
}
