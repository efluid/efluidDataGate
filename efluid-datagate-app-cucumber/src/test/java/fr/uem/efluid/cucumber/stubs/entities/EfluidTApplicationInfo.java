package fr.uem.efluid.cucumber.stubs.entities;

import fr.uem.efluid.cucumber.stubs.ManagedDatabaseAccess;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = ManagedDatabaseAccess.TAPPLICATIONINFO)
public class EfluidTApplicationInfo {

    @Id
    @GeneratedValue
    private int id;

    @Column(name = "PROJET")
    private String projet;

    @Column(name = "SITE")
    private String site;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProjet() {
        return projet;
    }

    public void setProjet(String projet) {
        this.projet = projet;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EfluidTApplicationInfo that = (EfluidTApplicationInfo) o;
        return id == that.id && Objects.equals(projet, that.projet) && Objects.equals(site, that.site);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, projet, site);
    }
}
