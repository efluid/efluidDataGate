package fr.uem.efluid.tests.inheritance.onValues;

/**
 * To test intermediate parent exclusion
 */
public class EfluidSubRoot extends EfluidObjectRoot{

    private String something;

    public String getSomething() {
        return something;
    }

    public void setSomething(String something) {
        this.something = something;
    }
}
