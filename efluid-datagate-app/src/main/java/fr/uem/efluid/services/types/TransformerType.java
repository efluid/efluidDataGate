package fr.uem.efluid.services.types;

import fr.uem.efluid.tools.Transformer;

import java.util.Objects;

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
        this.type = transformer.getClass().getSimpleName();
        this.name = transformer.getName();
    }

    public TransformerType(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransformerType that = (TransformerType) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }
}
