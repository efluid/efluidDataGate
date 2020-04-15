package fr.uem.efluid.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uem.efluid.model.entities.TransformerDef;
import fr.uem.efluid.model.repositories.TransformerDefRepository;
import fr.uem.efluid.services.types.TransformerDefDisplay;
import fr.uem.efluid.services.types.TransformerDefEditData;
import fr.uem.efluid.services.types.TransformerType;
import fr.uem.efluid.tools.Transformer;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
 * @version 1
 * @since v1.2.0
 */
@Service
@Transactional
public class TransformerService extends AbstractApplicationService {

    @Autowired
    private TransformerDefRepository transformerDefs;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private List<Transformer<?, ?>> availableTransformers;

    @Autowired
    private ProjectManagementService projectService;

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
     * @return all transformer defs from current project
     */
    public List<TransformerDefDisplay> getAllTransformerDefs() {
        this.projectService.assertCurrentUserHasSelectedProject();

        // For display of user friendly name of transformer type
        Map<String, String> transformerNameByType = this.availableTransformers.stream()
                .collect(Collectors.toMap(t -> t.getClass().getSimpleName(), Transformer::getName));

        return this.transformerDefs.findByProject(this.projectService.getCurrentSelectedProjectEntity()).stream()
                .map(d -> new TransformerDefDisplay(d, transformerNameByType.get(d.getType())))
                .collect(Collectors.toList());
    }

    /**
     * For large scale config edition, provides all configuration mapped to their transformer def UUID
     *
     * @return config for transformer defs uuid, on current project
     */
    public Map<UUID, String> getAllTransformerDefConfigs() {
        return this.transformerDefs.findByProject(this.projectService.getCurrentSelectedProjectEntity()).stream()
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

        editData.setName(transformer.getName());
        editData.setConfiguration(prettyConfig(transformer.getDefaultConfig()));
        editData.setPriority(1);

        editData.setTransformer(transformer);

        return editData;
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

        TransformerDef def = this.transformerDefs.findById(uuid).orElseThrow(() -> new ApplicationException(ErrorType.TRANSFORMER_NOT_FOUND));
        Transformer<?, ?> transformer = loadTransformerByType(def.getType());
        String prettyCfg = prettyConfig(parseConfiguration(def.getConfiguration(), transformer));

        return TransformerDefEditData.fromEntity(def, transformer, prettyCfg);
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
            def = this.transformerDefs.findById(editData.getUuid())
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
