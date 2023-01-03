package eu.bcvsolutions.idm.core.api.dto;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.ApplicantDto;
import org.modelmapper.ModelMapper;

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
    private String applicantType;

    public ApplicantImplDto() {
    }

    public ApplicantImplDto(UUID id, String applicantType) {
        this.id = id;
        this.applicantType = applicantType;
    }

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

    public void setApplicantType(String applicantType) {
        this.applicantType = applicantType;
    }

    @Override
    public String getApplicantType() {
        return applicantType;
    }

    public void setConceptOwner(UUID conceptOwner) {
        this.conceptOwner = conceptOwner;
    }

    public void setValidTill(LocalDate validTill) {
        this.validTill = validTill;
    }

    public static final class Converter implements com.fasterxml.jackson.databind.util.Converter {
        @Override
        public Object convert(Object o) {
            // this should contain only basic types, so we can use bare ModelMapper
            ModelMapper mapper = new ModelMapper();
            ApplicantImplDto result = new ApplicantImplDto();
            mapper.map(o, result);
            return result;
        }

        @Override
        public JavaType getInputType(TypeFactory typeFactory) {
            return typeFactory.constructFromCanonical(Object.class.getCanonicalName());
        }

        @Override
        public JavaType getOutputType(TypeFactory typeFactory) {
            return typeFactory.constructFromCanonical(ApplicantImplDto.class.getCanonicalName());
        }
    }
}
