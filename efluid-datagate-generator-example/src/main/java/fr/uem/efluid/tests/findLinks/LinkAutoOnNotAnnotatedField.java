package fr.uem.efluid.tests.findLinks;

import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterLink;
import fr.uem.efluid.ParameterTable;

@ParameterTable(domainName = "demo", tableName = "SOURCE")
public class LinkAutoOnNotAnnotatedField {

    @ParameterKey
    private int id;

    @ParameterLink(withValueName = "ASSOCIATED_ID")
    private LinkedType associated;

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
