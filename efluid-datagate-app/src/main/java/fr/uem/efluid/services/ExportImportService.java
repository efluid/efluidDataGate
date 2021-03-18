package fr.uem.efluid.services;

import static fr.uem.efluid.utils.ErrorType.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.uem.efluid.services.types.ExportFile;
import fr.uem.efluid.services.types.SharedPackage;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.SharedOutputInputUtils;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * <p>
 * Internal service for import / export using custom package model. Export features are
 * inherited from common <tt>ExportService</tt>
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Service
public class ExportImportService extends ExportService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExportService.class);

	private static final String ITEM_START_SEARCH = "\\[item\\]";

	/**
	 * @param file
	 * @return
	 */
	public List<SharedPackage<?>> importPackages(ExportFile file) {

		try {
			Path path = SharedOutputInputUtils.initTmpFile(FILE_ID, FILE_ZIP_EXT, true);
			Path uncompressPath = SharedOutputInputUtils.initTmpFolder();
			Files.write(path, file.getData());

			return unCompress(uncompressPath, path).stream()
					.filter(p -> p.getFileName().toString().endsWith(FILE_PCKG_EXT))
					.map(ExportImportService::readFile)
					.map(s -> readPackage(uncompressPath, s))
					.filter(Objects::nonNull)
					.collect(Collectors.toList());

		} catch (IOException e) {
			throw new ApplicationException(IMPORT_FAIL_FILE, "Cannot process import on package " + file.getFilename(), e);
		}
	}

	/**
	 * @param uncompressPath
	 *            location of currently uncompressing package
	 * @param pack
	 * @return
	 */
	private static SharedPackage<?> readPackage(Path uncompressPath, String pack) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Reading package content : \n{}", pack);
		}

		try {
			// Ignore pack ending
			String[] items = pack.substring(0, pack.length() - PACKAGE_END.length()).split(ITEM_START_SEARCH);

			// Pack def is first item
			// [pack|%s|%s|%s|%s]\n
			// Class, name, date, version
			String[] packParams = items[0].substring(6, items[0].length() - 2).split("\\|");

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

			// Must have same version
			if (!instance.getVersion().equals(version)) {
				throw new ApplicationException(IMPORT_WRONG_VERSION, "Processing of package type load is invalid. Version " + version
						+ " is not supported for \"" + name + "\" (current version is " + instance.getVersion() + ")");
			}

			List<String> itemSerialized = new ArrayList<>();

			// Get items
			for (int i = 1; i < items.length; i++) {

				// Drop item ending
				String itemContent = items[i].substring(0, items[i].length() - ITEM_END.length());

				LOGGER.debug("Identified serialized item in package \"{}\". Content : {}", name, itemContent);
				itemSerialized.add(itemContent);
			}

			// Apply uncompressPath if ref to files is used
			instance.setUncompressPath(uncompressPath);

			// Deserialize => prepare content
			instance.deserialize(itemSerialized);

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
	 * @param uncompressPath
	 *            were zipped file will be uncompressed
	 * @param zipped
	 *            location of zipped pack
	 * @return list of uncompressed files
	 * @throws IOException
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

	/**
	 * @param path
	 * @return
	 */
	private static String readFile(Path path) {
		try {
			return Files.readString(path, CHARSET);
		} catch (IOException e) {
			throw new ApplicationException(IMPORT_WRONG_READ, "Cannot read file " + path, e);
		}
	}

}
