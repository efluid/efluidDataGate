package fr.uem.efluid.config;

import static fr.uem.efluid.model.entities.IndexAction.ADD;
import static fr.uem.efluid.model.entities.IndexAction.REMOVE;
import static fr.uem.efluid.model.entities.IndexAction.UPDATE;
import static fr.uem.efluid.utils.DataGenerationUtils.*;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.CommitRepository;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.FunctionalDomainRepository;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.model.repositories.UserRepository;

/**
 * <p>
 * Create some default values for demo mode
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Configuration
@Profile("demo")
@Transactional
public class DemoDataLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(DemoDataLoader.class);

	@Autowired
	private FunctionalDomainRepository domains;

	@Autowired
	private DictionaryRepository dictionary;

	@Autowired
	private UserRepository users;

	@Autowired
	private IndexRepository index;

	@Autowired
	private CommitRepository commits;

	@PostConstruct
	public void initValues() {

		LOGGER.info("[DEMO] Init some demo values for testing");
		User dupont = this.users.save(user("dupont"));
		User testeur = this.users.save(user("testeur"));

		FunctionalDomain dom1 = this.domains.save(domain("Gestion du matériel"));
		FunctionalDomain dom2 = this.domains.save(domain("Matrice des prix"));
		FunctionalDomain dom3 = this.domains.save(domain("Processus utilisateurs"));
		this.domains.save(domain("Editions"));

		DictionaryEntry cmat = this.dictionary.save(entry("Catégorie de matériel", dom1, "TCATEGORYMATERIEL", "1=1", "id"));
		DictionaryEntry tmat = this.dictionary.save(entry("Type de matériel", dom1, "TTYPEMATERIEL", "1=1", "id"));
		DictionaryEntry tdij = this.dictionary.save(entry("Type de disjoncteur", dom1, "TDISJONCTEURTYPE", "1=1", "id"));
		this.dictionary.save(entry("Modèle de compteur", dom1, "TMODELE", "mod='test'", "id"));
		this.dictionary.save(entry("Programmation", dom1, "TPROGRAMMATION", "1=1", "id"));
		this.dictionary.save(entry("Modèle type", dom1, "TMODTYPE", "1=1", "id"));
		this.dictionary.save(entry("Couleur", dom1, "TCOULEUR", "vol='12'", "id"));
		this.dictionary.save(entry("Prix énergie", dom2, "TABLE1", "1=1", "id"));
		this.dictionary.save(entry("Autre exemple 1", dom2, "TABLE2", "1=1", "id"));
		this.dictionary.save(entry("Autre exemple 2", dom2, "TABLE3", "1=1", "id"));
		this.dictionary.save(entry("Autre exemple 3", dom3, "TABLE4", "1=1", "id"));

		Commit com1 = this.commits.save(commit("Ajout du paramètrage de Catégorie de matériel", dupont, 5));
		Commit com2 = this.commits.save(commit("Ajout des Types de matériel", testeur, 3));
		Commit com3 = this.commits.save(commit("Correction sur les types de matériel", testeur, 1));

		this.index.save(update("2349", ADD, "Name=something, Detail=something, value=12345", cmat, com1));
		this.index.save(update("2388", ADD, "Name=something, Detail=something, value=12345", cmat, com1));
		this.index.save(update("34", REMOVE, "Name=something, Detail=something, value=12345", cmat, com1));
		this.index.save(update("2355", ADD, "Name=something, Detail=other, value=12345", cmat, com1));
		this.index.save(update("19", REMOVE, "Name=something, Detail=something, value=12345", cmat, com1));
		this.index.save(update("234", ADD, "Name=something, Detail=something, value=12345", cmat, com1));
		this.index.save(update("234", REMOVE, "Name=something, Detail=something, value=12345", cmat, com1));
		this.index.save(update("455", ADD, "Name=something, Detail=something, value=12345", cmat, com1));
		this.index.save(update("234", UPDATE, "Name=other, Detail=something, value=12345", tmat, com2));
		this.index.save(update("234", ADD, "Name=something, Detail=something, value=12345", tmat, com2));
		this.index.save(update("234", REMOVE, "Name=something, Detail=something, value=12345", tmat, com2));
		this.index.save(update("123", ADD, "Name=something, Detail=other, value=33", tmat, com2));
		this.index.save(update("234", ADD, "Name=something, Detail=something, value=12345", tmat, com2));
		this.index.save(update("234", REMOVE, "Name=something, Detail=something, value=12345", tdij, com3));
		this.index.save(update("11", ADD, "Name=something, Detail=something, value=222", tdij, com2));
		this.index.save(update("234", UPDATE, "Name=something, Detail=something, value=12345", tdij, com2));

		LOGGER.info("[DEMO] Demo values init done");
	}

}
