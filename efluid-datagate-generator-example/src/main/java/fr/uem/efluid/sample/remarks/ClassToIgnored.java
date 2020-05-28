package fr.uem.efluid.sample.remarks;

import fr.uem.efluid.ParameterIgnored;
import fr.uem.efluid.ParameterTable;

import java.time.LocalDateTime;

@ParameterIgnored
@ParameterTable(tableName = "CLASS_TO_IGNORED")
public class ClassToIgnored {

    private String value;

    private LocalDateTime time;

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
