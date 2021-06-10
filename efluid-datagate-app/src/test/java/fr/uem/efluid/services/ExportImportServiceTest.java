package fr.uem.efluid.services;

import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.IndexEntry;
import fr.uem.efluid.services.types.CommitPackage;
import fr.uem.efluid.services.types.IndexEntryPackage;
import fr.uem.efluid.services.types.SharedPackage;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic testing on package read on some common cases
 */
public class ExportImportServiceTest {

    // New commit model
    private static final String NEW_COMMIT_PACK_CONTENT =
            "[pack|fr.uem.efluid.services.types.CommitPackage|commits-part|2021-03-19T08:32:50.993506400|3]\n" +
                    "[item]{\"com\":\":tada: Test commit init\",\"uid\":\"55a552b2-40ec-4d99-a1b7-e142e5808918\",\"ref\":\"0\",\"ver\":\"02ea2a55-e607-4b5a-8734-0c9542bb4d63\",\"cre\":\"2021-03-19 08:32:49\",\"ema\":\"any@test.fr\",\"pro\":\"e4c76236-d2c7-4337-96ae-35f2941ad850\"}[/item]\n" +
                    "[item]{\"com\":\":construction: Update 1\",\"uid\":\"3244332f-85a9-4cdb-8aa4-990b1a03bf82\",\"ref\":\"0\",\"ver\":\"02ea2a55-e607-4b5a-8734-0c9542bb4d63\",\"cre\":\"2021-03-19 08:32:50\",\"ema\":\"any@test.fr\",\"pro\":\"e4c76236-d2c7-4337-96ae-35f2941ad850\"}[/item]\n" +
                    "[item]{\"com\":\":construction: Update 2\",\"uid\":\"a19d5057-7431-48df-bf2c-822a4ed3279d\",\"ref\":\"0\",\"ver\":\"02ea2a55-e607-4b5a-8734-0c9542bb4d63\",\"cre\":\"2021-03-19 08:32:50\",\"ema\":\"any@test.fr\",\"pro\":\"e4c76236-d2c7-4337-96ae-35f2941ad850\"}[/item]\n" +
                    "[/pack]";

    private static final String LEGACY_COMMIT_PACK_CONTENT =
            "[pack|fr.uem.efluid.services.types.CommitPackage|commits-all|2021-02-26T18:13:52.915865400|2]\n" +
                    "[item]{\"com\":\":construction: Test many tables\",\"uid\":\"07db57d3-e5c7-4c3c-8f36-57806b2bc082\",\"ver\":\"62222bd2-aa40-44b9-8cd7-a66fcd9987c2\",\"cre\":\"2021-02-19 12:30:40\",\"ema\":\"rrrr\",\"pro\":\"66a0feec-16bd-48aa-9d30-c245b9f61ecf\",\"idx\":\"" +
                    "{\\\"act\\\":\\\"REMOVE\\\",\\\"pay\\\":\\\"VALUE=S/TW9kaWZpZWQ=,PRESET=S/T3RoZXI=,SOMETHING=S/MTI2MzQ=\\\",\\\"tim\\\":\\\"0\\\",\\\"key\\\":\\\"55\\\",\\\"dic\\\":\\\"781a5ec0-5673-4201-b4b4-d356abafb5c8\\\"}\\n" +
                    "{\\\"act\\\":\\\"REMOVE\\\",\\\"pay\\\":\\\"VALUE=S/U29tZXRoaW5n,PRESET=S/T3RoZXI=,SOMETHING=S/MTIxMTM0\\\",\\\"tim\\\":\\\"0\\\",\\\"key\\\":\\\"56\\\",\\\"dic\\\":\\\"781a5ec0-5673-4201-b4b4-d356abafb5c8\\\"}\"}" +
                    "[/item]\n[/pack]";

    private static final String INDEX_PACK_CONTENT =
            "[pack|fr.uem.efluid.services.types.IndexEntryPackage|indexes|2021-03-19T08:32:50.993506400|1]\n" +
                    "[item]{\"com\":\"55a552b2-40ec-4d99-a1b7-e142e5808918\",\"act\":\"ADD\",\"pay\":\"OTHER=S/T3RoZXIgQQ==\",\"tim\":\"1616139169015\",\"key\":\"A\",\"dic\":\"98a97de0-1942-4934-a02f-319df9e21fa8\"}[/item]\n" +
                    "[item]{\"com\":\"55a552b2-40ec-4d99-a1b7-e142e5808918\",\"act\":\"ADD\",\"pay\":\"OTHER=S/T3RoZXIgQw==\",\"tim\":\"1616139169015\",\"key\":\"C\",\"dic\":\"98a97de0-1942-4934-a02f-319df9e21fa8\"}[/item]\n" +
                    "[item]{\"com\":\"55a552b2-40ec-4d99-a1b7-e142e5808918\",\"act\":\"ADD\",\"pay\":\"OTHER=S/T3RoZXIgQg==\",\"tim\":\"1616139169015\",\"key\":\"B\",\"dic\":\"98a97de0-1942-4934-a02f-319df9e21fa8\"}[/item]\n" +
                    "[item]{\"com\":\"55a552b2-40ec-4d99-a1b7-e142e5808918\",\"act\":\"ADD\",\"pay\":\"PRESET=S/UHJlc2V0IDI=,SOMETHING=S/QkJC\",\"tim\":\"1616139169019\",\"key\":\"BBB\",\"dic\":\"a5ccd41d-367b-4c32-8dda-21086c44389d\"}[/item]\n" +
                    "[item]{\"com\":\"55a552b2-40ec-4d99-a1b7-e142e5808918\",\"act\":\"ADD\",\"pay\":\"PRESET=S/UHJlc2V0IDE=,SOMETHING=S/QUFB\",\"tim\":\"1616139169019\",\"key\":\"AAA\",\"dic\":\"a5ccd41d-367b-4c32-8dda-21086c44389d\"}[/item]\n" +
                    "[item]{\"com\":\"55a552b2-40ec-4d99-a1b7-e142e5808918\",\"act\":\"ADD\",\"pay\":\"VALUE=S/VHdv,OTHER=S/T3RoZXIgS0tL\",\"tim\":\"1616139169019\",\"key\":\"KKK\",\"dic\":\"96337cf8-46e1-4424-9fad-444b5ac37c0d\"}[/item]\n" +
                    "[item]{\"com\":\"55a552b2-40ec-4d99-a1b7-e142e5808918\",\"act\":\"ADD\",\"pay\":\"VALUE=S/T25l,OTHER=S/T3RoZXIgSkpK\",\"tim\":\"1616139169019\",\"key\":\"JJJ\",\"dic\":\"96337cf8-46e1-4424-9fad-444b5ac37c0d\"}[/item]\n" +
                    "[item]{\"com\":\"55a552b2-40ec-4d99-a1b7-e142e5808918\",\"act\":\"ADD\",\"pay\":\"PRESET=S/UHJlc2V0IDM=,SOMETHING=S/Q0ND\",\"tim\":\"1616139169020\",\"key\":\"CCC\",\"dic\":\"a5ccd41d-367b-4c32-8dda-21086c44389d\"}[/item]\n" +
                    "[item]{\"com\":\"55a552b2-40ec-4d99-a1b7-e142e5808918\",\"act\":\"ADD\",\"pay\":\"PRESET=S/UHJlc2V0IDU=,SOMETHING=S/RUVF\",\"tim\":\"1616139169020\",\"key\":\"EEE\",\"dic\":\"a5ccd41d-367b-4c32-8dda-21086c44389d\"}[/item]\n" +
                    "[item]{\"com\":\"55a552b2-40ec-4d99-a1b7-e142e5808918\",\"act\":\"ADD\",\"pay\":\"PRESET=S/UHJlc2V0IDQ=,SOMETHING=S/RERE\",\"tim\":\"1616139169020\",\"key\":\"DDD\",\"dic\":\"a5ccd41d-367b-4c32-8dda-21086c44389d\"}[/item]\n" +
                    "[item]{\"com\":\"3244332f-85a9-4cdb-8aa4-990b1a03bf82\",\"act\":\"ADD\",\"pay\":\"PRESET=S/UHJlc2V0IDI=,SOMETHING=S/QkJC\",\"tim\":\"1616139170096\",\"key\":\"HHH\",\"dic\":\"a5ccd41d-367b-4c32-8dda-21086c44389d\"}[/item]\n" +
                    "[item]{\"com\":\"3244332f-85a9-4cdb-8aa4-990b1a03bf82\",\"act\":\"ADD\",\"pay\":\"PRESET=S/UHJlc2V0IDc2,SOMETHING=S/WlpaWA==\",\"tim\":\"1616139170096\",\"key\":\"ZZZ\",\"dic\":\"a5ccd41d-367b-4c32-8dda-21086c44389d\"}[/item]\n" +
                    "[item]{\"com\":\"3244332f-85a9-4cdb-8aa4-990b1a03bf82\",\"pre\":\"PRESET=S/UHJlc2V0IDQ=,SOMETHING=S/RERE\",\"act\":\"UPDATE\",\"pay\":\"PRESET=S/UHJlc2V0IDQgdXBkYXRlZA==,SOMETHING=S/RERE\",\"tim\":\"1616139170100\",\"key\":\"DDD\",\"dic\":\"a5ccd41d-367b-4c32-8dda-21086c44389d\"}[/item]\n" +
                    "[item]{\"com\":\"3244332f-85a9-4cdb-8aa4-990b1a03bf82\",\"pre\":\"PRESET=S/UHJlc2V0IDM=,SOMETHING=S/Q0ND\",\"act\":\"REMOVE\",\"tim\":\"1616139170103\",\"key\":\"CCC\",\"dic\":\"a5ccd41d-367b-4c32-8dda-21086c44389d\"}[/item]\n" +
                    "[item]{\"com\":\"a19d5057-7431-48df-bf2c-822a4ed3279d\",\"act\":\"ADD\",\"pay\":\"VALUE=S/VHdv,OTHER=S/T3RoZXIgS0tL\",\"tim\":\"1616139170646\",\"key\":\"KKK2\",\"dic\":\"96337cf8-46e1-4424-9fad-444b5ac37c0d\"}[/item]\n" +
                    "[item]{\"com\":\"a19d5057-7431-48df-bf2c-822a4ed3279d\",\"act\":\"ADD\",\"pay\":\"VALUE=S/T25l,OTHER=S/T3RoZXIgSkpK\",\"tim\":\"1616139170646\",\"key\":\"JJJ2\",\"dic\":\"96337cf8-46e1-4424-9fad-444b5ac37c0d\"}[/item]\n" +
                    "[/pack]\n";

    @Test
    public void testReadNewCommitPackage() throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(NEW_COMMIT_PACK_CONTENT.getBytes(StandardCharsets.UTF_8))) {
            SharedPackage<?> pack = ExportImportService.readPackage(null, inputStream);
            assertThat(pack).isInstanceOf(CommitPackage.class);
            List<Commit> commits = ((CommitPackage) pack).content().collect(Collectors.toList());
            assertThat(commits).hasSize(3);
            assertThat(commits.get(1)).matches(c -> c.getComment().equals(":construction: Update 1"));
        }
    }

    @Test
    public void testReadIndexPackage() throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(INDEX_PACK_CONTENT.getBytes(StandardCharsets.UTF_8))) {
            SharedPackage<?> pack = ExportImportService.readPackage(null, inputStream);
            assertThat(pack).isInstanceOf(IndexEntryPackage.class);
            List<IndexEntry> index = ((IndexEntryPackage) pack).content().collect(Collectors.toList());
            assertThat(index).hasSize(16);
            assertThat(index.get(1)).matches(c -> c.getKeyValue().equals("C"));
        }
    }

    @Test
    public void testReadLegacyCommitPackage() throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(LEGACY_COMMIT_PACK_CONTENT.getBytes(StandardCharsets.UTF_8))) {
            SharedPackage<?> pack = ExportImportService.readPackage(null, inputStream);
            assertThat(pack).isInstanceOf(CommitPackage.class);
            List<Commit> commits = ((CommitPackage) pack).content().collect(Collectors.toList());
            assertThat(commits).hasSize(1);
            assertThat(((CommitPackage) pack).isCompatibilityMode()).isTrue();
            assertThat(commits.get(0)).matches(c -> c.getComment().equals(":construction: Test many tables"));
            assertThat(commits.get(0).getIndex()).hasSize(2);
            assertThat(commits.get(0).getIndex().iterator().next().getKeyValue()).isEqualTo("55");
        }
    }
}
