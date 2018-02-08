package fr.uem.efluid.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import fr.uem.efluid.model.Shared;
import fr.uem.efluid.services.types.ExportImportFile;
import fr.uem.efluid.utils.TechnicalException;

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

	private static final String PACKAGE_START = "[pack|%s|%s|%s|%s]\n";

	private static final String PACKAGE_END = "[/pack]\n";

	private static final String ITEM_START = "[item]";

	private static final String ITEM_END = "[/item]\n";

	private static final Charset CHARSET = Charset.forName("utf8");

	private static final String RESOURCE_NAME = "/content.pckg";

	/**
	 * @param packages
	 * @return
	 */
	ExportImportFile exportPackages(List<ExportImportPackage<?>> packages) {

		try {
			File tmp = File.createTempFile("export-package", "packs");
			try (FileWriter writer = new FileWriter(tmp)) {

				for (ExportImportPackage<?> pckg : packages) {

					// Start of package
					writer.write(String.format(PACKAGE_START, pckg.getClass(), pckg.getName(), pckg.getExportDate(), pckg.getVersion()));

					// Package content
					pckg.serialize().forEach(s -> {
						try {
							writer.write(ITEM_START + s + ITEM_END);
						} catch (IOException e) {
							throw new TechnicalException("Cannot write " + s, e);
						}
					});

					// End of package
					writer.write(PACKAGE_END);
				}

				writer.flush();
				writer.close();

				// TODO : define CType
				return new ExportImportFile(compress(tmp), "PCKG");
			}

		} catch (IOException e) {
			throw new TechnicalException("Cannot process export on " + packages.size() + " packages", e);
		}

	}

	/**
	 * @param file
	 * @return
	 */
	List<ExportImportPackage<?>> importPackages(ExportImportFile file) {

		String whole = new String(file.getData(), CHARSET);

		return Stream.of(whole.split(PACKAGE_END)).map(this::readPackage).collect(Collectors.toList());
	}

	/**
	 * @param pack
	 * @return
	 */
	private ExportImportPackage<?> readPackage(String pack) {

		try {
			String[] items = pack.split(ITEM_START);

			// Pack def is first item
			// [pack|%s|%s|%s|%s]\n
			// Class, name, date, version
			String[] packParams = items[0].substring(6, items[0].length() - 2).split("\\|");
			Class<?> type = Class.forName(packParams[0]);

			// Check type is valid
			if (!ExportImportPackage.class.isAssignableFrom(type)) {
				throw new TechnicalException("Processing of package type load is invalid. Type " + type + " is not a package");
			}

			// Init instance
			ExportImportPackage<?> instance = (ExportImportPackage<?>) type.getConstructor(String.class, LocalDateTime.class)
					.newInstance(packParams[1], LocalDateTime.parse(packParams[2]));

			// Must have same version
			if (!instance.getVersion().equals(packParams[3])) {
				throw new TechnicalException("Processing of package type load is invalid. Version " + packParams[3]
						+ " is not supported (current version is " + instance.getVersion() + ")");
			}

			List<String> itemSerialized = new ArrayList<>();

			// Get items
			for (int i = 1; i < items.length; i++) {
				itemSerialized.add(items[i].substring(0, items[i].length() - ITEM_END.length()));
			}

			// Deserialize => prepare content
			instance.deserialize(itemSerialized);

			// Package ready, with imported content
			return instance;
		}

		// From instance init
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new TechnicalException("Processing of package type load is invalid. Check type", e);
		}
	}

	/**
	 * @param tmp
	 * @return
	 * @throws IOException
	 */
	private static File compress(File tmp) throws IOException {
		File zip = File.createTempFile("export-package", "zip");
		Map<String, String> env = new HashMap<>();
		env.put("create", "true");

		URI uri = tmp.toURI();

		try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
			Path pathInZipfile = zipfs.getPath(RESOURCE_NAME);
			// copy a file into the zip file
			Files.copy(zip.toPath(), pathInZipfile, StandardCopyOption.REPLACE_EXISTING);
		}
		return zip;
	}

	public abstract class ExportImportPackage<T extends Shared> {

		private final String name;
		private final LocalDateTime exportDate;

		private List<T> contents;

		/**
		 * @param name
		 * @param exportDate
		 */
		public ExportImportPackage(String name, LocalDateTime exportDate) {
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
		 * @return the content
		 */
		public List<T> getContent() {
			return this.contents;
		}

		/**
		 * @param contentRaw
		 */
		private void deserialize(List<String> contentRaw) {

			this.contents = contentRaw.stream().map(s -> {
				T content = initContent();
				content.deserialize(s);
				return content;
			}).collect(Collectors.toList());

		}

		/**
		 * @return
		 */
		private List<String> serialize() {

			return this.contents.stream().map(Shared::serialize).collect(Collectors.toList());
		}

		/**
		 * @return
		 */
		public abstract String getVersion();

		/**
		 * @return
		 */
		protected abstract T initContent();

	}
}
