package fr.uem.efluid.services;

import fr.uem.efluid.services.types.ExportFile;
import fr.uem.efluid.services.types.SharedPackage;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.SharedOutputInputUtils;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static fr.uem.efluid.utils.ErrorType.*;

/**
 * <p>
 * Internal service for import / export using custom package model. Export features are
 * inherited from common <tt>ExportService</tt>
 * </p>
 * <p>The import process is based on pure stream chain building</p>
 *
 * @author elecomte
 * @version 2
 * @since v0.0.1
 */
@Service
public class ExportImportService extends ExportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportService.class);

    private static final String HEADER_SEARCH = "\\[pack\\|[^]]*]";
    private static final String ITEM_SEARCH = "\\[/?item]";

    /**
     * From an uploaded .par file, get all the identifiable SharedPackage. The file is written in temp location, unziped,
     * and each included .pack file is processed to get the corresponding SharedPackage
     *
     * @param file uploaded file definition, with compressed content access
     * @return the SharedPackage in a list, each initialized with a content deserialization stream
     */
    public List<SharedPackage<?>> importPackages(ExportFile file) {

        try {
            Path path = SharedOutputInputUtils.initTmpFile(FILE_ID, FILE_ZIP_EXT, true);
            Path uncompressPath = SharedOutputInputUtils.initTmpFolder();
            Files.write(path, file.getData());

            return unCompress(uncompressPath, path).stream()
                    .filter(p -> p.getFileName().toString().endsWith(FILE_PCKG_EXT))
                    .map(p -> {
                        try {
                            return new FileInputStream(p.toFile());
                        } catch (FileNotFoundException e) {
                            throw new ApplicationException(IMPORT_ZIP_FAILED, "Cannot read " + p);
                        }
                    })
                    .map(s -> readPackage(uncompressPath, s))
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new ApplicationException(IMPORT_FAIL_FILE, "Cannot process import on package " + file.getFilename(), e);
        }
    }

    /**
     * @param uncompressPath location of currently uncompressing package
     * @param pack           InputStream of the corresponding (.pack) file
     * @return prepared package with content stream initialized for deserialization
     */
    private static SharedPackage<?> readPackage(Path uncompressPath, InputStream pack) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Reading package content : \n{}", pack);
        }

        try {
            Scanner sc = new Scanner(pack);

            // Pack def is first item in header part
            // [pack|%s|%s|%s|%s]\n
            // Class, name, date, version
            String header = sc.findInLine(HEADER_SEARCH);
            String[] packParams = header.substring(6, header.length() - 1).split("\\|");

            Class<?> type = Class.forName(packParams[0]);
            String version = packParams[3].replaceAll(PACKAGE_START_ENDING, "");
            LocalDateTime date = LocalDateTime.parse(packParams[2]);
            String name = packParams[1];

            // Check type is valid
            if (!SharedPackage.class.isAssignableFrom(type)) {
                throw new ApplicationException(IMPORT_WRONG_TYPE,
                        "Processing of package type load is invalid. Type " + type + " is not a package");
            }

            // Init instance
            SharedPackage<?> instance = (SharedPackage<?>) type.getConstructor(String.class, LocalDateTime.class)
                    .newInstance(name, date);

            // Validate compatibility on specific rules from package (based on version)
            if (!instance.isCompatible(version)) {
                throw new ApplicationException(IMPORT_WRONG_VERSION, "Processing of package type load is invalid. Version " + version
                        + " is not supported for \"" + name + "\" (current version is " + instance.getVersion() + ")");
            }

            // Get items
            sc.useDelimiter(ITEM_SEARCH);

            // Apply uncompressPath if ref to files is used
            instance.setUncompressPath(uncompressPath);

            // Prepare deserialized stream process from scanner
            instance.deserialize(sc.tokens().onClose(() -> {
                try {
                    pack.close();
                } catch (IOException e) {
                    throw new ApplicationException(IMPORT_WRONG_READ, "Cannot close package file stream", e);
                }
            }));

            // Package ready, with imported content
            return instance;
        }

        // From instance init
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new ApplicationException(IMPORT_WRONG_INSTANCE, "Processing of package type load is invalid. Check type", e);
        }
    }

    /**
     * @param uncompressPath were zipped file will be uncompressed
     * @param zipped         location of zipped pack
     * @return list of uncompressed files
     */
    private static List<Path> unCompress(Path uncompressPath, Path zipped) throws IOException {

        try {
            ZipFile zipFile = new ZipFile(zipped.toString());
            zipFile.extractAll(uncompressPath.toString());
            return Files.walk(uncompressPath)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        } catch (ZipException e) {
            throw new ApplicationException(IMPORT_ZIP_FAILED, "Cannot unzip " + zipped);
        }
    }
}
