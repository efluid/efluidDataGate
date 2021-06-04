package fr.uem.efluid.cucumber.stubs.entities;

import fr.uem.efluid.cucumber.stubs.ManagedDatabaseAccess;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

/**
 * With clob
 *
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@Entity
@Table(name = ManagedDatabaseAccess.TABLE_SIX)
public class SimulatedTableSix {

    @Id
    private Long identifier;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String text;

    private LocalDate date;

    public Long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Long identifier) {
        this.identifier = identifier;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimulatedTableSix that = (SimulatedTableSix) o;
        return Objects.equals(getIdentifier(), that.getIdentifier()) &&
                Objects.equals(getText(), that.getText()) &&
                Objects.equals(getDate(), that.getDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdentifier(), getText(), getDate());
    }
}
