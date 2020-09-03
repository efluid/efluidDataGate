package fr.uem.efluid;

import fr.uem.efluid.generation.DictionaryContent;
import fr.uem.efluid.generation.DictionaryExporter;
import fr.uem.efluid.generation.DictionaryGenerator;
import fr.uem.efluid.generation.DictionaryGeneratorConfig;
import fr.uem.efluid.model.ParameterDomainDefinition;
import fr.uem.efluid.model.ParameterLinkDefinition;
import fr.uem.efluid.model.ParameterProjectDefinition;
import fr.uem.efluid.model.ParameterTableDefinition;
import org.junit.Assert;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tool for complete testing of a generated dictionary, with good test readability
 *
 * @author elecomte
 * @version 1
 * @since v1.2.0
 */
public class GeneratorTester {

    private final DictionaryGeneratorConfig config;
    private DictionaryContent content;
    private Exception generationException;
    private Exception exportException;

    private final List<CharSequence> debugs = new ArrayList<>();
    private final List<CharSequence> infos = new ArrayList<>();
    private final List<CharSequence> errors = new ArrayList<>();
    private final List<Throwable> errorsExcs = new ArrayList<>();

    private boolean uploadToServer = false;
    private String uploadEntryPointUri = "http://127.0.0.1:8080/rest/v1";
    private String uploadSecurityToken = "afc9921811684c7f88062cd47ddf0ff5";
    private String projectVersion = "1.2.3";

    private Map<Class<?>, List<FoundTable>> foundTables = new HashMap<>();
    private Map<Class<?>, List<FoundKey>> foundKeys = new HashMap<>();
    private Map<Class<?>, List<String>> foundValues = new HashMap<>();

    private Pattern keySearch = Pattern.compile("^Found key (.*) of type (.*) for type (.*)$");
    private Pattern tableSearch = Pattern.compile("^Found new mapped parameter .* in type (.*) with table (.*) and generated UUID (.*)$");
    private Pattern valueSearch = Pattern.compile("^Found selected value (.*) for type (.*)$");

    private GeneratorTester(String pack, Class<?>... searchTypes) {
        this.config = config(pack, Stream.of(searchTypes).collect(Collectors.toSet()));
    }

    public GeneratorTester withSpecifiedVersion(String version) {
        this.projectVersion = version;
        return this;
    }

    public GeneratorTester generate() {
        DictionaryGenerator generator = new DictionaryGenerator(this.config);
        DictionaryContent ct;

        try {
            ct = generator.extractDictionary(getClass().getClassLoader());
        } catch (Exception e) {
            this.generationException = e;
            ct = null;
        }

        this.content = ct;

        export();

        return this;
    }


    public void exportWithUpload(int port, String token) {
        this.uploadToServer = true;
        this.uploadEntryPointUri = "http://127.0.0.1:" + port + "/rest/v1";
        this.uploadSecurityToken = token;
        export();
    }

    public UUID getDefaultProjectUuid() {
        return this.content.getAllProjects().iterator().next().getUuid();
    }

    public static GeneratorTester onPackage(String pack) {
        return new GeneratorTester(pack);
    }

    /**
     * More convenient accessor for readability to avoid managing string package name. Process only given classes
     *
     * @param classes any type to process. Only these classes will be used for generator
     * @return parsed tester
     */
    public static GeneratorTester onClasses(Class<?>... classes) {
        return new GeneratorTester(null, classes);
    }


    public GeneratorTester assertThatContentWereIdentified() {
        Assert.assertNotNull(this.content);
        return this;
    }

    public GeneratorTester assertFoundDomainsAre(String... names) {
        Assert.assertEquals(names.length, this.content.getAllDomains().size());

        Set<String> foundNames = this.content.getAllDomains().stream().map(ParameterDomainDefinition::getName).collect(Collectors.toSet());

        Assert.assertTrue(Stream.of(names).allMatch(foundNames::contains));

        return this;
    }

    public GeneratorTester assertFoundTablesAre(String... names) {
        Assert.assertEquals(names.length, this.content.getAllTables().size());
        Set<String> foundNames = this.content.getAllTables().stream().map(ParameterTableDefinition::getTableName).collect(Collectors.toSet());

        Assert.assertTrue(Stream.of(names).allMatch(foundNames::contains));
        return this;
    }

    public GeneratorTester assertFoundLinkCountIs(int count) {
        Assert.assertEquals(count, this.content.getAllLinks().size());
        return this;
    }

    public GeneratorTester assertFoundMappingCountIs(int count) {
        Assert.assertEquals(count, this.content.getAllMappings().size());
        return this;
    }

    public GeneratedTableAssert assertThatTable(String table) {
        return new GeneratedTableAssert(
                table,
                this.content.getAllTables().stream().filter(t -> t.getTableName().equals(table)).findFirst());
    }

    public GeneratorTester assertNoErrorWasMet() {
        assertNoGenerationErrorWasMet();
        assertNoExportErrorWasMet();
        return this;
    }

    public GeneratorTester assertNoGenerationErrorWasMet() {
        if (this.generationException != null) {
            throw new AssertionError("Generation exceptions was met", generationException);
        }
        return this;
    }

    public GeneratorTester assertNoExportErrorWasMet() {
        if (this.exportException != null) {
            throw new AssertionError("Export exceptions was met", exportException);
        }
        return this;
    }

    public UUID getUuidForTable(String table) {
        return this.content.getAllTables().stream()
                .filter(t -> t.getTableName().equals(table)).findFirst()
                .orElseThrow(() -> new AssertionError("Table \"" + table + "\" was not identified, cannot get uuid")).getUuid();
    }

    public UUID getUuidForDomain(String domain) {
        return this.content.getAllDomains().stream()
                .filter(t -> t.getName().equals(domain)).findFirst()
                .orElseThrow(() -> new AssertionError("Domain \"" + domain + "\" was not identified, cannot get uuid")).getUuid();
    }

    public void export() {
        DictionaryExporter exporter = new DictionaryExporter(this.config);

        if (this.content != null) {
            try {
                exporter.export(this.content);
            } catch (Exception e) {
                this.exportException = e;
            }
        }
    }

    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalGetWithoutIsPresent"})
    public class GeneratedTableAssert {
        private final String tableName;
        private final Optional<ParameterTableDefinition> tableOpt;

        private GeneratedTableAssert(String tableName, Optional<ParameterTableDefinition> tableOpt) {
            this.tableName = tableName;
            this.tableOpt = tableOpt;
        }

        public void doesntExist() {
            Assert.assertTrue("Table " + tableName + " shouldn't exist but does", tableOpt.isEmpty());
        }

        public GeneratedTableAssert exists() {
            Assert.assertTrue("Table " + tableName + " must exist but doesn't", tableOpt.isPresent());
            return this;
        }

        public GeneratedTableAssert wasFoundOn(Class<?> type) {

            GeneratorTester.this.foundTables.get(type).stream()
                    .filter(t -> t.getName().equals(this.tableName))
                    .findFirst().orElseThrow(
                    () -> new AssertionError("Table \"" + this.tableName + "\" was not found onto type " + type.getName())
            );

            return this;
        }

        public GeneratedTableAssert hasDictionaryEntryName(String name) {
            Assert.assertEquals("Specified name is not valid. Expected \"" + name + "\" but get \"" + this.tableOpt.get().getParameterName() + "\" for table \"" + tableName + "\"", name, this.tableOpt.get().getParameterName());
            return this;
        }

        public GeneratedTableAssert hasKey(String keyname, ColumnType type) {
            List<String> names = this.tableOpt.get().getAllKeyNames();
            List<ColumnType> types = this.tableOpt.get().getAllKeyTypes();
            Assert.assertTrue("Specified key is not valid. Expected to found \"" + keyname + "\" but get \"" + Arrays.toString(names.toArray()) + "\" for table \"" + tableName + "\"", names.contains(keyname));
            ColumnType found = types.get(names.indexOf(keyname));
            Assert.assertEquals("Specified key type is not valid. Expected to found \"" + type + "\" but get \"" + Arrays.toString(types.toArray()) + "\" for table \"" + tableName + "\"", type, found);
            return this;
        }

        public GeneratedTableAssert doesntHaveKey(String keyname) {
            List<String> names = this.tableOpt.get().getAllKeyNames();
            Assert.assertFalse("Specified key is not valid. Expected to not found \"" + keyname + "\" but get \"" + Arrays.toString(names.toArray()) + "\" for table \"" + tableName + "\"", names.contains(keyname));
            return this;
        }

        public GeneratedTableAssert hasColumns(String... selectCols) {
            String[] splitSelect = this.tableOpt.get().getSelectClause().split(", ");
            Assert.assertEquals("Specified select is not valid. Do not find the correct number of columns in current select \"" + this.tableOpt.get().getSelectClause()
                    + "\" for table \"" + tableName + "\"", selectCols.length, splitSelect.length);
            List<String> cols = Stream.of(splitSelect).map(v -> v.replaceAll("cur\\.\"", "").replaceAll("\"", "")).collect(Collectors.toList());
            Stream.of(selectCols).forEach(v -> Assert.assertTrue("Specified select is not valid. Cannot found specified col \"" + v + "\" for table \"" + tableName + "\"", cols.contains(v)));
            return this;
        }

        public GeneratedTableAssert doesntHaveColumns(String... selectCols) {
            String[] splitSelect = this.tableOpt.get().getSelectClause().split(", ");
            List<String> cols = Stream.of(splitSelect).map(v -> v.replaceAll("cur\\.\"", "").replaceAll("\"", "")).collect(Collectors.toList());
            Stream.of(selectCols).forEach(v -> Assert.assertFalse("Specified select is not valid. Found unexpected col \"" + v + "\" for table \"" + tableName + "\"", cols.contains(v)));
            return this;
        }

        public GeneratedTableAssert isInProject(String projectname) {
            Optional<ParameterDomainDefinition> domain = GeneratorTester.this.content.getAllDomains().stream()
                    .filter(d -> d.getName().equals(this.tableOpt.get().getDomain().getName())).findFirst();

            if (domain.isEmpty()) {
                throw new AssertionError("Cannot check project for table " + this.tableName + " has it doesn't have any domain");
            } else {
                Optional<ParameterProjectDefinition> project = GeneratorTester.this.content.getAllProjects().stream()
                        .filter(p -> p.getUuid().equals(domain.get().getProject().getUuid())).findFirst();
                if (project.isEmpty()) {
                    throw new AssertionError("Cannot check project for table \""
                            + this.tableName + "\" has its domain doesn't have any specified project");
                } else if (!project.get().getName().equals(projectname)) {
                    throw new AssertionError("Project for table \"" + this.tableName
                            + "\" is not \"" + projectname + "\" as expected but \"" + project.get().getName() + "\"");
                }
            }
            return this;
        }

        public GeneratedTableAssert isInDomain(String domainname) {
            Optional<ParameterDomainDefinition> domain = GeneratorTester.this.content.getAllDomains().stream()
                    .filter(d -> d.getName().equals(this.tableOpt.get().getDomain().getName())).findFirst();

            if (domain.isEmpty()) {
                throw new AssertionError("Cannot check domain for table " + this.tableName + " : it doesn't have any domain");
            } else if (!domain.get().getName().equals(domainname)) {
                throw new AssertionError("Domain for table \"" + this.tableName
                        + "\" is not \"" + domainname + "\" as expected but \"" + domain.get().getName() + "\"");
            }
            return this;
        }

        public GeneratorTester and() {
            return GeneratorTester.this;
        }

        public GeneratedTableAssertLink hasLinkForColumn(String colname) {
            Set<ParameterLinkDefinition> links = GeneratorTester.this.content.getAllLinks().stream().filter(l -> l.getDictionaryEntry().getTableName().equals(this.tableName) && l.getColumnFrom().equals(colname)).collect(Collectors.toSet());
            if (links.isEmpty()) {
                throw new AssertionError("Couldn't find any link from column " + colname + " in table " + tableName);
            }
            return new GeneratedTableAssertLink(colname, links);
        }

        public class GeneratedTableAssertLink {
            private final String colname;
            private final Set<ParameterLinkDefinition> links;

            private GeneratedTableAssertLink(String colname, Set<ParameterLinkDefinition> links) {
                super();
                this.links = links;
                this.colname = colname;
            }

            public GeneratedTableAssert with(String tableTo, String colTo) {
                this.links.stream().filter(l -> l.getTableTo().equals(tableTo) && l.getColumnTo().equals(colTo)).findFirst()
                        .orElseThrow(() -> new AssertionError("Couldn't found referenced link from column "
                                + colname + " in table " + GeneratedTableAssert.this.tableName
                                + " to column " + colTo + " in table " + tableTo));
                return GeneratedTableAssert.this;
            }
        }
    }

    private static class FoundKey {
        private final String name;
        private final ColumnType type;

        private FoundKey(String name, ColumnType type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public ColumnType getType() {
            return type;
        }
    }

    private static class FoundTable {
        private final String name;
        private final UUID uuid;

        private FoundTable(String name, UUID uuid) {
            this.name = name;
            this.uuid = uuid;
        }

        public String getName() {
            return name;
        }

        public UUID getUuid() {
            return uuid;
        }
    }

    // [DEBUG] Found new mapped parameter from set in type fr.uem.efluid.sample.advanced.TypeOnMultipleTablesSecond with table T_TABLE_MUTSECOND_THREE and generated UUID 6b2e9eb3-0000-0000-0000-0000fe2b3363

    private DictionaryGeneratorConfig config(final String packageName, final Set<Class<?>> classes) {

        return new DictionaryGeneratorConfig() {

            @Override
            public String getSourcePackage() {
                return packageName;
            }

            @Nullable
            @Override
            public Set<Class<?>> getSourceClasses() {
                return classes;
            }

            @Override
            public LogFacade getLogger() {
                return new LogFacade() {

                    @Override
                    public void debug(CharSequence var1) {

                        try {
                            Matcher mk = keySearch.matcher(var1);

                            if (mk.matches()) {
                                FoundKey key = new FoundKey(mk.group(1), ColumnType.valueOf(mk.group(2)));
                                Class<?> type = Class.forName(mk.group(3));
                                GeneratorTester.this.foundKeys.computeIfAbsent(type, k -> new ArrayList<>()).add(key);
                            } else {
                                Matcher mt = tableSearch.matcher(var1);

                                if (mt.matches()) {
                                    FoundTable table = new FoundTable(mt.group(2), UUID.fromString(mt.group(3)));
                                    Class<?> type = Class.forName(mt.group(1));
                                    GeneratorTester.this.foundTables.computeIfAbsent(type, k -> new ArrayList<>()).add(table);
                                } else {
                                    Matcher mv = valueSearch.matcher(var1);

                                    if (mv.matches()) {
                                        String value = mv.group(1);
                                        Class<?> type = Class.forName(mv.group(2));
                                        GeneratorTester.this.foundValues.computeIfAbsent(type, k -> new ArrayList<>()).add(value);
                                    }
                                }
                            }

                        } catch (Exception e) {
                            System.out.println("[TESTER ERROR] Cannot process debug expression \"" + var1 + "\". Got \"" + e.getMessage() + "\":");
                            e.printStackTrace();
                        }

                        GeneratorTester.this.debugs.add(var1);
                        System.out.println("[DEBUG] " + var1);
                    }

                    @Override
                    public void info(CharSequence var1) {
                        GeneratorTester.this.infos.add(var1);
                        System.out.println("[INFO] " + var1);
                    }

                    @Override
                    public void error(CharSequence var1, Throwable var2) {
                        GeneratorTester.this.errors.add(var1);
                        GeneratorTester.this.errorsExcs.add(var2);
                        System.out.println("[ERROR] " + var1 + ". " + var2.getMessage() + ":");
                        var2.printStackTrace(System.out);
                    }
                };
            }

            @Override
            public String getDestinationFolder() {
                return "./target/";
            }

            @Override
            public boolean isProtectColumn() {
                return true;
            }

            @Override
            public String getDestinationFileDesignation() {
                return DictionaryGeneratorConfig.AUTO_GEN_DEST_FILE_DESG;
            }

            @Override
            public boolean isUploadToServer() {
                return GeneratorTester.this.uploadToServer;
            }

            @Override
            public String getUploadEntryPointUri() {
                return GeneratorTester.this.uploadEntryPointUri;
            }

            @Override
            public String getUploadSecurityToken() {
                return GeneratorTester.this.uploadSecurityToken;
            }

            @Override
            public String getProjectVersion() {
                return GeneratorTester.this.projectVersion;
            }

            @Override
            public boolean isCheckDuplicateTables() {
                return false;
            }
        };
    }
}
