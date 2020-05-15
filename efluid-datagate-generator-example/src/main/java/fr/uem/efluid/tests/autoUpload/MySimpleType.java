package fr.uem.efluid.tests.autoUpload;

import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterTable;

@MyDomain
@ParameterTable(tableName = "T_TABLE")
public class MySimpleType {

    @ParameterKey
    private String key;

    private String value;

    private int other;
}
