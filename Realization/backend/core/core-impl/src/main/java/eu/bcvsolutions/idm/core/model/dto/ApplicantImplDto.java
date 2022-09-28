package eu.bcvsolutions.idm.core.model.dto;

import eu.bcvsolutions.idm.core.api.dto.ApplicantDto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public class ApplicantImplDto implements ApplicantDto {

    private UUID id;

    private UUID conceptOwner;
    private LocalDate validFrom;
    private LocalDate validTill;

    @Override
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTill() {
        return validTill;
    }

    @Override
    public UUID getConceptOwner() {
        return conceptOwner;
    }

    public void setConceptOwner(UUID conceptOwner) {
        this.conceptOwner = conceptOwner;
    }

    public void setValidTill(LocalDate validTill) {
        this.validTill = validTill;
    }
}
