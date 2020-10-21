package fr.uem.efluid.tests.inheritance.exclude.all;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.ParameterTable;

import java.time.LocalDate;

@ParameterTable(domainName = "domain", tableName = "LINKED", keyField = "KEY", keyType = ColumnType.STRING)
public class SomeLinkedType {

    private String key;

    private LocalDate when;

    private String data;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public LocalDate getWhen() {
        return when;
    }

    public void setWhen(LocalDate when) {
        this.when = when;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
