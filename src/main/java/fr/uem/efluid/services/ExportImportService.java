package fr.uem.efluid.services;

import static fr.uem.efluid.utils.ErrorType.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.uem.efluid.model.Shared;
import fr.uem.efluid.services.types.ExportImportFile;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.SharedOutputInputUtils;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

/**
 * <p>
 * Internal service for import / export using custom package model
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Service
@SuppressWarnings("synthetic-access")
public class ExportImportService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExportImportService.class);

	private static final String PACKAGE_START = "[pack|%s|%s|%s|%s]\n";

	private static final String PACKAGE_END = "[/pack]\n";

	private static final String ITEM_START = "[item]";
	private static final String ITEM_START_SEARCH = "\\[item\\]";

	private static final String ITEM_END = "[/item]\n";

	private static final Charset CHARSET = Charset.forName("utf8");

	private static final String FILE_PCKG_EXT = ".packs";

	private static final String FILE_ZIP_EXT = ".par";

	private static final String FILE_ID = "export-package";

	private final static ZipParameters ZIP_PARAMS = new ZipParameters();

	static {
		ZIP_PARAMS.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		ZIP_PARAMS.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
	}

	/**
	 * @param packages
	 * @return
	 */
	ExportImportFile exportPackages(List<ExportImportPackage<?>> packages) {

		LOGGER.info("packaging {} items", Integer.valueOf(packages.size()));

		try {

			Path workFolder = SharedOutputInputUtils.initTmpFolder();
			List<Path> packFiles = new ArrayList<>();

			for (ExportImportPackage<?> pckg : packages) {

				Path pckgFile = Files.createFile(workFolder.resolve(pckg.getName() + FILE_PCKG_EXT));

				// Identifier at start of package
				append(pckgFile,
						String.format(PACKAGE_START, pckg.getClass().getName(), pckg.getName(), pckg.getExportDate(), pckg.getVersion()));

				// Package content
				pckg.serialize().forEach(s -> append(pckgFile, ITEM_START + s + ITEM_END));

				// End of package
				append(pckgFile, PACKAGE_END);

				packFiles.add(pckgFile);
			}

			// TODO : define CType
			return new ExportImportFile(compress(packFiles), "PCKG");

		} catch (IOException e) {
			throw new ApplicationException(EXPORT_FAIL_FILE, "Cannot process export on " + packages.size() + " packages", e);
		}
	}

	/**
	 * @param file
	 * @return
	 */
	List<ExportImportPackage<?>> importPackages(ExportImportFile file) {

		try {
			Path path = SharedOutputInputUtils.initTmpFile(FILE_ID, FILE_ZIP_EXT, true);
			Path uncompressPath = SharedOutputInputUtils.initTmpFolder();
			Files.write(path, file.getData());

			return unCompress(uncompressPath, path).stream()
					.filter(p -> p.getFileName().toString().endsWith(FILE_PCKG_EXT))
					.map(ExportImportService::readFile)
					.map(s -> readPackage(uncompressPath, s))
					.filter(i -> i != null)
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
	private static ExportImportPackage<?> readPackage(Path uncompressPath, String pack) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Reading package content : \n{}", pack);
		}

		try {
			String[] items = pack.split(ITEM_START_SEARCH);

			// Empty pack :
			if (items[0].endsWith(PACKAGE_END)) {
				return null;
			}

			// Pack def is first item
			// [pack|%s|%s|%s|%s]\n
			// Class, name, date, version
			String[] packParams = items[0].substring(6, items[0].length() - 2).split("\\|");

			Class<?> type = Class.forName(packParams[0]);
			String version = packParams[3];
			LocalDateTime date = LocalDateTime.parse(packParams[2]);
			String name = packParams[1];

			// Check type is valid
			if (!ExportImportPackage.class.isAssignableFrom(type)) {
				throw new ApplicationException(IMPORT_WRONG_TYPE,
						"Processing of package type load is invalid. Type " + type + " is not a package");
			}

			// Init instance
			ExportImportPackage<?> instance = (ExportImportPackage<?>) type.getConstructor(String.class, LocalDateTime.class)
					.newInstance(name, date);

			// Must have same version
			if (!instance.getVersion().equals(version)) {
				throw new ApplicationException(IMPORT_WRONG_VERSION, "Processing of package type load is invalid. Version " + version
						+ " is not supported for \"" + name + "\" (current version is " + instance.getVersion() + ")");
			}

			List<String> itemSerialized = new ArrayList<>();

			// Get items
			for (int i = 1; i < items.length; i++) {
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
	 * @param unzipped
	 * @return
	 * @throws IOException
	 */
	private static Path compress(List<Path> unzipped) throws IOException {

		try {
			LOGGER.debug("Will compress {} unzipped package files", Integer.valueOf(unzipped.size()));
			Path zip = SharedOutputInputUtils.initTmpFile(FILE_ID, FILE_ZIP_EXT, false);
			ZipFile zipFile = new ZipFile(zip.toString());
			unzipped.forEach(p -> {
				try {
					LOGGER.debug("Adding file \"{}\" to zip destination \"{}\"", p, zip);
					zipFile.addFile(p.toFile(), ZIP_PARAMS);
				} catch (ZipException e) {
					throw new ApplicationException(EXPORT_ZIP_FAILED, "Cannot zip " + p, e);
				}
			});
			return zip;
		} catch (ZipException e) {
			throw new ApplicationException(EXPORT_ZIP_FAILED, "Cannot zip files " + unzipped);
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
	 * @param value
	 * @throws IOException
	 */
	private static void append(Path path, String value) {
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Appending to file \"{}\" content : \n{}", path, value);
			}
			Files.write(path, value.getBytes(CHARSET), StandardOpenOption.APPEND);
		} catch (IOException e) {
			throw new ApplicationException(EXPORT_WRONG_APPEND, "Cannot append to file " + path, e);
		}
	}

	/**
	 * @param path
	 * @return
	 */
	private static String readFile(Path path) {
		try {
			return new String(Files.readAllBytes(path), CHARSET);
		} catch (IOException e) {
			throw new ApplicationException(IMPORT_WRONG_READ, "Cannot read file " + path, e);
		}
	}

	/**
	 * @author elecomte
	 * @since v0.0.1
	 * @version 1
	 * @param <T>
	 */
	public abstract static class ExportImportPackage<T extends Shared> {

		private final String name;
		private final LocalDateTime exportDate;

		private List<T> contents;

		private Path uncompressPath;

		/**
		 * @param name
		 * @param exportDate
		 */
		protected ExportImportPackage(String name, LocalDateTime exportDate) {
			super();
			this.name = name;
			this.exportDate = exportDate;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * @return the exportDate
		 */
		public LocalDateTime getExportDate() {
			return this.exportDate;
		}

		/**
		 * Init content when creating a new package. Simple inline setter
		 * 
		 * @param lcontents
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public <K extends ExportImportPackage<T>> K initWithContent(List<T> lcontents) {
			this.contents = lcontents;
			return (K) this;
		}

		/**
		 * @return the content
		 */
		public Stream<T> streamContent() {
			return this.contents.stream();
		}

		/**
		 * @return the content
		 */
		public List<T> getContent() {
			return this.contents;
		}

		/**
		 * <p>
		 * Available only once content is initialized (directly or with deserialize)
		 * </p>
		 * 
		 * @return
		 */
		public int getContentSize() {
			return this.contents != null ? this.contents.size() : 0;
		}

		/**
		 * @return
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return this.getClass().getSimpleName() + "[\"" + this.getName() + "\"|" + this.getExportDate() + "|v\"" + this.getVersion()
					+ "\"]";
		}

		/**
		 * @return
		 */
		public abstract String getVersion();

		/**
		 * @param rawContent
		 * @return
		 */
		protected T deserializeOne(String rawContent) {
			T content = initContent();
			content.deserialize(rawContent);
			return content;
		}

		/**
		 * @return the uncompressPath
		 */
		protected Path getUncompressPath() {
			return this.uncompressPath;
		}

		/**
		 * @return
		 */
		protected abstract T initContent();

		/**
		 * @param uncompressPath
		 *            the uncompressPath to set
		 */
		private void setUncompressPath(Path uncompressPath) {
			this.uncompressPath = uncompressPath;
		}

		/**
		 * @param contentRaw
		 */
		private void deserialize(List<String> contentRaw) {
			this.contents = contentRaw.stream().map(this::deserializeOne).collect(Collectors.toList());
		}

		/**
		 * @return
		 */
		private List<String> serialize() {
			return this.contents.stream().map(Shared::serialize).collect(Collectors.toList());
		}
	}
}
