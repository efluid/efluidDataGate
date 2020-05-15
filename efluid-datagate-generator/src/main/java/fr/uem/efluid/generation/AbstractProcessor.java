package fr.uem.efluid.generation;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

        List<URL> searchUrls = new ArrayList<>();

        if (config().getSourcePackage() != null) {
            searchUrls.addAll(ClasspathHelper.forPackage(config().getSourcePackage()));
        }

        if (config().getSourceClasses() != null) {
            searchUrls.addAll(Objects.requireNonNull(config().getSourceClasses()).stream().map(ClasspathHelper::forClass).collect(Collectors.toSet()));
        }

        return new Reflections(new ConfigurationBuilder()
                .addClassLoader(contextClassLoader)
                .setUrls(searchUrls)
                .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner(), new FieldAnnotationsScanner(),
                        new MethodAnnotationsScanner()));
    }

    protected boolean isValidMember(Class<?> type) {
        if (config().getSourcePackage() == null) {
            return Objects.requireNonNull(config().getSourceClasses()).contains(type);
        }
        return type.getPackageName().startsWith(config().getSourcePackage());
    }

}
