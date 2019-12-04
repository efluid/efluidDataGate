package fr.uem.efluid.sample.remarks;

public class ParentType {

    private String something;

    private transient boolean enabled;

    public String getValue(){
        return "value";
    }

    public void process(){
        this.something = "som";
        this.enabled = true;
    }

    public boolean isEnabled(){
        return this.enabled;
    }
}
