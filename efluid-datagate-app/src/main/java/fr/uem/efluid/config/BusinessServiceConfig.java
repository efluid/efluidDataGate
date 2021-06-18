package fr.uem.efluid.config;

import fr.uem.efluid.services.PrepareIndexService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BusinessServiceConfig.IndexDisplayConfigProperties.class)
public class BusinessServiceConfig {

    // TODO : report here all init of configuration properties for backlog / dictionary service layer init

    @ConfigurationProperties(prefix = "datagate-efluid.display")
    public static class IndexDisplayConfigProperties implements PrepareIndexService.IndexDisplayConfig {

        private long detailsIndexMax;

        private long combineSimilarDiffAfter;

        public IndexDisplayConfigProperties() {
            super();
        }

        @Override
        public long getDetailsIndexMax() {
            return detailsIndexMax;
        }

        public void setDetailsIndexMax(long detailsIndexMax) {
            this.detailsIndexMax = detailsIndexMax;
        }

        @Override
        public long getCombineSimilarDiffAfter() {
            return combineSimilarDiffAfter;
        }

        public void setCombineSimilarDiffAfter(long combineSimilarDiffAfter) {
            this.combineSimilarDiffAfter = combineSimilarDiffAfter;
        }
    }
}
