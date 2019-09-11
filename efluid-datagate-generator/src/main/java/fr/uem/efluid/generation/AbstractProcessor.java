package fr.uem.efluid.generation;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 * <p>Common definition of a processor for DataGate. Mostly provides config</p>
 *
 * @author elecomte
 * @version 1
 * @since v2.0.0
 */
public abstract class AbstractProcessor {

    private final DictionaryGeneratorConfig config;

    protected AbstractProcessor(DictionaryGeneratorConfig config) {
        this.config = config;
    }

    /**
     * <p>
     * Proxy to logger
     * </p>
     *
     * @return
     */
    protected DictionaryGeneratorConfig.LogFacade getLog() {
        return this.config.getLogger();
    }

    protected DictionaryGeneratorConfig config() {
        return this.config;
    }

    /**
     * @param contextClassLoader
     * @return
     */
    protected Reflections initReflectionEntryPoint(ClassLoader contextClassLoader) {

        return new Reflections(new ConfigurationBuilder()
                .addClassLoader(contextClassLoader)
                .setUrls(ClasspathHelper.forPackage(config().getSourcePackage()))
                .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner(), new FieldAnnotationsScanner(),
                        new MethodAnnotationsScanner()));
    }

}
