package fr.uem.efluid.plugin;

import fr.uem.efluid.generation.DictionaryExporter;
import fr.uem.efluid.generation.DictionaryGenerator;
import fr.uem.efluid.generation.DictionaryGeneratorConfig;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DictionaryGeneratorMojo extends AbstractMojo implements DictionaryGeneratorConfig {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(required = true, readonly = true)
    private String sourcePackage;

    @Parameter(defaultValue = "${project.basedir}/target", required = true, readonly = true)
    private String destinationFolder;

    @Parameter(defaultValue = "true", required = false, readonly = true)
    private boolean protectColumn;

    @Parameter(defaultValue = DictionaryGeneratorConfig.AUTO_GEN_DEST_FILE_DESG, required = false, readonly = true)
    private String destinationFileDesignation;

    @Parameter(defaultValue = "false", required = false, readonly = true)
    private boolean uploadToServer;

    @Parameter(required = false, readonly = true)
    private String uploadEntryPointUri;

    @Parameter(required = false, readonly = true)
    private String uploadSecurityToken;

    @Parameter(required = true, readonly = true)
    private String projectVersion;

    @Parameter(defaultValue = "false", required = false, readonly = true)
    private boolean checkDuplicateTables;

    /**
     * @throws MojoExecutionException
     * @throws MojoFailureException
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        getLog().info("###### Begin process for dictionary generation ######");

        try {
            DictionaryGenerator generator = new DictionaryGenerator(this);
            DictionaryExporter exporter = new DictionaryExporter(this);

            long start = System.currentTimeMillis();
            getLog().info("###### efluid-datagate-generator VERSION " + DictionaryGenerator.VERSION + " - START GENERATE ######");

            // Start generator using the classpath and custom logFacade
            exporter.export(
                    generator.extractDictionary(getMavenContextClassLoader())
            );

            getLog().info("###### efluid-datagate-generator VERSION " + DictionaryGenerator.VERSION + " - GENERATE COMPLETED IN "
                    + BigDecimal.valueOf(System.currentTimeMillis() - start, 3).toPlainString()
                    + " s ######");


        } catch (Exception e) {
            throw new MojoFailureException("Cannot process generation of client", e);
        }
    }

    /**
     * <p>
     * Dedicated init of classloader with access to all maven execution classpath, to
     * allow the use of current compiled source + external dependencies during generation.
     * </p>
     * <p>
     * We need to use the classpath from the project for class identification, including
     * compiled sources from trans-connected project in IDE. So define a custom
     * classloader with the maven project classpath.
     * </p>
     * <p>
     * Some hacks are related to Eclipse M2E : Eclipse m2e is bullshit, and system is not
     * compliant with maven project properties without custom m2e connector.
     * </p>
     *
     * @return
     * @throws Exception
     */
    private ClassLoader getMavenContextClassLoader() throws Exception {

        ClassLoader contextClassLoader;

        // Hack from project param
        if (this.project != null) {

            Set<URL> urls = new HashSet<>();
            List<String> elements = this.project.getTestClasspathElements();

            for (String element : elements) {
                urls.add(new File(element).toURI().toURL());
            }

            contextClassLoader = URLClassLoader.newInstance(urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());

        }

        // Try anyway with current classpath
        else {
            contextClassLoader = this.getClass().getClassLoader().getParent();
        }

        Thread.currentThread().setContextClassLoader(contextClassLoader);

        return contextClassLoader;
    }

    /**
     * @return the project
     */
    public MavenProject getProject() {
        return this.project;
    }

    /**
     * @return the sourcePackage
     */
    @Override
    public String getSourcePackage() {
        return this.sourcePackage;
    }

    @Override
    public Set<Class<?>> getSourceClasses() {
        // Not supported - null is ok
        return null;
    }

    /**
     * @return the destinationFolder
     */
    @Override
    public String getDestinationFolder() {
        return this.destinationFolder;
    }

    /**
     * @return the protectColumn
     */
    @Override
    public boolean isProtectColumn() {
        return this.protectColumn;
    }

    /**
     * @return the destinationFileDesignation
     */
    @Override
    public String getDestinationFileDesignation() {
        return this.destinationFileDesignation;
    }

    /**
     * @return the uploadToServer
     */
    @Override
    public boolean isUploadToServer() {
        return this.uploadToServer;
    }

    /**
     * @return the uploadEntryPointUri
     */
    @Override
    public String getUploadEntryPointUri() {
        // Support all model where entry point was at /dictionary
        return this.uploadEntryPointUri.replaceAll("/dictionary", "");
    }

    /**
     * @return the uploadSecurityToken
     */
    @Override
    public String getUploadSecurityToken() {
        return this.uploadSecurityToken;
    }

    /**
     * @return the projectVersion
     */
    @Override
    public String getProjectVersion() {
        return this.projectVersion;
    }

    @Override
    public boolean isCheckDuplicateTables() {
        return checkDuplicateTables;
    }

    /**
     * @return
     * @see fr.uem.efluid.generation.DictionaryGeneratorConfig#getLogger()
     */
    @Override
    public LogFacade getLogger() {
        return new LogFacade() {

            @Override
            public void debug(CharSequence var1) {
                getLog().debug(var1);
            }

            @Override
            public void info(CharSequence var1) {
                getLog().info(var1);
            }

            @Override
            public void error(CharSequence var1, Throwable var2) {
                getLog().error(var1, var2);
            }
        };
    }
}
