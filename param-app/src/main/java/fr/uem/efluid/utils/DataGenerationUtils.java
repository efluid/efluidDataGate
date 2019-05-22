package fr.uem.efluid.utils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.UUID;

import org.pac4j.core.credentials.password.PasswordEncoder;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.CommitState;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.IndexEntry;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.entities.TableLink;
import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.entities.Version;
import fr.uem.efluid.tools.ManagedValueConverter;

/**
 * @author elecomte
 * @version 2
 * @since v0.0.1
 */
public class DataGenerationUtils {

    /**
     * @param login
     * @return
     */
    public static User user(String login, PasswordEncoder encoder) {

        User user = new User(login);

        user.setEmail(login + "@efluid.fr");
        user.setPassword(encoder.encode(login));
        user.setToken(login);

        return user;
    }

    /**
     * @param name
     * @return
     */
    public static Version version(String name, Project project) {

        Version version = new Version(UUID.randomUUID());

        version.setCreatedTime(LocalDateTime.now());
        version.setUpdatedTime(version.getCreatedTime());
        version.setName(name);
        version.setProject(project);

        return version;
    }

    /**
     * @param login
     * @return
     */
    public static User user(String login) {

        User user = new User(login);

        user.setEmail(login + "@efluid.fr");
        user.setPassword("FPQSFIKQPSFIQSF[ENCRYPTED]");
        user.setToken(login);

        return user;
    }

    /**
     * @param name
     * @return
     */
    public static FunctionalDomain domain(String name, Project project) {

        FunctionalDomain domain = new FunctionalDomain();

        domain.setCreatedTime(LocalDateTime.now().minusDays(name.length()));
        domain.setUpdatedTime(domain.getCreatedTime().plusMinutes(1));
        domain.setUuid(UUID.randomUUID());
        domain.setName(name);
        domain.setProject(project);

        return domain;
    }

    /**
     * @param name
     * @param domain
     * @param select
     * @param table
     * @param where
     * @param keyName
     * @return
     */
    public static DictionaryEntry entry(String name, FunctionalDomain domain, String select, String table, String where, String keyName,
                                        ColumnType keyType) {

        DictionaryEntry entry = new DictionaryEntry();

        entry.setCreatedTime(LocalDateTime.now().minusDays(name.length()));
        entry.setUpdatedTime(entry.getCreatedTime().plusMinutes(1));
        entry.setUuid(UUID.randomUUID());
        entry.setParameterName(name);
        entry.setDomain(domain);
        entry.setSelectClause(select != null ? select.toUpperCase() : null);
        entry.setTableName(table);
        entry.setWhereClause(where);
        entry.setKeyName(keyName != null ? keyName.toUpperCase() : null);
        entry.setKeyType(keyType);
        return entry;
    }

    /**
     * @param key
     * @param action
     * @param payload
     * @param de
     * @param com
     * @return
     */
    public static IndexEntry update(String key, IndexAction action, String payload, DictionaryEntry de, Commit com) {

        IndexEntry entry = new IndexEntry();

        entry.setKeyValue(key);
        entry.setAction(action);
        entry.setDictionaryEntry(de);
        entry.setPayload(payload);
        entry.setCommit(com);

        return entry;
    }

    /**
     * @param name
     * @return
     */
    public static Project project(String name) {

        Project project = new Project();

        project.setUuid(UUID.randomUUID());
        project.setName(name);
        project.setCreatedTime(LocalDateTime.now().minusDays(10));
        project.setColor(2);

        return project;
    }

    /**
     * @param detail
     * @param user
     * @param daysOld
     * @param proj
     * @return
     */
    public static Commit commit(String detail, User user, int daysOld, Project proj, Version version) {

        Commit commit = new Commit();

        commit.setUuid(UUID.randomUUID());
        commit.setComment(detail);
        commit.setHash(detail);
        commit.setCreatedTime(LocalDateTime.now().minusDays(daysOld));
        commit.setState(CommitState.LOCAL);
        commit.setOriginalUserEmail(user.getEmail());
        commit.setUser(user);
        commit.setProject(proj);
        commit.setVersion(version);

        return commit;
    }

    /**
     * @param entry
     * @param colFrom
     * @param colTo
     * @param tableTo
     * @return
     */
    public static TableLink link(DictionaryEntry entry, String colFrom, String colTo, String tableTo) {

        TableLink link = new TableLink();

        link.setUuid(UUID.randomUUID());
        link.setColumnFrom(colFrom);
        link.setColumnTo(colTo);
        link.setTableTo(tableTo);
        link.setCreatedTime(entry.getCreatedTime());
        link.setUpdatedTime(entry.getUpdatedTime());
        link.setDictionaryEntry(entry);

        return link;
    }

    /**
     * Support string / integer only
     *
     * @param raw
     * @return
     */
    public static String content(String raw, ManagedValueConverter converter) {

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();

        String[] parts = raw.split(",");

        for (String part : parts) {
            String[] item = part.trim().split("=");
            String val = item[1].trim();
            result.put(item[0].trim(), val.charAt(0) == '"' ? val.substring(1, val.length() - 1) : Integer.decode(val));
        }

        return converter.convertToExtractedValue(result);
    }

}
