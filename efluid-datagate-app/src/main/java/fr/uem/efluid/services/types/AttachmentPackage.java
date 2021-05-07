package fr.uem.efluid.services.types;

import fr.uem.efluid.model.entities.Attachment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * Attachment uses a combined file + json data for export / import
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v2.0.0
 */
public class AttachmentPackage extends AbstractMixedPackage<Attachment> {

    /**
     * @param name
     * @param exportDate
     */
    public AttachmentPackage(String name, LocalDateTime exportDate) {
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
    protected Attachment initContent() {
        return new Attachment();
    }

    /**
     * @return
     */
    public List<AttachmentLine> toAttachmentLines() {
        return content().map(c -> {
            AttachmentLine line = new AttachmentLine();
            line.setUuid(c.getUuid());
            line.setName(c.getName());
            line.setType(c.getType());
            line.setTmpPath(getUncompressPath() + "/" + c.getTmpPath());
            line.setImportedTime(LocalDateTime.now());
            return line;
        }).collect(Collectors.toList());
    }
}
