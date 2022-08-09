package eu.bcvsolutions.idm.core.api.config.datasource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.StreamSupport;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import eu.bcvsolutions.idm.core.api.repository.ExtendedJpaRepositoryFactoryBean;

@Configuration("hikariDatasourceConfig")
@EnableJpaRepositories(
        repositoryFactoryBeanClass = ExtendedJpaRepositoryFactoryBean.class,
        entityManagerFactoryRef = DatasourceConfig.CORE_ENTITY_MANAGER,
        transactionManagerRef = "coreTransactionManager",
        basePackages = {"eu.bcvsolutions.idm"},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION,
                        value = CoreDatasourceExcluded.class
                )
        })
@EnableTransactionManagement
public class DatasourceConfig {

    public static final String CORE_ENTITY_MANAGER = "coreEntityManager";

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public HikariConfig coreDataSourceProperties() {
        return new HikariConfig();
    }

    @Bean("dataSource")
    @Primary
    @FlywayDataSource
    public DataSource dataSource() {
        final HikariConfig hikariConfig = coreDataSourceProperties();
        if (StringUtils.isEmpty(hikariConfig.getJdbcUrl())) {
            hikariConfig.setDriverClassName("org.h2.Driver");
            hikariConfig.setJdbcUrl("jdbc:h2:mem:testdb");
            hikariConfig.setUsername("");
            hikariConfig.setPassword("");
        }
        return new HikariDataSource(hikariConfig);
    }

    @Bean(CORE_ENTITY_MANAGER)
    @CoreEntityManager
    @Primary
    public LocalContainerEntityManagerFactoryBean coreEntityManagerFactory(Environment env) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        final DataSource dataSource = dataSource();
        em.setDataSource(dataSource);
        em.setPackagesToScan("eu.bcvsolutions.idm");
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        final HikariConfig hikariConfig = coreDataSourceProperties();
        if (StringUtils.isEmpty(hikariConfig.getJdbcUrl()) || "org.h2.Driver".equals(hikariConfig.getDriverClassName())) {
            // We do not use Flyway for H2 database
            vendorAdapter.setGenerateDdl(true);
        }
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(true);
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();

        // fill jpa and hibernate properties
        MutablePropertySources propSrcs = ((AbstractEnvironment) env).getPropertySources();
        StreamSupport.stream(propSrcs.spliterator(), false)
                .filter(EnumerablePropertySource.class::isInstance)
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

    @Bean
    @Primary
    public PlatformTransactionManager coreTransactionManager(@CoreEntityManager LocalContainerEntityManagerFactoryBean em) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(em.getObject());
        return transactionManager;
    }

    @Bean
    public JdbcTemplate coreJdbcTemplate(@Qualifier("dataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
