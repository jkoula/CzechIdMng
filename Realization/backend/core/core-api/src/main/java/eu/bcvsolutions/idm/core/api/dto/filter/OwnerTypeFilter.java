package eu.bcvsolutions.idm.core.api.dto.filter;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface OwnerTypeFilter extends BaseDataFilter {

    String OWNER_TYPE = "ownerType";

    default Class<?> getOwnerType() {
        return getParameterConverter().toClass(getData(), OWNER_TYPE);
    }
    default void setOwnerType(Class<?> ownerType) {
        set(OWNER_TYPE, ownerType);
    }

}
