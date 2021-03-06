package fr.uem.efluid.sample.remarks;

import fr.uem.efluid.ParameterIgnored;
import fr.uem.efluid.ParameterTable;

import java.time.LocalDateTime;

@ParameterTable(tableName = "CLASS_NOT_IGNORED", keyField = "key")
public class ClassNotIgnored {

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
