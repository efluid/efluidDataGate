package fr.uem.efluid.services.types;

import fr.uem.efluid.model.Shared;
import fr.uem.efluid.utils.SharedOutputInputUtils;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Package for item which use a combined file + json data for export / import
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v2.1.18
 */
public abstract class AbstractMixedPackage<T extends Shared> extends SharedPackage<T> {

    private List<Path> attFiles = new ArrayList<>();

    /**
     * @param name
     * @param exportDate
     */
    public AbstractMixedPackage(String name, LocalDateTime exportDate) {
        super(name, exportDate);
    }

    /**
     * @param rawContent
     * @return
     * @see SharedPackage#deserializeOne(String)
     */
    @Override
    protected T deserializeOne(String rawContent) {
        // For Attachment deser we use "mixed form" for content : the uncompress path
        // (where attachment files are uncompressed) is associated to content for inline
        // processing in deser process.
        return super.deserializeOne(SharedOutputInputUtils.mergeValues(getUncompressPath().toString(), rawContent));
    }

    /**
     * @param content
     * @return
     * @see SharedPackage#serializeOne(fr.uem.efluid.model.Shared)
     */
    @Override
    protected String serializeOne(T content) {

        // Attachment serial. uses a mixed content result
        String[] mixedContent = SharedOutputInputUtils.splitValues(super.serializeOne(content));

        // Attachment name, if any
        String includedFilename = mixedContent[0];

        // Move available generated file to TMP folder for inclusion in zip
        if(StringUtils.hasText(includedFilename)) {
            this.attFiles.add(SharedOutputInputUtils.repatriateTmpFile(includedFilename, getUncompressPath()));
        }

        // In pak file will use only json part
        return mixedContent[1];
    }

    /**
     * @return
     * @see SharedPackage#getComplementaryFiles()
     */
    @Override
    public List<Path> getComplementaryFiles() {
        return this.attFiles;
    }
}
