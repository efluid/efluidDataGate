package fr.uem.efluid.model.entities;

import fr.uem.efluid.model.AnomalyContextType;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "anomalies")
public class Anomaly {

    @Id
    @GeneratedValue
    private Long id;

    private LocalDateTime detectTime;

    @Enumerated(EnumType.STRING)
    private AnomalyContextType contextType;

    private String contextName;

    private String code;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String message;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDetectTime() {
        return detectTime;
    }

    public void setDetectTime(LocalDateTime detectTime) {
        this.detectTime = detectTime;
    }

    public String getMessage() {
        return message;
    }

    public AnomalyContextType getContextType() {
        return contextType;
    }

    public void setContextType(AnomalyContextType contextType) {
        this.contextType = contextType;
    }

    public String getContextName() {
        return contextName;
    }

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Anomaly anomaly = (Anomaly) o;
        return Objects.equals(id, anomaly.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
