package fr.uem.efluid.tests.findLinks;

import fr.uem.efluid.*;

@ParameterTable(
        domainName = "demo",
        tableName = "SOURCE",
        values = {
                @ParameterValue("ASSOCIATED_ID"),
                @ParameterValue("OTHER")
        },
        keys = @ParameterKey(value = "ID", type = ColumnType.ATOMIC))
public class LinkSpecifiedOnSpecifiedValue {

    private int id;

    @ParameterLink(withValueName = "ASSOCIATED_ID", toTableName = "LINKED", toColumn = "KEY")
    private LinkedType associated;

    private String other;

    public LinkedType getAssociated() {
        return associated;
    }

    public void setAssociated(LinkedType associated) {
        this.associated = associated;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }
}
