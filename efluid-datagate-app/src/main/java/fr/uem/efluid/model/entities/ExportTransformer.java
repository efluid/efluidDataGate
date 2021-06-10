package fr.uem.efluid.model.entities;

import javax.persistence.*;
import java.util.Objects;

/**
 * Customization of a transformer for an export
 *
 * @author elecomte
 * @version 1
 * @since v1.1.0
 */
@Entity
@Table(name = "export_transformers")
public class ExportTransformer {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private TransformerDef transformerDef;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String configuration;

    private boolean disabled;

    @ManyToOne
    private Export export;

    public ExportTransformer() {
    }

    public ExportTransformer(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TransformerDef getTransformerDef() {
        return transformerDef;
    }

    public void setTransformerDef(TransformerDef transformerDef) {
        this.transformerDef = transformerDef;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public Export getExport() {
        return export;
    }

    public void setExport(Export export) {
        this.export = export;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportTransformer that = (ExportTransformer) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
