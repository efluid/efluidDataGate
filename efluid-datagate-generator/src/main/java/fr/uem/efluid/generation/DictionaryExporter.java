package fr.uem.efluid.generation;

import fr.uem.efluid.clients.DictionaryApiClient;
import fr.uem.efluid.model.*;
import fr.uem.efluid.rest.v1.DictionaryApi;
import fr.uem.efluid.rest.v1.model.CreatedDictionaryView;
import fr.uem.efluid.services.ExportService;
import fr.uem.efluid.services.types.ExportFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

/**
 * <p>Specific process for Dictionary export. Extracted from generator code</p>
 *
 * @author elecomte
 * @version 1
 * @since v2.0.0
 */
public class DictionaryExporter extends AbstractProcessor {

    private final ExportService export;

    public DictionaryExporter(DictionaryGeneratorConfig config) {
        super(config);
        this.export = new ExportService();
    }

    /**
     * <p>Process export of extracted dictionary content</p>
     * <p>If config specify <code>uploadToServer</code>, process upload to using config parameters</p>
     *
     * @param content generated content
     * @throws IOException                              on file export error
     * @throws fr.uem.efluid.utils.ApplicationException on upload error
     */
    public void export(
            DictionaryContent content) throws IOException {

        getLog().info("Identified " + content.getAllProjects().size() + " projects, " + content.getAllVersions().size() + " versions, " + content.getAllDomains().size()
                + " domains, " + content.getAllTables().size() + " tables, " + content.getAllLinks().size() + " links and " + content.getAllMappings().size()
                + " mappings : now export dictionary to " + config().getDestinationFolder());

        ExportFile file = this.export.exportPackages(Arrays.asList(
                new GeneratedProjectPackage(content.getAllProjects()),
                new GeneratedDictionaryPackage(content.getAllTables()),
                new GeneratedFunctionalDomainPackage(content.getAllDomains()),
                new GeneratedTableLinkPackage(content.getAllLinks()),
                new GeneratedTableMappingPackage(content.getAllMappings()),
                new GeneratedVersionPackage(content.getAllVersions())));

        String filename = DictionaryGeneratorConfig.AUTO_GEN_DEST_FILE_DESG.equals(config().getDestinationFileDesignation())
                ? file.getFilename()
                : config().getDestinationFileDesignation() + ".par";

        // Write dictionary .par locally
        writeExportFile(file, filename);

        // Upload to remote server if asked for
        if (config().isUploadToServer()) {
            uploadExportFile(file);
        }
    }

    /**
     * @throws IOException on file write error
     */
    private void writeExportFile(ExportFile file, String name) throws IOException {

        Path destFolder = Paths.get(config().getDestinationFolder());

        if (!Files.exists(destFolder)) {
            getLog().debug("Creating missing destination folder " + config().getDestinationFolder());
            Files.createDirectories(destFolder);
        }

        Path destFile = destFolder.resolve(name);

        getLog().debug("Will write dictionary export in destination file " + destFile.toString());

        Files.write(destFile, file.getData(), StandardOpenOption.CREATE);

        getLog().info("Dictionary export done in file " + destFile.toString());
    }

    /**
     * @param file export to push to remote DataGate instance
     * @throws fr.uem.efluid.utils.ApplicationException on upload error
     */
    private void uploadExportFile(ExportFile file) {

        getLog().debug("Will upload the generated dictionary .par to " + config().getUploadEntryPointUri());

        DictionaryApi client = new DictionaryApiClient(config().getUploadEntryPointUri(), config().getUploadSecurityToken());

        CreatedDictionaryView view = client.uploadDictionaryPackage(file.toMultipartFile());

        getLog().info("Generated dictionary uploaded to " + config().getUploadEntryPointUri() + " with "
                + view.getAddedVersionCount() + "/" + view.getUpdatedVersionCount() + " versions changes. Get result : " + view);
    }
}
