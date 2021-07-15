package fr.uem.efluid.services;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.ContentLine;
import fr.uem.efluid.model.DiffPayloads;
import fr.uem.efluid.model.entities.CommitState;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.repositories.KnewContentRepository;
import fr.uem.efluid.services.types.PilotedCommitPreparation;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.stubs.TestUtils;
import fr.uem.efluid.tools.diff.ManagedValueConverter;
import fr.uem.efluid.utils.DataGenerationUtils;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public class PrepareDiffServiceUnitTest {

    // No spring here neither
    final ManagedValueConverter converter = new ManagedValueConverter();

    final KnewContentRepositoryMock knewContentHolder = new KnewContentRepositoryMock();

    // No spring load here, for simple diff needs
    private PrepareIndexService service = new PrepareIndexService() {

        /**
         * @return
         * @see fr.uem.efluid.services.PrepareIndexService#getConverter()
         */
        @Override
        protected ManagedValueConverter getConverter() {
            return PrepareDiffServiceUnitTest.this.converter;
        }

        @Override
        protected KnewContentRepository getKnewContents() {
            return PrepareDiffServiceUnitTest.this.knewContentHolder;
        }
    };

    @Test
    public void testGenerateDiffIndexDiffNotChanged() {
        Collection<PreparedIndexEntry> index = getDiff("diff1");
        assertEquals(0, index.size());
    }

    @Test
    public void testGenerateDiffIndexAddedTwo() {
        Collection<PreparedIndexEntry> index = getDiff("diff2");
        assertEquals(2, index.size());
        assertTrue(index.stream().allMatch(i -> i.getAction() == IndexAction.ADD));
        assertTrue(index.stream()
                .allMatch(i -> Value.mapped(this.converter.expandInternalValue(i.getPayload())).get("VALUE").getValueAsString()
                        .equals("Different")));
    }

    @Test
    public void testGenerateDiffIndexAddedNoPrevious() {
        Collection<PreparedIndexEntry> index = getDiff("diff2");

        assertThat(index).allMatch(i -> i.getAction() == IndexAction.ADD && i.getPrevious() == null);
    }

    @Test
    public void testGenerateDiffIndexRemovedTwo() {
        Collection<PreparedIndexEntry> index = getDiff("diff3");
        assertEquals(2, index.size());
        assertTrue(index.stream().allMatch(i -> i.getAction() == IndexAction.REMOVE));
        assertTrue(index.stream().allMatch(i -> i.getPayload() == null));
    }

    @Test
    public void testGenerateDiffIndexRemovedDetectPrevious() {
        Collection<PreparedIndexEntry> index = getDiff("diff3");

        Optional<PreparedIndexEntry> o12i = index.stream().filter(i -> i.getKeyValue().equals("12")).findFirst();
        Optional<PreparedIndexEntry> o13i = index.stream().filter(i -> i.getKeyValue().equals("13")).findFirst();

        assertThat(o12i).isPresent();
        assertThat(o13i).isPresent();

        assertThat(o12i.get().getPrevious()).isEqualTo("VALUE=S/RGlmZmVyZW50,PRESET=S/T3RoZXI=,SOMETHING=S/MTIzMzM0");
        assertThat(o12i.get().getPayload()).isNull();

        assertThat(o13i.get().getPrevious()).isEqualTo("VALUE=S/RGlmZmVyZW50,PRESET=S/T3RoZXI=,SOMETHING=S/MTI5ODk0NA==");
        assertThat(o13i.get().getPayload()).isNull();
    }

    @Test
    public void testGenerateDiffIndexModifiedTwo() {
        Collection<PreparedIndexEntry> index = getDiff("diff4");
        assertEquals(2, index.size());
        assertTrue(index.stream().allMatch(i -> i.getAction() == IndexAction.UPDATE));
        assertTrue(index.stream().anyMatch(i -> i.getKeyValue().equals("5")));
        assertTrue(index.stream().anyMatch(i -> i.getKeyValue().equals("8")));
        assertTrue(index.stream()
                .allMatch(i -> Value.mapped(this.converter.expandInternalValue(i.getPayload())).get("VALUE").getValueAsString()
                        .equals("Modified")));
    }

    @Test
    public void testGenerateDiffIndexModifiedDetectPrevious() {
        Collection<PreparedIndexEntry> index = getDiff("diff4");

        Optional<PreparedIndexEntry> o5i = index.stream().filter(i -> i.getKeyValue().equals("5")).findFirst();
        Optional<PreparedIndexEntry> o8i = index.stream().filter(i -> i.getKeyValue().equals("8")).findFirst();

        assertThat(o5i).isPresent();
        assertThat(o8i).isPresent();

        assertThat(o5i.get().getPrevious()).isEqualTo("VALUE=S/U29tZXRoaW5n,PRESET=S/T3RoZXI=,SOMETHING=S/MTI2MzQ=");
        assertThat(o5i.get().getPayload()).isEqualTo("VALUE=S/TW9kaWZpZWQ=,PRESET=S/T3RoZXI=,SOMETHING=S/MTI2MzQ=");

        assertThat(o8i.get().getPrevious()).isEqualTo("VALUE=S/U29tZXRoaW5n,PRESET=S/T3RoZXI=,SOMETHING=S/MTc3MjM0");
        assertThat(o8i.get().getPayload()).isEqualTo("VALUE=S/TW9kaWZpZWQ=,PRESET=S/T3RoZXI=,SOMETHING=S/MTc3MjM0");
    }

    @Test
    public void testGenerateDiffIndexAddedTwoRemovedTwoModifiedTwo() {
        Collection<PreparedIndexEntry> index = getDiff("diff5");
        assertEquals(6, index.size());
        assertTrue(index.stream().filter(i -> i.getAction() == IndexAction.UPDATE).anyMatch(i -> i.getKeyValue().equals("5")));
        assertTrue(index.stream().filter(i -> i.getAction() == IndexAction.UPDATE).anyMatch(i -> i.getKeyValue().equals("8")));
        assertTrue(index.stream().filter(i -> i.getAction() == IndexAction.REMOVE).anyMatch(i -> i.getKeyValue().equals("2")));
        assertTrue(index.stream().filter(i -> i.getAction() == IndexAction.REMOVE).anyMatch(i -> i.getKeyValue().equals("4")));
        assertTrue(index.stream().filter(i -> i.getAction() == IndexAction.ADD).anyMatch(i -> i.getKeyValue().equals("12")));
        assertTrue(index.stream().filter(i -> i.getAction() == IndexAction.ADD).anyMatch(i -> i.getKeyValue().equals("13")));
    }

    @Test
    public void testGenerateDiffIndexAddedFourRemovedThreeModifiedFour() {
        Collection<PreparedIndexEntry> index = getDiff("diff6");
        assertEquals(11, index.size());
        assertEquals(4, index.stream().filter(i -> i.getAction() == IndexAction.UPDATE).count());
        assertEquals(3, index.stream().filter(i -> i.getAction() == IndexAction.REMOVE).count());
        assertEquals(4, index.stream().filter(i -> i.getAction() == IndexAction.ADD).count());
    }

    /**
     * @param diffName
     * @return
     */
    private Collection<PreparedIndexEntry> getDiff(String diffName) {
        Map<String, String> diff1Actual = TestUtils.readDataset(diffName + "/actual.csv", this.converter);
        this.knewContentHolder.setCurrentKnew(TestUtils.readDataset(diffName + "/knew.csv", this.converter));

        Project proj = DataGenerationUtils.project("mock");
        DictionaryEntry entry = DataGenerationUtils.entry("mock", DataGenerationUtils.domain("mock", proj), "s*", "table", "1=1", "key", ColumnType.STRING);

        // Static internal
        return this.service.generateDiffIndexFromContent(
                diff1Actual.entrySet().stream()
                        .map(e -> new ContentLine() {

                            @Override
                            public String getKeyValue() {
                                return e.getKey();
                            }

                            @Override
                            public String getPayload() {
                                return e.getValue();
                            }
                        }),
                PreparedIndexEntry::new,
                this.knewContentHolder.knewContentKeys(entry),
                l -> {
                },
                entry,
                new PilotedCommitPreparation<>(CommitState.LOCAL),
                System.currentTimeMillis());

    }

    private static class KnewContentRepositoryMock implements KnewContentRepository {

        Map<String, String> currentKnew;

        public void setCurrentKnew(Map<String, String> currentKnew) {
            this.currentKnew = currentKnew;
        }

        @Override
        public Set<String> knewContentKeys(DictionaryEntry dictionaryEntry) {
            return new HashSet<>(this.currentKnew.keySet());
        }

        @Override
        public Map<String, String> knewContentForKeysBefore(DictionaryEntry dictionaryEntry, Collection<String> keys, long timestamp) {
            return this.currentKnew.entrySet().stream()
                    .filter(e -> keys.contains(e.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        @Override
        public Set<String> knewContentKeysBefore(DictionaryEntry dictionaryEntry, long timestamp) {
            return this.currentKnew.keySet();
        }

        @Override
        public Map<String, DiffPayloads> knewContentPayloadsForKeysBefore(DictionaryEntry dictionaryEntry, Collection<String> keys, long timestamp) {
            return null; // Not used
        }
    }
}
