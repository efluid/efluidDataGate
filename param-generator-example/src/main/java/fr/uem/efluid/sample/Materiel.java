package fr.uem.efluid.sample;

import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterLink;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterValue;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@GestionDuMateriel
@ParameterTable(name = "Materiel", tableName = "TMATERIEL")
public class Materiel {

	private Long id;

	@ParameterKey("SERIAL")
	private String numeroSerie;

	@ParameterValue("DETAILS")
	private String details;

	@ParameterValue("CAT_ID")
	@ParameterLink(toColumn = "ID")
	private CategorieDeMateriel categorie;

	/**
	 * 
	 */
	public Materiel() {
		// TODO Auto-generated constructor stub
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
	 * @return the numeroSerie
	 */
	public String getNumeroSerie() {
		return this.numeroSerie;
	}

	/**
	 * @param numeroSerie
	 *            the numeroSerie to set
	 */
	public void setNumeroSerie(String numeroSerie) {
		this.numeroSerie = numeroSerie;
	}

	/**
	 * @return the categorie
	 */
	public CategorieDeMateriel getCategorie() {
		return this.categorie;
	}

	/**
	 * @param categorie
	 *            the categorie to set
	 */
	public void setCategorie(CategorieDeMateriel categorie) {
		this.categorie = categorie;
	}

	/**
	 * @return the details
	 */
	public String getDetails() {
		return this.details;
	}

	/**
	 * @param details
	 *            the details to set
	 */
	public void setDetails(String details) {
		this.details = details;
	}

}
