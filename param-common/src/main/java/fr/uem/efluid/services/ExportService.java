package fr.uem.efluid.services;

import static fr.uem.efluid.utils.ErrorType.EXPORT_FAIL_FILE;
import static fr.uem.efluid.utils.ErrorType.EXPORT_WRONG_APPEND;
import static fr.uem.efluid.utils.ErrorType.EXPORT_ZIP_FAILED;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import fr.uem.efluid.services.types.ExportFile;
import fr.uem.efluid.services.types.SharedPackage;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.SharedOutputInputUtils;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

/**
 * <p>
 * Internal service for export using custom package model. At this level the import
 * features ARE NOT implemented
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class ExportService {

	protected static final String PACKAGE_START = "[pack|%s|%s|%s|%s]\n";

	protected static final String PACKAGE_END = "[/pack]\n";

	protected static final String ITEM_START = "[item]";

	protected static final String ITEM_END = "[/item]\n";

	protected static final Charset CHARSET = Charset.forName("utf8");

	protected static final String FILE_PCKG_EXT = ".packs";

	protected static final String FILE_ZIP_EXT = ".par";

	protected static final String FILE_ID = "export-package";

	protected final static ZipParameters ZIP_PARAMS = new ZipParameters();

	static {
		ZIP_PARAMS.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		ZIP_PARAMS.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
	}

	/**
	 * @param packages
	 * @return
	 */
	public ExportFile exportPackages(List<SharedPackage<?>> packages) {

		try {

			Path workFolder = SharedOutputInputUtils.initTmpFolder();
			List<Path> packFiles = new ArrayList<>();

			for (SharedPackage<?> pckg : packages) {

				pckg.setUncompressPath(workFolder);

				Path pckgFile = Files.createFile(workFolder.resolve(pckg.getName() + FILE_PCKG_EXT));

				// Identifier at start of package
				append(pckgFile,
						String.format(PACKAGE_START, pckg.getClass().getName(), pckg.getName(), pckg.getExportDate(), pckg.getVersion()));

				// Package content
				pckg.serialize().forEach(s -> append(pckgFile, ITEM_START + s + ITEM_END));

				// End of package
				append(pckgFile, PACKAGE_END);

				packFiles.add(pckgFile);

				// If package has complementary files, add them
				packFiles.addAll(pckg.getComplementaryFiles());
			}

			// TODO : define CType
			return new ExportFile(compress(packFiles), "PCKG");

		} catch (IOException e) {
			throw new ApplicationException(EXPORT_FAIL_FILE, "Cannot process export on " + packages.size() + " packages", e);
		}
	}

	/**
	 * @param unzipped
	 * @return
	 * @throws IOException
	 */
	private static Path compress(List<Path> unzipped) throws IOException {

		try {
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
		} catch (ZipException e) {
			throw new ApplicationException(EXPORT_ZIP_FAILED, "Cannot zip files " + unzipped);
		}
	}

	/**
	 * @param path
	 * @param value
	 * @throws IOException
	 */
	private static void append(Path path, String value) {
		try {
			Files.write(path, value.getBytes(CHARSET), StandardOpenOption.APPEND);
		} catch (IOException e) {
			throw new ApplicationException(EXPORT_WRONG_APPEND, "Cannot append to file " + path, e);
		}
	}
}
