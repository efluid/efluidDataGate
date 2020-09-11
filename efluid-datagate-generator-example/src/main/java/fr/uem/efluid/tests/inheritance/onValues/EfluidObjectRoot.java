package fr.uem.efluid.tests.inheritance.onValues;

import fr.uem.efluid.ParameterKey;

/**
 * Based on efluid models
 */
public class EfluidObjectRoot {

    @ParameterKey
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
