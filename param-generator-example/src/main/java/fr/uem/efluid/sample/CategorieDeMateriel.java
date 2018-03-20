package fr.uem.efluid.sample;

import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterValue;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@GestionDuMateriel
@ParameterTable(name = "Categorie", tableName = "TCATEGORY")
public class CategorieDeMateriel {

	private Long id;

	@ParameterValue("NAME")
	private String name;

	@ParameterKey("CODE")
	private String code;

	/**
	 * 
	 */
	public CategorieDeMateriel() {
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
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the code
	 */
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

}
