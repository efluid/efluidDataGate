package fr.uem.efluid.generation;

import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import fr.uem.efluid.ParameterDomain;
import fr.uem.efluid.generation.DictionaryGeneratorConfig.LogFacade;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class DictionaryGenerator {

	private static final String VERSION = "1";

	private final DictionaryGeneratorConfig config;

	/**
	 * 
	 */
	public DictionaryGenerator(DictionaryGeneratorConfig config) {
		this.config = config;
	}

	/**
	 * @param contextClassLoader
	 */
	public void generateDictionaryExport(ClassLoader contextClassLoader) throws Exception {
		getLog().info("###### PARAM-GENERATOR VERSION " + VERSION + "######");

		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.addClassLoader(contextClassLoader)
				.setUrls(ClasspathHelper.forPackage(this.config.getSourcePackage()))
				.setScanners(new SubTypesScanner(), new TypeAnnotationsScanner(), new FieldAnnotationsScanner(),
						new MethodAnnotationsScanner()));

		// Domains
		Set<Class<?>> domains = reflections.getTypesAnnotatedWith(ParameterDomain.class);

		// TODO : continue implement. Use generator test
	}

	/**
	 * <p>
	 * Proxy to logger
	 * </p>
	 * 
	 * @return
	 */
	private LogFacade getLog() {
		return this.config.getLogger();
	}
}
