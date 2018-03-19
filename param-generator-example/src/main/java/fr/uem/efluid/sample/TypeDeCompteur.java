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
@ParameterTable(name = "Type de compteur", tableName = "TTYPECOMPTEURS")
public class TypeDeCompteur {

	private Long id;

	@ParameterKey("DESIGNATION")
	private String designation;

	@ParameterValue("VARIANTE")
	private String variante;

	/**
	 * 
	 */
	public TypeDeCompteur() {
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
	 * @return the designation
	 */
	public String getDesignation() {
		return this.designation;
	}

	/**
	 * @param designation
	 *            the designation to set
	 */
	public void setDesignation(String designation) {
		this.designation = designation;
	}

	/**
	 * @return the variante
	 */
	public String getVariante() {
		return this.variante;
	}

	/**
	 * @param variante
	 *            the variante to set
	 */
	public void setVariante(String variante) {
		this.variante = variante;
	}

}
