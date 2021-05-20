package fr.uem.efluid.services.types;

import fr.uem.efluid.model.entities.TransformerDef;
import fr.uem.efluid.transformers.Transformer;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
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

    @NotNull
    private String name;

    @Min(0)
    private int priority;

    @NotNull
    private String type;

    private String typeName;

    @NotNull
    private String configuration;

    private String packageComment;

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

    public String getTypeName() {
        return this.typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getPackageComment() {
        return packageComment;
    }

    public void setPackageComment(String packageComment) {
        this.packageComment = packageComment;
    }

    public void setTransformer(Transformer<?, ?> transformer) {
        this.setType(transformer.getClass().getSimpleName());
        this.setTypeName(transformer.getName());
    }

    public String getConfiguration() {
        return this.configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public static TransformerDefEditData fromEntity(TransformerDef def, Transformer<?, ?> transformer, String prettyConfig, String packageComment) {

        TransformerDefEditData editData = new TransformerDefEditData();

        editData.setName(def.getName());
        editData.setPriority(def.getPriority());
        editData.setConfiguration(prettyConfig);
        editData.setUuid(def.getUuid());

        editData.setTransformer(transformer);
        editData.setPackageComment(packageComment);

        return editData;
    }
}
