package fr.uem.efluid.services.types;

import fr.uem.efluid.tools.Transformer;

/**
 * Definition of a transformer to select / call
 *
 * @author elecomte
 * @version 1
 * @since v1.2.0
 */
public class TransformerType {

    private final String type;
    private final String name;

    public TransformerType(Transformer<?, ?> transformer) {
        this.type = transformer.getClass().getTypeName();
        this.name = transformer.getName();
    }

    public String getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }
}
