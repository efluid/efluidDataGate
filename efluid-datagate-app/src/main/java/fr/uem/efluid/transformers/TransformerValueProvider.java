package fr.uem.efluid.transformers;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.tools.ManagedQueriesGenerator;
import fr.uem.efluid.utils.FormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * A basic extensible component providing some standard values for Transformer needs.
 * Can be extended for test purpose
 *
 * @author elecomte
 * @version 1
 * @since v2.0.4
 */
@Component
public class TransformerValueProvider {

    private final ManagedQueriesGenerator queryGenerator;

    public TransformerValueProvider(@Autowired ManagedQueriesGenerator queryGenerator) {
        this.queryGenerator = queryGenerator;
    }

    public String getFormatedCurrentTime() {
        return FormatUtils.format(LocalDateTime.now());
    }

    public List<String> getDictionaryEntryColumns(DictionaryEntry dict) {
        return StringUtils.hasText(dict.getSelectClause())
                ? this.queryGenerator.splitSelectClause(dict.getSelectClause(), null, null)
                : Collections.emptyList();
    }
}
