package fr.uem.efluid.sample;

import fr.uem.efluid.ParameterLink;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterValue;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@GestionDuMateriel
@ParameterTable(name = "Compteur", tableName = "TCOMPTEUR", useAllFields = false, filterClause = "cur.\"ACTIF\"=1")
public class Compteur extends Materiel {

	@ParameterValue("NOM")
	private String nom;

	@ParameterValue("FABRIQUANT")
	private String fabriquant;

	private boolean actif;

	@ParameterValue("TYPE_ID")
	@ParameterLink(toColumn = "ID")
	private TypeDeCompteur type;

	/**
	 * 
	 */
	public Compteur() {
		super();
	}

	/**
	 * @return the nom
	 */
	public String getNom() {
		return this.nom;
	}

	/**
	 * @param nom
	 *            the nom to set
	 */
	public void setNom(String nom) {
		this.nom = nom;
	}

	/**
	 * @return the fabriquant
	 */
	public String getFabriquant() {
		return this.fabriquant;
	}

	/**
	 * @param fabriquant
	 *            the fabriquant to set
	 */
	public void setFabriquant(String fabriquant) {
		this.fabriquant = fabriquant;
	}

	/**
	 * @return the actif
	 */
	public boolean isActif() {
		return this.actif;
	}

	/**
	 * @param actif
	 *            the actif to set
	 */
	public void setActif(boolean actif) {
		this.actif = actif;
	}

	/**
	 * @return the type
	 */
	public TypeDeCompteur getType() {
		return this.type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(TypeDeCompteur type) {
		this.type = type;
	}

}
