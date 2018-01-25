package fr.uem.efluid.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.FunctionalDomainRepository;
import fr.uem.efluid.services.types.DictionaryEntrySummary;
import fr.uem.efluid.services.types.FunctionalDomainData;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Service
@Transactional
public class DictionaryManagementService {

	@Autowired
	private FunctionalDomainRepository domains;

	@Autowired
	private DictionaryRepository dictionary;

	/**
	 * @return
	 */
	public List<FunctionalDomainData> getAvailableFunctionalDomains() {

		// TODO : keep this in cache, or precalculated (once used, cannot be "unused")
		List<UUID> usedIds = this.domains.findUsedIds();

		return this.domains.findAll().stream()
				.map(FunctionalDomainData::fromEntity)
				.peek(d -> d.setCanDelete(!usedIds.contains(d.getUuid())))
				.collect(Collectors.toList());
	}

	/**
	 * @param uuid
	 */
	public void deleteFunctionalDomain(UUID uuid) {

		assertDomainCanBeRemoved(uuid);

		this.domains.delete(uuid);
	}

	/**
	 * As summaries, for display or first level edit
	 * 
	 * @return
	 */
	public List<DictionaryEntrySummary> getDictionnaryEntrySummaries() {

		// TODO : keep this in cache, or precalculated (once used, cannot be "unused")
		List<UUID> usedIds = this.dictionary.findUsedIds();

		return this.dictionary.findAll().stream()
				.map(DictionaryEntrySummary::fromEntity)
				.peek(d -> d.setCanDelete(!usedIds.contains(d.getUuid())))
				.collect(Collectors.toList());
	}

	/**
	 * @param name
	 * @return
	 */
	public FunctionalDomainData createNewFunctionalDomain(String name) {

		FunctionalDomain domain = new FunctionalDomain();

		domain.setUuid(UUID.randomUUID());
		domain.setCreatedTime(LocalDateTime.now());
		domain.setName(name);

		this.domains.save(domain);

		return FunctionalDomainData.fromEntity(domain);
	}

	/**
	 * @param uuid
	 */
	private void assertDomainCanBeRemoved(UUID uuid) {

		if (this.domains.findUsedIds().contains(uuid)) {
			throw new IllegalArgumentException("FunctionalDomain with UUID " + uuid + " is used in index and therefore cannot be deleted");
		}
	}
}
