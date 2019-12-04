package fr.uem.efluid.model;

import fr.uem.efluid.services.types.TableLinkExportPackage;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public class GeneratedTableLinkPackage extends TableLinkExportPackage<ParameterLinkDefinition> {

    private static final String RELOADABLE_TYPE = "fr.uem.efluid.services.types.TableLinkPackage";

    /**
     * @param allLinks all included entities in package
     */
    public GeneratedTableLinkPackage(Collection<ParameterLinkDefinition> allLinks) {
        super(TableLinkExportPackage.LINKS_EXPORT, LocalDateTime.now());
        initWithContent(allLinks.stream().sorted(Comparator.comparing(ParameterLinkDefinition::getTableTo))
                .collect(Collectors.toList()));
    }

    /**
     * @see fr.uem.efluid.services.types.SharedPackage#initContent()
     */
    @Override
    protected ParameterLinkDefinition initContent() {
        return new ParameterLinkDefinition();
    }

    /**
     * @see fr.uem.efluid.services.types.SharedPackage#getReloadableTypeName()
     */
    @Override
    public String getReloadableTypeName() {
        // Allows reload at import from app
        return RELOADABLE_TYPE;
    }
}