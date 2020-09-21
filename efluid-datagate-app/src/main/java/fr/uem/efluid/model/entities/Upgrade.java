package fr.uem.efluid.model.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "upgrades")
public class Upgrade {

    @Id
    private String name;

    private int index;

    private LocalDateTime runTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public LocalDateTime getRunTime() {
        return runTime;
    }

    public void setRunTime(LocalDateTime runTime) {
        this.runTime = runTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Upgrade upgrade = (Upgrade) o;
        return index == upgrade.index &&
                Objects.equals(name, upgrade.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, index);
    }
}
