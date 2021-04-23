package fr.uem.efluid.sample.updates;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterValue;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@ParameterTable(tableName = "TANOTHER_LAST", keys = @ParameterKey(value = "IDENTIFIER", type = ColumnType.STRING))
public class LastLinkedType extends AnotherLinkedType {

    private long id;

    @ParameterValue
    private int anotherOther;

    /**
     * @return the id
     */
    public long getId() {
        return this.id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the anotherOther
     */
    public int getAnotherOther() {
        return this.anotherOther;
    }

    /**
     * @param anotherOther the anotherOther to set
     */
    public void setAnotherOther(int anotherOther) {
        this.anotherOther = anotherOther;
    }

}
