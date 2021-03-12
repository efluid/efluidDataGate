package fr.uem.efluid.config;

import fr.uem.efluid.utils.jpa.HbmInStatementInterceptor;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuration for Manager database (datagate own model)
 *
 * @author elecomte
 * @version 1
 * @since v2.1.10
 */
@Configuration
public class ManagerDatabaseConfig implements HibernatePropertiesCustomizer {

    private static final String HBM_INTERCEPTOR_KEY = "hibernate.session_factory.interceptor";

    @Bean
    public HbmInStatementInterceptor inStatementInterceptor() {
        return new HbmInStatementInterceptor();
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(HBM_INTERCEPTOR_KEY, inStatementInterceptor());
    }
}
