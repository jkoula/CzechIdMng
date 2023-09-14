package eu.bcvsolutions.idm.doc.config.flyway;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;

import eu.bcvsolutions.idm.core.api.config.flyway.AbstractFlywayConfiguration;
import eu.bcvsolutions.idm.core.api.config.flyway.IdmFlywayAutoConfiguration;

/**
 * DB migration for Doc module
 *
 * @author Jirka Koula
 */
@Configuration
@ConditionalOnClass(Flyway.class)
@ConditionalOnProperty(prefix = "flyway", name = "enabled", matchIfMissing = false)
@AutoConfigureAfter(IdmFlywayAutoConfiguration.IdmFlywayConfiguration.class)
@EnableConfigurationProperties(FlywayProperties.class)
@PropertySource("classpath:/flyway-doc.properties")
public class DocFlywayConfig extends AbstractFlywayConfiguration {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DocFlywayConfig.class);

	@Bean
	@DependsOn("flywayCore")
	@ConditionalOnMissingBean(name = "flywayModuleDoc")
	@ConditionalOnExpression("${flyway.enabled:true} && '${flyway.doc.locations}'!=''")
	@ConfigurationProperties(prefix = "flyway.doc")
	public Flyway flywayModuleDoc() {
		Flyway flyway = super.createFlyway();
		LOG.info("Starting flyway migration for doc module [{}]: ", flyway.getConfiguration().getTable());
		return flyway;
	}
}
