package eu.bcvsolutions.idm.core.api.config.flyway;

import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Used by {@link IdmFlywayPostProcessor} to determine order, in which migrations will be executed.
 *
 * Default order is:
 * - Core module migration
 * - Acc module migration
 * - Rest
 *
 * This comparator uses migration schema table names to determine order of migrations.
 *
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Component("defaultIdmFlywayComparator")
public class DefaultIdmFlywayComparator implements IdmFlywayComparator{

    @Override
    public int compare(Flyway o1, Flyway o2) {
        final Map<String, Integer> moduleTableOrder = Map.of("idm_schema_version_core", 1, "idm_schema_version_acc", 2);
        final Integer o1Order = moduleTableOrder.getOrDefault(o1.getConfiguration().getTable(), moduleTableOrder.size() + 1);
        final Integer o2Order = moduleTableOrder.getOrDefault(o2.getConfiguration().getTable(), moduleTableOrder.size() + 1);
        return o1Order.compareTo(o2Order);
    }

}
