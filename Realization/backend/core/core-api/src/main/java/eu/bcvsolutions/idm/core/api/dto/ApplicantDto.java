package eu.bcvsolutions.idm.core.api.dto;

import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;

import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface ApplicantDto extends ValidableEntity {

    public UUID getId();

    public UUID getConceptOwner();


}
