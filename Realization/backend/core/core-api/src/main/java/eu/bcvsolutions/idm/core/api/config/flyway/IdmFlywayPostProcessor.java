package eu.bcvsolutions.idm.core.api.config.flyway;

import org.flywaydb.core.Flyway;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * {@link FlywayMigrationStrategy} itself can't be used for modular {@link Flyway} configuration. 
 * We need to use {@link FlywayMigrationStrategy} directly after module dependent {@link Flyway} is created.
 *
 * @since 12.2.x this component is {@link ApplicationContextAware} instead of {@link org.springframework.beans.factory.config.BeanPostProcessor}.
 * The reason is that due to some circular dependencies on {@link eu.bcvsolutions.idm.core.api.config.datasource.DatasourceConfig}, {@link Flyway}
 * beans could not be processed.
 *
 * This solution also uses {@link IdmFlywayComparator}, which enables us to specify order in which migrations will be executed.
 *
 * @author Peter Štrunc <github.com/peter-strunc>
 * @author Radek Tomiška
 */
@Component(IdmFlywayPostProcessor.NAME)
public class IdmFlywayPostProcessor implements ApplicationContextAware {
	
	public static final String NAME = "flywayPostProcessor";
	
	@Autowired
	private FlywayMigrationStrategy flywayMigrationStrategy;

	@Autowired
	IdmFlywayComparator flywayComparator;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		final Map<String, Flyway> flyways = applicationContext.getBeansOfType(Flyway.class);
		flyways.values().stream().sorted(flywayComparator).forEach(this::executeMigration);
	}

	private void executeMigration(Flyway flyway) {
		flywayMigrationStrategy.migrate(flyway);
	}
}
