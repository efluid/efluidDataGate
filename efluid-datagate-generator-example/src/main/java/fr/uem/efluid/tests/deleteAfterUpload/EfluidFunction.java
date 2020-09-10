package fr.uem.efluid.tests.deleteAfterUpload;

import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterTable;

import java.time.LocalDateTime;

@EfluidWorkflowDomain
@ParameterTable(name = "Function", tableName = "TMODELEFONCTION")
public class EfluidFunction {

    @ParameterKey
    private int id;

    private String value;

    private LocalDateTime createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
