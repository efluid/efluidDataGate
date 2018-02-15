package fr.uem.efluid.config;

import static fr.uem.efluid.utils.DataGenerationUtils.domain;
import static fr.uem.efluid.utils.DataGenerationUtils.entry;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.metas.ColumnType;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.FunctionalDomainRepository;
import fr.uem.efluid.model.repositories.UserRepository;
import fr.uem.efluid.services.AbstractApplicationService;

/**
 * <p>
 * Create some minimal values for easy import testing
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Configuration
@Profile("minimal")
@Transactional
public class MinimalDataLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(MinimalDataLoader.class);

	@Autowired
	private FunctionalDomainRepository domains;

	@Autowired
	private DictionaryRepository dictionary;

	@Autowired
	private UserRepository users;

	@PostConstruct
	public void initValues() {

		LOGGER.info("[MINIMAL] Init Minimal values for testing");

		this.users.save(AbstractApplicationService.FAKE_USER);
		FunctionalDomain dom1 = this.domains.save(domain("Minimal"));
		this.dictionary.save(entry("Minimal dic", dom1, "\"WEIGHT\", \"LAST_UPDATED\"", "NOT_USED", "1=1", "VALUE", ColumnType.STRING));

		LOGGER.info("[MINIMAL] Minimal values init done");
	}

}
