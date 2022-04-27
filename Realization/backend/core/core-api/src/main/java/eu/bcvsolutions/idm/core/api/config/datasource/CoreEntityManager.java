package eu.bcvsolutions.idm.core.api.config.datasource;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Target({METHOD, FIELD, PARAMETER})
@Retention(RUNTIME)
@Qualifier(DatasourceConfig.CORE_ENTITY_MANAGER)
public @interface CoreEntityManager {
}
