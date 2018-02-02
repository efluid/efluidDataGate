package fr.uem.efluid.services;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import fr.uem.efluid.model.repositories.CommitRepository;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.FunctionalDomainRepository;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.services.types.ApplicationDetails;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Service
public class ApplicationDetailsService {

	private static final long INDEX_ENTRY_ESTIMATED_SIZE = 1024;

	@Autowired
	private CommitRepository commits;

	@Autowired
	private IndexRepository index;

	@Autowired
	private FunctionalDomainRepository domains;

	@Autowired
	private DictionaryRepository dictionary;

	@Value("${param-efluid.managed-datasource.url}")
	private String managedDbUrl;

	/**
	 *
	 * @return
	 */
	@Cacheable("details")
	public ApplicationDetails getCurrentDetails() {

		ApplicationDetails details = new ApplicationDetails();

		details.setCommitsCount(this.commits.count());
		details.setDbUrl(this.managedDbUrl);
		details.setDomainsCount(this.domains.count());
		details.setDictionaryCount(this.dictionary.count());
		details.setIndexSize(getEstimatedIndexSize());

		return details;
	}

	/**
	 * @return
	 */
	private String getEstimatedIndexSize() {
		long size = this.index.count() * INDEX_ENTRY_ESTIMATED_SIZE;
		BigDecimal estim = new BigDecimal(size / (1024 * 1024));
		estim.setScale(1, RoundingMode.HALF_UP);

		return estim.toPlainString() + " Mb";
	}
}
