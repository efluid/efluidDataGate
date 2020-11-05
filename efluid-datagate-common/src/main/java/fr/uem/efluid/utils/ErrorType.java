package fr.uem.efluid.utils;

/**
 * @author elecomte
 * @version 10
 * @since v0.0.1
 */
public enum ErrorType {

    OTHER,

    INIT_FAILED,

    WRONG_DS_TYPE,

    WRONG_CLIENT_CALL,

    /** Cannot process SQL extraction of current content */
    EXTRACTION_ERROR,

    REGENERATE_ERROR,

    PREPARATION_INTERRUPTED,
    PREPARATION_BIZ_FAILURE,
    PREPARATION_CANNOT_START,
    PREPARATION_NOT_READY,

    TABLE_NAME_INVALID,
    TABLE_WRONG_REF,

    UNSUPPORTED_UUID,

    COMMIT_IMPORT_INVALID,
    COMMIT_IMPORT_MISS_REF,
    COMMIT_EXISTS,
    COMMIT_MISS_COMMENT,

    METADATA_WRONG_TYPE,
    METADATA_WRONG_SCHEMA,
    METADATA_FAILED,
    METADATA_WRONG_TABLE,
    VALUE_CHECK_FAILED,
    VALUE_SHA_UNSUP,

    APPLY_FAILED,

    DIC_ENTRY_NOT_FOUND,
    DIC_NO_KEY,
    DIC_TOO_MANY_KEYS,
    DIC_NOT_REMOVABLE,
    DOMAIN_NOT_REMOVABLE,
    DIC_KEY_NOT_UNIQ,
    DOMAIN_NOT_EXIST,

    IMPORT_FAIL_FILE,
    EXPORT_FAIL_FILE,
    IMPORT_WRONG_TYPE,
    IMPORT_WRONG_VERSION,
    IMPORT_WRONG_INSTANCE,
    EXPORT_WRONG_APPEND,
    EXPORT_ZIP_FAILED,
    IMPORT_ZIP_FAILED,
    IMPORT_WRONG_READ,
    IMPORT_RUNNING,
    MERGE_DICT_NOT_COMPATIBLE,
    MERGE_FAILURE,
    MERGE_RESOLUTION_UNKNOWN,

    UPLOAD_WRG_DATA,

    TMP_ERROR,
    DATA_WRITE_ERROR,
    DATA_READ_ERROR,
    JSON_WRITE_ERROR,
    JSON_READ_ERROR,

    VERIFIED_APPLY_NOT_FOUND,

    REFER_MISS_LINK,

    PROJECT_MANDATORY,
    PROJECT_WRONG,
    PROJECT_NAME_EXIST,
    PROJECT_WRONG_ID,

    VERSION_NOT_EXIST,
    VERSION_NOT_UP_TO_DATE,
    VERSION_NOT_IMPORTED,
    VERSION_NOT_MODEL_ID,
    OUTPUT_ERROR,

    TRANSFORMER_CONFIG_WRONG,
    TRANSFORMER_NOT_FOUND,

    ATTACHMENT_ERROR,
    ATTACHMENT_EXEC_ERROR,
    LDAP_ERROR

}
