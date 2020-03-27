package fr.uem.efluid.services.types;

import fr.uem.efluid.model.entities.TransformerDef;

import java.util.UUID;

/**
 * For edition of a TransformerDef
 *
 * @author elecomte
 * @version 1
 * @since v1.2.0
 */
public class TransformerDefEditData {

    private UUID uuid;

    private String name;

    private int priority;

    private String type;

    private String configuration;

    public TransformerDefEditData() {
        super();
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConfiguration() {
        return this.configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public static TransformerDefEditData fromEntity(TransformerDef def, String prettyConfig) {

        TransformerDefEditData editData = new TransformerDefEditData();

        editData.setName(def.getName());
        editData.setPriority(def.getPriority());
        editData.setType(def.getType());
        editData.setConfiguration(prettyConfig);
        editData.setUuid(def.getUuid());

        return editData;
    }
}
