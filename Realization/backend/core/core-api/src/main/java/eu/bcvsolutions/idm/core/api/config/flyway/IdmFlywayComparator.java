package eu.bcvsolutions.idm.core.api.config.flyway;

import org.flywaydb.core.Flyway;

import java.util.Comparator;

/**
 * Components, which are being used by {@link IdmFlywayPostProcessor} to determine order in which {@link Flyway} migrations will be executed.
 *
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface IdmFlywayComparator extends Comparator<Flyway> {

}
