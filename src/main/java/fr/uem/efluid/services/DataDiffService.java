package fr.uem.efluid.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexEntry;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.ManagedParametersRepository;

/**
 * <p>
 * Core service for diff processes
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Service
@Transactional
public class DataDiffService {

	// TODO : rename this service

	@Autowired
	private DictionaryRepository dictionary;

	@Autowired
	private ManagedParametersRepository rawParameters;

	/**
	 * @param dictionaryEntryUuid
	 * @return
	 */
	public List<IndexEntry> processDiff(UUID dictionaryEntryUuid) {

		// Here the main complexity : diff check using JDBC. Backlog construction +
		// restoration

		DictionaryEntry entry = this.dictionary.findOne(dictionaryEntryUuid);

		Map<String, String> knewContent = this.rawParameters.regenerateKnewContent(entry);
		Map<String, String> actualContent = this.rawParameters.extractCurrentContent(entry);

		return generateDiffIndex(knewContent, actualContent);
	}

	/**
	 * @param knewContent
	 * @param actualContent
	 * @return
	 */
	private static List<IndexEntry> generateDiffIndex(Map<String, String> knewContent, Map<String, String> actualContent) {

		// TODO : add tested diff, regarding various strategy
		return new ArrayList<>();
	}
}
