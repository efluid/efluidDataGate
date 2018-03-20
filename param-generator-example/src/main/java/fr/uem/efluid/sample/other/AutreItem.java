package fr.uem.efluid.sample.other;

import java.time.LocalDateTime;

import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterValue;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@ParameterTable(name = "Autre table", tableName = "TOTHER", filterClause = "1=1")
public class AutreItem {

	private Long id;

	private String code;

	private LocalDateTime when;

	private Float value;

	private byte[] file;

	/**
	 * 
	 */
	public AutreItem() {
		super();
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return this.id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the code
	 */
	@ParameterKey("CODE")
	public String getCode() {
		return this.code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the when
	 */
	@ParameterValue("WHEN")
	public LocalDateTime getWhen() {
		return this.when;
	}

	/**
	 * @param when
	 *            the when to set
	 */
	public void setWhen(LocalDateTime when) {
		this.when = when;
	}

	/**
	 * @return the value
	 */
	@ParameterValue("VALUE")
	public Float getValue() {
		return this.value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Float value) {
		this.value = value;
	}

	/**
	 * @return the file
	 */
	@ParameterValue("BFILE")
	public byte[] getFile() {
		return this.file;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(byte[] file) {
		this.file = file;
	}

}
