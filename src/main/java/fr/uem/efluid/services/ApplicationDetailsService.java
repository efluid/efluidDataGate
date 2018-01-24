package fr.uem.efluid.services;

import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class ApplicationDetailsService {

    /**
     *
     * @return
     */
    public ApplicationDetails getCurrentDetails(){

        ApplicationDetails details = new ApplicationDetails();

        // TODO : replace hard codded
        details.setCommitsCount(15);
        details.setDbUrl("ORACLE_DB_SERVER_123/ABCDEF");
        details.setDomainsCount(25);

        return details;
    }

    /**
     *
     */
    public static class ApplicationDetails {

        private String dbUrl;
        private int domainsCount;
        private int commitsCount;

        public String getDbUrl() {
            return this.dbUrl;
        }

        public void setDbUrl(String dbUrl) {
            this.dbUrl = dbUrl;
        }

        public int getDomainsCount() {
            return this.domainsCount;
        }

        public void setDomainsCount(int domainsCount) {
            this.domainsCount = domainsCount;
        }

        public int getCommitsCount() {
            return this.commitsCount;
        }

        public void setCommitsCount(int commitsCount) {
            this.commitsCount = commitsCount;
        }
    }
}
