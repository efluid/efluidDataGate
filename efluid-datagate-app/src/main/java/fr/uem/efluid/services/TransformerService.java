package fr.uem.efluid.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uem.efluid.model.entities.ExportTransformer;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.entities.TransformerDef;
import fr.uem.efluid.model.repositories.TransformerDefRepository;
import fr.uem.efluid.services.types.TransformerDefDisplay;
import fr.uem.efluid.services.types.TransformerDefEditData;
import fr.uem.efluid.services.types.TransformerDefPackage;
import fr.uem.efluid.services.types.TransformerType;
import fr.uem.efluid.tools.ManagedValueConverter;
import fr.uem.efluid.transformers.Transformer;
import fr.uem.efluid.transformers.TransformerProcessor;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * Management and use of transformers :
 * <ul>
 *     <li>Management of TransformerDef data</li>
 *     <li>Process transformer on diffs</li>
 * </ul>
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v1.2.0
 */
@Service
@Transactional
public class TransformerService extends AbstractApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformerService.class);

    @Autowired
    private TransformerDefRepository transformerDefs;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private List<Transformer<?, ?>> availableTransformers;

    @Autowired
    private ProjectManagementService projectService;

    @Autowired
    private JdbcTemplate managedSource;

    @Autowired
    private ManagedValueConverter valueConverter;

    /**
     * Get all available transformer types to list on edit page or for transformer use
     *
     * @return available types for current Datagate instance
     */
    public List<TransformerType> getAllTransformerTypes() {
        return this.availableTransformers.stream().map(TransformerType::new).collect(Collectors.toList());
    }

    /**
     * For edit page, list all specified transformer defs
     *
     * @param loadPackageComment true if the optional load package comment has to be included
     * @return all transformer defs from current project
     */
    public List<TransformerDefDisplay> getAllTransformerDefs(boolean loadPackageComment) {
        this.projectService.assertCurrentUserHasSelectedProject();

        // For display of user friendly name of transformer type
        Map<String, String> transformerNameByType = this.availableTransformers.stream()
                .collect(Collectors.toMap(t -> t.getClass().getSimpleName(), Transformer::getName));

        return this.transformerDefs.findByProjectAndDeletedTimeIsNull(this.projectService.getCurrentSelectedProjectEntity()).stream()
                .map(def -> {
                    String packageComment = null;

                    // If package comment has to be loaded, the transformer must be fully initialized
                    if (loadPackageComment) {
                        Transformer<?, ?> transformer = loadTransformerByType(def.getType());
                        Transformer.TransformerConfig config = parseConfiguration(def.getConfiguration(), transformer);
                        packageComment = config.isAttachmentPackageSupport()
                                ? config.getAttachmentPackageComment(this.managedSource, this.valueConverter) : null;
                    }
                    return new TransformerDefDisplay(def, transformerNameByType.get(def.getType()), packageComment);
                })
                .collect(Collectors.toList());
    }

    /**
     * For large scale config edition, provides all configuration mapped to their transformer def UUID
     *
     * @return config for transformer defs uuid, on current project
     */
    public Map<UUID, String> getAllTransformerDefConfigs() {
        return this.transformerDefs.findByProjectAndDeletedTimeIsNull(this.projectService.getCurrentSelectedProjectEntity()).stream()
                .collect(Collectors.toMap(TransformerDef::getUuid, def -> {
                    Transformer<?, ?> transformer = loadTransformerByType(def.getType());
                    return prettyConfig(parseConfiguration(def.getConfiguration(), transformer));
                }));
    }

    /**
     * Init a def ready to be completed for specified type
     *
     * @param type selected type for new def
     * @return prepared edit data bean
     * @throws ApplicationException if type is invalid
     */
    public TransformerDefEditData prepareNewTransformerDef(String type) {

        Transformer<?, ?> transformer = loadTransformerByType(type);

        TransformerDefEditData editData = new TransformerDefEditData();
        Transformer.TransformerConfig config = transformer.getDefaultConfig();
        editData.setName(transformer.getName());
        editData.setConfiguration(prettyConfig(config));
        editData.setPriority(1);
        editData.setPackageComment(config.isAttachmentPackageSupport()
                ? config.getAttachmentPackageComment(this.managedSource, this.valueConverter) : null);

        editData.setTransformer(transformer);

        return editData;
    }

    /**
     * Provides a default transformer config as a formated raw json string
     *
     * @param type specified transformer type identifier
     * @return config content
     */
    public String getDefaultConfigRawJson(String type) {

        Transformer<?, ?> transformer = loadTransformerByType(type);

        return prettyConfig(transformer.getDefaultConfig());
    }

    /**
     * Validate the provided configuration : do not throw, but return back validation details in a string.
     * If null result, the configuration is OK for specified transformer
     *
     * @param type             specified transformer for which the configuration can be processed
     * @param rawConfiguration the content to validate for the configuration
     * @return null if no error, or complete details on errors
     */
    public String validateConfiguration(String type, String rawConfiguration) {

        try {
            Transformer<?, ?> transformer = loadTransformerByType(type);
            parseConfiguration(rawConfiguration, transformer);
        } catch (ApplicationException e) {
            return e.getPayload();
        }

        return null;
    }

    /**
     * Get an existing def for given id and prepare data to edit
     *
     * @param uuid selected def id
     * @return found edit data
     * @throws ApplicationException if not found
     */
    public TransformerDefEditData editTransformerDef(UUID uuid) {

        TransformerDef def = this.transformerDefs.findByUuidAndDeletedTimeIsNull(uuid).orElseThrow(() -> new ApplicationException(ErrorType.TRANSFORMER_NOT_FOUND));
        Transformer<?, ?> transformer = loadTransformerByType(def.getType());
        Transformer.TransformerConfig config = parseConfiguration(def.getConfiguration(), transformer);
        String prettyCfg = prettyConfig(config);
        String packageComment = config.isAttachmentPackageSupport()
                ? config.getAttachmentPackageComment(this.managedSource, this.valueConverter) : null;

        return TransformerDefEditData.fromEntity(def, transformer, prettyCfg, packageComment);
    }

    /**
     * Drop a specified transformer def
     *
     * @param uuid of def to delete
     */
    public void deleteTransformerDef(UUID uuid) {
        TransformerDef def = this.transformerDefs.findByUuidAndDeletedTimeIsNull(uuid).orElseThrow(() -> new ApplicationException(ErrorType.TRANSFORMER_NOT_FOUND));
        def.setDeletedTime(LocalDateTime.now());
        this.transformerDefs.save(def);
    }

    /**
     * Save edited def
     *
     * @param editData user defined def content
     */
    public void saveTransformerDef(TransformerDefEditData editData) {

        this.projectService.assertCurrentUserHasSelectedProject();

        Transformer<?, ?> transformer = loadTransformerByType(editData.getType());
        Transformer.TransformerConfig config = parseConfiguration(editData.getConfiguration(), transformer);

        TransformerDef def;

        if (editData.getUuid() != null) {
            def = this.transformerDefs.findByUuidAndDeletedTimeIsNull(editData.getUuid())
                    .orElseThrow(() -> new ApplicationException(ErrorType.TRANSFORMER_NOT_FOUND));
        } else {
            def = new TransformerDef();
            def.setUuid(UUID.randomUUID());
            def.setCreatedTime(LocalDateTime.now());
            def.setType(editData.getType());
            def.setProject(this.projectService.getCurrentSelectedProjectEntity());
        }

        def.setUpdatedTime(LocalDateTime.now());

        def.setName(editData.getName());
        def.setPriority(editData.getPriority());
        try {
            // No pretty for inner content, for compacity
            def.setConfiguration(this.mapper.writeValueAsString(config));
        } catch (JsonProcessingException e) {
            throw new ApplicationException(ErrorType.TRANSFORMER_CONFIG_WRONG,
                    "Cannot process json content", e, "Parsing error : " + e.getMessage());
        }

        this.transformerDefs.save(def);
    }

    /**
     * Get an available transformer for a specified type
     *
     * @param type inner type
     * @return available transformer
     */
    public Transformer<?, ?> loadTransformerByType(String type) {
        return this.availableTransformers.stream()
                .filter(t -> t.getClass().getSimpleName().equals(type))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(ErrorType.TRANSFORMER_NOT_FOUND));
    }

    /**
     * <p>From an import package of TransformerDefs, init locally and prepare a processor for merge transformation</p>
     * <p>Return Null if no content identified, as the processor is optionally present</p>
     *
     * @param pckg processing import package
     * @return processor ready to apply transformers onto commit diffs in merge process.
     */
    TransformerProcessor importTransformerDefsAndPrepareProcessor(TransformerDefPackage pckg) {

        // Prepare processor from defs ...
        return new TransformerProcessor(
                // On imported defs ...
                pckg.content()
                        .map(p -> {
                            // ... Store them locally ...
                            TransformerDef tDef = importTransformerDef(p);
                            // ... And init them as "transformer Apply" used in TranformerProcessor
                            Transformer<?, ?> transformer = loadTransformerByType(tDef.getType());
                            Transformer.TransformerConfig config = parseConfiguration(tDef.getConfiguration(), transformer);
                            // ... If an attachment package exist, load it
                            config.importAttachmentPackageData(p.getAttachmentPackage(), this.managedSource, this.valueConverter);
                            // ... Finalize processor for transformer use
                            return new TransformerProcessor.TransformerApply(tDef.getName(), transformer, config, tDef.getPriority());
                        }).collect(Collectors.toList())
        );
    }

    /**
     * Prepare TransformerDefs for export with applied customization (if any).
     * Keep only customization which are different than existing content.
     * <p>
     * Load also any associated attachment package
     *
     * @param project        associated project
     * @param customizations specified customizations for export
     * @return transformer def ready to be exported, with applied customizations
     */
    Stream<TransformerDef> getCustomizedTransformerDefForExport(Project project, Collection<ExportTransformer> customizations) {

        Map<UUID, String> confs = customizations.stream().collect(Collectors.toMap(c -> c.getTransformerDef().getUuid(), ExportTransformer::getConfiguration));
        Set<UUID> disabled = customizations.stream().filter(ExportTransformer::isDisabled).map(t -> t.getTransformerDef().getUuid()).collect(Collectors.toSet());

        return this.transformerDefs.findByProjectAndDeletedTimeIsNull(project).stream()
                // Remove disabled transformers ...
                .filter(t -> !disabled.contains(t.getUuid()))
                // ... And apply updated configs
                .peek(t -> {
                    String customization = confs.get(t.getUuid());
                    if (customization != null && !customization.equals(t.getConfiguration())) {
                        t.setCustomizedConfiguration(customization);
                    }
                    loadTransformerAttachmentPackage(t);
                });
    }

    private TransformerDef importTransformerDef(TransformerDef imported) {

        Optional<TransformerDef> localOpt = this.transformerDefs.findById(imported.getUuid());

        // Exists already
        localOpt.ifPresent(d -> LOGGER.debug("Import existing TransformerDef {} : will update currently owned", imported.getUuid()));

        // Or is a new one
        TransformerDef local = localOpt.orElseGet(() -> {
            LOGGER.debug("Import new TransformerDef {} : will create currently owned", imported.getUuid());
            TransformerDef loc = new TransformerDef(imported.getUuid());
            loc.setCreatedTime(imported.getCreatedTime());
            return loc;
        });

        // Common attrs
        local.setUpdatedTime(imported.getUpdatedTime());
        local.setType(imported.getType());
        local.setPriority(imported.getPriority());
        local.setName(imported.getName());
        local.setProject(imported.getProject());
        local.setConfiguration(imported.getConfiguration());
        local.setImportedTime(LocalDateTime.now());
        local.setDeletedTime(imported.getDeletedTime());

        return local;
    }

    /**
     * Load any optional attachment packages from the transformer def, if any specified in
     * transformer configuration
     *
     * @param transformerDef transformer def where the data load will be processed
     */
    private void loadTransformerAttachmentPackage(TransformerDef transformerDef) {

        // We need to instantiate transformer components including config
        Transformer<?, ?> transformer = loadTransformerByType(transformerDef.getType());

        // Apply immediately customization if any is specified
        Transformer.TransformerConfig config = parseConfiguration(
                transformerDef.getCustomizedConfiguration() != null
                        ? transformerDef.getCustomizedConfiguration()
                        : transformerDef.getConfiguration(), transformer);

        // If attachment package support
        if (config.isAttachmentPackageSupport()) {

            // Call for data load, if specified in config impl
            byte[] attachementData = config.exportAttachmentPackageData(this.managedSource);

            // Then apply loaded data, if any (can be null)
            transformerDef.setAttachmentPackage(attachementData);
        }

    }

    private String prettyConfig(Transformer.TransformerConfig config) {
        try {
            return this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
        } catch (JsonProcessingException e) {
            throw new ApplicationException(ErrorType.TRANSFORMER_CONFIG_WRONG,
                    "Cannot process json content", e, "Parsing error : " + e.getMessage());
        }
    }

    /**
     * Process raw content of config as a clean config type
     *
     * @param rawValue    string configuration to convert
     * @param transformer corresponding transformer, for config type access and
     * @param <C>         transformer configuration type
     * @return validated configuration
     */
    @SuppressWarnings("unchecked")
    private <C extends Transformer.TransformerConfig> C parseConfiguration(String rawValue, Transformer<C, ?> transformer) {

        try {
            C config = (C) this.mapper.readValue(rawValue, transformer.getDefaultConfig().getClass());
            transformer.validateConfig(config);
            return config;
        } catch (IOException e) {
            throw new ApplicationException(ErrorType.TRANSFORMER_CONFIG_WRONG,
                    "Cannot process json content", e, "JSON Parsing error : " + e.getMessage());
        }
    }
}
