package fr.uem.efluid.tools;

import fr.uem.efluid.utils.FormatUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * A basic extensible component providing some standard values for Transformer needs.
 * Can be extended for test purpose
 * @since v2.0.4
 * @version 1
 * @author elecomte
 */
@Component
public class TransformerValueProvider {

    public String getFormatedCurrentTime(){
        return FormatUtils.format(LocalDateTime.now());
    }

}
