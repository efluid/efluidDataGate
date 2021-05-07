package fr.uem.efluid.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * <p>
 * Spec for an Share entity from the dictionary
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v2.1.18
 */
public interface SharedDictionary extends Shared, UpdateChecked {

    void setUuid(UUID uuid);

    void setCreatedTime(LocalDateTime time);

    void setUpdatedTime(LocalDateTime time);

    void setImportedTime(LocalDateTime time);
}
