package fr.uem.efluid.utils;

import java.time.LocalDateTime;
import java.util.UUID;

import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.IndexEntry;
import fr.uem.efluid.model.entities.User;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class DataGenerationUtils {

	/**
	 * @param login
	 * @return
	 */
	public static User user(String login) {

		User user = new User(login);
		user.setEmail(login + "@efluid.fr");
		user.setPassword("FPQSFIKQPSFIQSF[ENCRYPTED]");

		return user;
	}

	/**
	 * @param name
	 * @return
	 */
	public static FunctionalDomain domain(String name) {
		FunctionalDomain domain = new FunctionalDomain();

		domain.setCreatedTime(LocalDateTime.now().minusDays(name.length()));
		domain.setUuid(UUID.randomUUID());
		domain.setName(name);

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
	public static DictionaryEntry entry(String name, FunctionalDomain domain, String select, String table, String where, String keyName) {

		DictionaryEntry entry = new DictionaryEntry();

		entry.setCreatedTime(LocalDateTime.now().minusDays(name.length()));
		entry.setUuid(UUID.randomUUID());
		entry.setParameterName(name);
		entry.setDomain(domain);
		entry.setSelectClause(select);
		entry.setTableName(table);
		entry.setWhereClause(where);
		entry.setKeyName(keyName);

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
	 * @param detail
	 * @param user
	 * @return
	 */
	public static Commit commit(String detail, User user, int daysOld) {
		Commit commit = new Commit();
		commit.setUuid(UUID.randomUUID());
		commit.setComment(detail);
		commit.setHash(detail);
		commit.setCreatedTime(LocalDateTime.now().minusDays(daysOld));
		commit.setImported(false);
		commit.setOriginalUserEmail(user.getEmail());
		commit.setUser(user);
		return commit;
	}

}
