package fr.uem.efluid.tests.deleteAfterUpload;

import fr.uem.efluid.ParameterKey;

public class EfluidObjectRoot {

    @ParameterKey
    private String keyOne;

    @ParameterKey
    private int keyTwo;

    public String getKeyOne() {
        return keyOne;
    }

    public void setKeyOne(String keyOne) {
        this.keyOne = keyOne;
    }

    public int getKeyTwo() {
        return keyTwo;
    }

    public void setKeyTwo(int keyTwo) {
        this.keyTwo = keyTwo;
    }
}
