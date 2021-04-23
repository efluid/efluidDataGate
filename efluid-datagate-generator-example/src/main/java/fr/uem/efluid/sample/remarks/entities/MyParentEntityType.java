package fr.uem.efluid.sample.remarks.entities;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.ParameterInheritance;
import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterTable;

@ParameterTable(
        excludeInherited = @ParameterInheritance(of = MyParentEntityType.class),
        keys = @ParameterKey(value = "ID", type = ColumnType.STRING),
        domainName = "Entities Efluid")
public abstract class MyParentEntityType {

    private String internal;

    public abstract String getId();

    public String getInternal() {
        return internal;
    }

    public void setInternal(String internal) {
        this.internal = internal;
    }
}
