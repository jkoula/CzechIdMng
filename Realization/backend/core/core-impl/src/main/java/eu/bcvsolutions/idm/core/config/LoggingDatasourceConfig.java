package eu.bcvsolutions.idm.core.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import eu.bcvsolutions.idm.core.api.config.datasource.DatasourceConfig;
import eu.bcvsolutions.idm.core.api.repository.ExtendedJpaRepositoryFactoryBean;
import eu.bcvsolutions.idm.core.audit.repository.IdmLoggingEventExceptionRepository;
import eu.bcvsolutions.idm.core.audit.repository.IdmLoggingEventPropertyRepository;
import eu.bcvsolutions.idm.core.audit.repository.IdmLoggingEventRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.*;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.StreamSupport;

@Configuration
@EnableJpaRepositories(
        repositoryFactoryBeanClass = ExtendedJpaRepositoryFactoryBean.class,
        basePackages = {"eu.bcvsolutions.idm"},
        entityManagerFactoryRef = "loggingEntityManagerFactory",
        transactionManagerRef = "loggingTransactionManager",

        includeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                                IdmLoggingEventRepository.class,
                                IdmLoggingEventExceptionRepository.class,
                                IdmLoggingEventPropertyRepository.class
                        }
                )
        })
@EnableTransactionManagement
public class LoggingDatasourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.logging-datasource")
    public HikariConfig loggingHikariProperties() {
        return new HikariConfig();
    }

    @Bean("loggingDatasource")
    public DataSource loggingDatasource() {
        final HikariConfig hikariConfig = loggingHikariProperties();
        if (StringUtils.isEmpty(hikariConfig.getJdbcUrl())) {
            hikariConfig.setDriverClassName("org.h2.Driver");
            hikariConfig.setJdbcUrl("jdbc:h2:mem:testdb");
        }
        return new HikariDataSource(hikariConfig);
    }

    @Bean("loggingEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean loggingEntityManagerFactory(@Qualifier("loggingDatasource") DataSource datasource,
                                                                       Environment env) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(datasource);
        em.setPackagesToScan("eu.bcvsolutions.idm");
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        final HikariConfig hikariConfig = loggingHikariProperties();
        if (StringUtils.isEmpty(hikariConfig.getJdbcUrl()) || "org.h2.Driver".equals(hikariConfig.getDriverClassName())) {
            // We do not use Flyway for H2 database
            vendorAdapter.setGenerateDdl(true);
        }
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        MutablePropertySources propSrcs = ((AbstractEnvironment) env).getPropertySources();
        StreamSupport.stream(propSrcs.spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames())
                .flatMap(Arrays::stream)
                .filter(propName -> propName.startsWith("hibernate") || propName.startsWith("spring.jpa.properties"))
                .forEach(propName -> properties.put(
                        propName
                                .replace("spring.jpa.properties.", "")
                        ,
                        env.getProperty(propName)));
        em.setJpaPropertyMap(properties);
        return em;

    }

    @Bean("loggingTransactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("loggingEntityManagerFactory") LocalContainerEntityManagerFactoryBean em) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(em.getObject());
        return transactionManager;
    }

}
