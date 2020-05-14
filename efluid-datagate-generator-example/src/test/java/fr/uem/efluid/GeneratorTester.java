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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    private final DictionaryContent content;
    private Exception generationException;
    private Exception exportException;

    private final List<CharSequence> debugs = new ArrayList<>();
    private final List<CharSequence> infos = new ArrayList<>();
    private final List<CharSequence> errors = new ArrayList<>();
    private final List<Throwable> errorsExcs = new ArrayList<>();

    private GeneratorTester(String pack) {

        DictionaryGeneratorConfig config = config(pack);
        DictionaryGenerator generator = new DictionaryGenerator(config);
        DictionaryExporter exporter = new DictionaryExporter(config);

        DictionaryContent ct;

        try {
            ct = generator.extractDictionary(getClass().getClassLoader());
        } catch (Exception e) {
            this.generationException = e;
            ct = null;
        }

        this.content = ct;

        if (this.content != null) {
            try {
                exporter.export(this.content);
            } catch (Exception e) {
                this.exportException = e;
            }
        }
    }

    public static GeneratorTester onPackage(String pack) {
        return new GeneratorTester(pack);
    }

    public GeneratorTester assertThatIdentifiedTablesAre(String... tableNames) {
        return this;
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

    public GeneratorTester assertFoundTableCountIs(int count) {
        Assert.assertEquals(count, this.content.getAllTables().size());
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

        public GeneratedTableAssert exist() {
            Assert.assertTrue("Table " + tableName + " must exist but doesn't", tableOpt.isPresent());
            return this;
        }

        public GeneratedTableAssert hasDictionaryEntryName(String name) {
            Assert.assertEquals("Specified name is not valid. Expected \"" + name + "\" but get \"" + this.tableOpt.get().getParameterName() + "\" for table \"" + tableName + "\"", name, this.tableOpt.get().getParameterName());
            return this;
        }

        public GeneratedTableAssert hasKey(String keyname, ColumnType type) {
            Assert.assertEquals("Specified key is not valid. Expected \"" + keyname + "\" but get \"" + this.tableOpt.get().getKeyName() + "\" for table \"" + tableName + "\"", keyname, this.tableOpt.get().getKeyName());
            Assert.assertEquals("Specified key type is not valid. Expected \"" + type + "\" but get \"" + this.tableOpt.get().getKeyType() + "\" for table \"" + tableName + "\"", type, this.tableOpt.get().getKeyType());
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

        public GeneratorTester and(){
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

    private DictionaryGeneratorConfig config(final String packageName) {

        return new DictionaryGeneratorConfig() {

            @Override
            public String getSourcePackage() {
                return packageName;
            }

            @Override
            public LogFacade getLogger() {
                return new LogFacade() {

                    @Override
                    public void debug(CharSequence var1) {

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
                return false;
            }

            @Override
            public String getUploadEntryPointUri() {
                return "http://127.0.0.1:8080/rest/v1/dictionary";
            }

            @Override
            public String getUploadSecurityToken() {
                return "afc9921811684c7f88062cd47ddf0ff5";
            }

            @Override
            public String getProjectVersion() {
                return "1.2.3";
            }

            @Override
            public boolean isCheckDuplicateTables() {
                return false;
            }
        };
    }
}
