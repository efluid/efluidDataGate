package fr.uem.efluid.generation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public interface DictionaryGeneratorConfig {

    String AUTO_GEN_DEST_FILE_DESG = "auto";

    String getSourcePackage();

    @Nullable
    Set<Class<?>> getSourceClasses();

    String getDestinationFolder();

    boolean isProtectColumn();

    LogFacade getLogger();

    String getDestinationFileDesignation();

    boolean isUploadToServer();

    String getUploadEntryPointUri();

    String getUploadSecurityToken();

    String getProjectVersion();

    @Deprecated
    boolean isCheckDuplicateTables();


    /**
     * <p>
     * For shared logging using source logger. As in maven plugin process we have to use a
     * dedicated logger, this facade allows to log transparently during generation process
     * </p>
     *
     * @author elecomte
     * @version 1
     * @since v2.1.0
     */
    interface LogFacade {

        void debug(CharSequence var1);

        void info(CharSequence var1);

        void error(CharSequence var1, Throwable var2);
    }
}
