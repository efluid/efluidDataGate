package fr.uem.efluid.model;

import java.time.LocalDateTime;

/**
 * Specification of a model entity with checked update, for "archived" version model.
 * There is no "full" version support but a basic model for archived versions with
 * content specified as an export content on each created version.
 */
public interface UpdateChecked {

    LocalDateTime getCreatedTime();

    LocalDateTime getUpdatedTime();

}
