package fr.uem.efluid.services.types;

import fr.uem.efluid.model.entities.TransformerDef;

import java.time.LocalDateTime;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public class TransformerDefPackage extends SharedPackage<TransformerDef> {

    /**
     * @param name
     * @param exportDate
     */
    public TransformerDefPackage(String name, LocalDateTime exportDate) {
        super(name, exportDate);
    }

    /**
     * @return
     */
    @Override
    public String getVersion() {
        return "1";
    }

    /**
     * @return
     */
    @Override
    protected TransformerDef initContent() {
        return new TransformerDef();
    }

}
