package fr.uem.efluid.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uem.efluid.tools.MergeResolutionProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@Configuration
@EnableConfigurationProperties(MergeResolutionConfig.MergeProperties.class)
public class MergeResolutionConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergeResolutionConfig.class);

    @Autowired
    private MergeProperties properties;

    @Bean
    public MergeResolutionProcessor mergeResolutionProcessor() throws IOException {

        URL json = ResourceUtils.getURL(this.properties.getRuleFile());

        ObjectMapper mapper = new ObjectMapper();

        List<MergeResolutionProcessor.ResolutionCase> cases = mapper.readValue(json,
                new TypeReference<List<MergeResolutionProcessor.ResolutionCase>>() {
                });

        LOGGER.info("[MERGE-CFG] Loaded {} resolution cases from json file {}", cases.size(), this.properties.getRuleFile());

        return new MergeResolutionProcessor(cases);
    }

    @ConfigurationProperties(prefix = "datagate-efluid.merge")
    public static class MergeProperties {

        private boolean useNewProcess;
        private String ruleFile;

        public MergeProperties() {
            super();
        }

        public boolean isUseNewProcess() {
            return useNewProcess;
        }

        public void setUseNewProcess(boolean useNewProcess) {
            this.useNewProcess = useNewProcess;
        }

        public String getRuleFile() {
            return ruleFile;
        }

        public void setRuleFile(String ruleFile) {
            this.ruleFile = ruleFile;
        }
    }

}
