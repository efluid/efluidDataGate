package fr.uem.efluid.stubs;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * For test situations where the transaction cannot be shared between source init and extraction
 */
@Transactional(isolation = Isolation.READ_UNCOMMITTED, propagation = Propagation.REQUIRES_NEW)
@Component
public class TesterWithIndependentTransaction {

    @Autowired
    private TestDataLoader loader;

    @Autowired
    private DictionaryRepository dictionary;

    private UUID dictionaryEntryUuid;
    private UUID projectUuid;

    public void setupDatabase(String diff) {
        DataLoadResult res = this.loader.setupDatabaseForDiff(diff);
        this.dictionaryEntryUuid = res.getDicUuid();
        this.projectUuid = res.getProjectUuid();
    }

    public DictionaryEntry dict() {
        DictionaryEntry dic = this.dictionary.getOne(this.dictionaryEntryUuid);
        dic.getSelectClause();
        return dic;
    }

    public Project proj() {
        return new Project(this.projectUuid);
    }

    public void assertDbContentIs(Map<String, String> raw, String src) {
        this.loader.assertDatasetEqualsRegardingConverter(raw, src);
    }
}
