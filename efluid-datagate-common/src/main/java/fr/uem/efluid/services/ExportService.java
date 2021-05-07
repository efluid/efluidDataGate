package fr.uem.efluid.services;

import fr.uem.efluid.services.types.ExportFile;
import fr.uem.efluid.services.types.SharedPackage;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.SharedOutputInputUtils;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static fr.uem.efluid.utils.ErrorType.*;

/**
 * <p>
 * Internal service for export using custom package model. At this level the import
 * features ARE NOT implemented
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public class ExportService {

    protected static final String PACKAGE_START_ENDING = "]\n";

    protected static final String PACKAGE_START = "[pack|%s|%s|%s|%s" + PACKAGE_START_ENDING;

    protected static final String PACKAGE_END = "[/pack]\n";

    protected static final String ITEM_START = "[item]";

    protected static final String ITEM_END = "[/item]\n";

    protected static final Charset CHARSET = StandardCharsets.UTF_8;

    protected static final String FILE_PCKG_EXT = ".packs";

    protected static final String FILE_ZIP_EXT = ".par";

    protected static final String FILE_ID = "export-package";

    protected final static ZipParameters ZIP_PARAMS = new ZipParameters();

    static {
        ZIP_PARAMS.setCompressionMethod(CompressionMethod.DEFLATE);
        ZIP_PARAMS.setCompressionLevel(CompressionLevel.NORMAL);
    }

    /**
     * @param packages
     * @return
     */
    public ExportFile exportPackages(List<SharedPackage<?>> packages) {

        try {

            Path workFolder = SharedOutputInputUtils.initTmpFolder();
            List<Path> packFiles = new ArrayList<>();

            packages.parallelStream().forEach(pckg -> {

                pckg.setUncompressPath(workFolder);

                Path pckgFile = initPackageFile(workFolder, pckg);

                // Identifier at start of package
                append(pckgFile,
                        String.format(PACKAGE_START, pckg.getReloadableTypeName(), pckg.getName(), pckg.getExportDate(), pckg.getVersion()));

                // Package content
                pckg.serialize().forEach(s -> append(pckgFile, ITEM_START + s + ITEM_END));

                // End of package
                append(pckgFile, PACKAGE_END);

                packFiles.add(pckgFile);

                // If package has complementary files, add them
                packFiles.addAll(pckg.getComplementaryFiles());
            });

            return new ExportFile(compress(packFiles), "PCKG");

        } catch (IOException e) {
            throw new ApplicationException(EXPORT_FAIL_FILE, "Cannot process export on " + packages.size() + " packages", e);
        }
    }

    private static Path initPackageFile(Path workFolder, SharedPackage<?> pckg) {
        try {
            return Files.createFile(workFolder.resolve(pckg.getName() + FILE_PCKG_EXT));
        } catch (IOException e) {
            throw new ApplicationException(EXPORT_ZIP_FAILED, "Cannot init package file for " + pckg.getName(), e);
        }
    }

    /**
     * @param unzipped
     * @return
     * @throws IOException
     */
    private static Path compress(List<Path> unzipped) {

        Path zip = SharedOutputInputUtils.initTmpFile(FILE_ID, FILE_ZIP_EXT, false);
        ZipFile zipFile = new ZipFile(zip.toString());
        unzipped.forEach(p -> {
            try {
                zipFile.addFile(p.toFile(), ZIP_PARAMS);
            } catch (ZipException e) {
                throw new ApplicationException(EXPORT_ZIP_FAILED, "Cannot zip " + p, e);
            }
        });
        return zip;
    }

    /**
     * Pure streamed file write step
     *
     * @param path
     * @param value
     */
    private static void append(Path path, String value) {
        try {
            Files.writeString(path, value, CHARSET, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new ApplicationException(EXPORT_WRONG_APPEND, "Cannot append to file " + path, e);
        }
    }
}
