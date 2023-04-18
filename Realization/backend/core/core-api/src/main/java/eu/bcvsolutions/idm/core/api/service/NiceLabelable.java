package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface NiceLabelable<T extends BaseDto> {
    /**
     * Better "toString".
     * Returns identity's fullName with titles if lastName is not blank, otherwise returns username
     *
     * @param identity
     * @return
     */
    String getNiceLabel(T identity);
}
