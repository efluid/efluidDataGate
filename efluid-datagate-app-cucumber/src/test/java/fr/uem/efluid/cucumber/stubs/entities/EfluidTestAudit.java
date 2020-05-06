package fr.uem.efluid.cucumber.stubs.entities;

import fr.uem.efluid.cucumber.stubs.ManagedDatabaseAccess;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = ManagedDatabaseAccess.EFLUIDTESTAUDIT)
public class EfluidTestAudit {

    @Id
    private String id;

    private String value;

    private String etatObjet;

    private LocalDate dateSuppression;
    private LocalDate dateModification;
    private LocalDate dateCreation;

    private String acteurSuppression;
    private String acteurModification;
    private String acteurCreation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getEtatObjet() {
        return etatObjet;
    }

    public void setEtatObjet(String etatObjet) {
        this.etatObjet = etatObjet;
    }

    public LocalDate getDateSuppression() {
        return dateSuppression;
    }

    public void setDateSuppression(LocalDate dateSuppression) {
        this.dateSuppression = dateSuppression;
    }

    public LocalDate getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDate dateModification) {
        this.dateModification = dateModification;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getActeurSuppression() {
        return acteurSuppression;
    }

    public void setActeurSuppression(String acteurSuppression) {
        this.acteurSuppression = acteurSuppression;
    }

    public String getActeurModification() {
        return acteurModification;
    }

    public void setActeurModification(String acteurModification) {
        this.acteurModification = acteurModification;
    }

    public String getActeurCreation() {
        return acteurCreation;
    }

    public void setActeurCreation(String acteurCreation) {
        this.acteurCreation = acteurCreation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EfluidTestAudit that = (EfluidTestAudit) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(value, that.value) &&
                Objects.equals(etatObjet, that.etatObjet) &&
                Objects.equals(dateSuppression, that.dateSuppression) &&
                Objects.equals(dateModification, that.dateModification) &&
                Objects.equals(dateCreation, that.dateCreation) &&
                Objects.equals(acteurSuppression, that.acteurSuppression) &&
                Objects.equals(acteurModification, that.acteurModification) &&
                Objects.equals(acteurCreation, that.acteurCreation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value, etatObjet, dateSuppression, dateModification, dateCreation, acteurSuppression, acteurModification, acteurCreation);
    }
}
