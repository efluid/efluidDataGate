package fr.uem.efluid.generation;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface DictionaryGeneratorConfig {

	String AUTO_GEN_DEST_FILE_DESG = "auto";
	
	String getSourcePackage();

	String getDestinationFolder();

	boolean isProtectColumn();

	LogFacade getLogger();
	
	String getDestinationFileDesignation();

	boolean isUploadToServer();
	
	String getUploadEntryPointUri();
	
	String getUploadSecurityToken();
	
	String getProjectVersion();
	
	/**
	 * <p>
	 * For shared logging using source logger. As in maven plugin process we have to use a
	 * dedicated logger, this facade allows to log transparently during generation process
	 * </p>
	 * 
	 * @author elecomte
	 * @since v2.1.0
	 * @version 1
	 */
	interface LogFacade {

		void debug(CharSequence var1);

		void info(CharSequence var1);

		void error(CharSequence var1, Throwable var2);
	}
}
