package eu.bcvsolutions.idm.core.api.service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Suggests that a field containing a reference to the same type the object is itself should be ignored
 * during import.
 *
 * @author Tomáš Doischer
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface IgnoreSameTypeReferenceFieldDuringImport {
}
