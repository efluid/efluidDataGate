package fr.uem.efluid.sample.remarks;

import fr.uem.efluid.ParameterTable;

import java.time.LocalDateTime;

@ParameterTable(tableName = "T_ETAPE_WFL", keyField = "key")
public class EtapeWorkflow {

    private int key;

    private String value;

    private LocalDateTime time;

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}
