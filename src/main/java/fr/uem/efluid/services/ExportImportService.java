package fr.uem.efluid.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import fr.uem.efluid.model.ExportImportPackage;
import fr.uem.efluid.services.types.ExportImportFile;
import fr.uem.efluid.utils.TechnicalException;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Service
public class ExportImportService {

	private static final String PACKAGE_START = "[pack|%s|%s|%s]\n";

	private static final String PACKAGE_END = "[/pack]\n";

	private static final String ITEM_JOIN = "\n";

	private static final String RESOURCE_NAME = "/content.pckg";

	/**
	 * @param packages
	 * @return
	 */
	public ExportImportFile exportPackages(List<ExportImportPackage<?>> packages) {

		try {
			File tmp = File.createTempFile("export-package", "packs");
			try (FileWriter writer = new FileWriter(tmp)) {

				for (ExportImportPackage<?> pckg : packages) {

					// Start of package
					writer.write(String.format(PACKAGE_START, pckg.getName(), pckg.getExportDate(), pckg.getVersion()));

					// Package content
					pckg.serialize().forEach(s -> {
						try {
							writer.write(s + ITEM_JOIN);
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

	public List<ExportImportPackage<?>> importPackages(ExportImportFile file) {
		return null;
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
}
