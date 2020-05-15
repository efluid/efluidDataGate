package fr.uem.efluid.tests.ignoreconstants;

import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterTable;

@ParameterTable(tableName = "T_ROOT", useAllFields = true, domainName = "domain")
public class RootType {

    @ParameterKey
    private String key;

    private static final String CONSTANT_PROPERTY = "SOMETHING";

    private String value;

    private int other;
}
