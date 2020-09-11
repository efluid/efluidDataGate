package fr.uem.efluid.tests.findLinks;

import fr.uem.efluid.*;

@ParameterTableSet(domainName = "demo",
        tables = {
                @ParameterTable(
                        tableName = "SOURCE_ONE"
                ),
                @ParameterTable(
                        tableName = "SOURCE_TWO"
                )})
public class LinkInTableSet {

    @ParameterKey
    private int id;

    @ParameterLink(toTableName = "LINKED", toColumn = "KEY")
    @ParameterValue(name = "ASSOCIATED_ID", forTable = "SOURCE_ONE")
    private LinkedType associated;

    @ParameterValue("OTHER")
    private String other;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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
