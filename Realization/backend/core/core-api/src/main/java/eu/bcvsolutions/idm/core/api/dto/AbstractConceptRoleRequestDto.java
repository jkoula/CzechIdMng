package eu.bcvsolutions.idm.core.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

import java.io.ObjectInputStream;
import java.time.LocalDate;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public abstract class AbstractConceptRoleRequestDto extends FormableDto implements Loggable {

    public static final String WF_PROCESS_FIELD = "wfProcessId";
    public static final String DUPLICATES = "duplicates";
    protected UUID roleRequest;
    @Embedded(dtoClass = IdmRoleDto.class)
    protected UUID role;
    protected LocalDate validFrom;
    protected LocalDate validTill;
    protected ConceptRoleRequestOperation operation;
    protected RoleRequestState state;
    protected String wfProcessId;
    protected String log;
    protected boolean valid = true; // Is concept valid?
    protected UUID roleSystem;
    protected UUID roleTreeNode; // Relation on sys-system form ACC. We need to working with that also in the core module (cross-domains).
    /*
     *  Is concept duplicate and with identity role or another concept,
     *  if boolean is null. Duplicated wasn't processed. Duplicates can be
     *  filled by IdmRoleRequestService#markDuplicates
     *  @since 9.6.0
     */
    protected Boolean duplicate = null;
    protected OperationResultDto systemState;
    protected UUID directRole; // directly assigned business role
    protected UUID directConcept; // directly assigned business role concept (when new business role is assigned)
    protected UUID roleComposition; // business role definition

    public UUID getRoleRequest() {
        return roleRequest;
    }

    public void setRoleRequest(UUID roleRequest) {
        this.roleRequest = roleRequest;
    }

    public UUID getRole() {
        return role;
    }

    public void setRole(UUID role) {
        this.role = role;
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

    public void setValidTill(LocalDate validTill) {
        this.validTill = validTill;
    }

    public ConceptRoleRequestOperation getOperation() {
        return operation;
    }

    public void setOperation(ConceptRoleRequestOperation operation) {
        this.operation = operation;
    }

    public RoleRequestState getState() {
        return state;
    }

    public void setState(RoleRequestState state) {
        this.state = state;
    }

    public String getWfProcessId() {
        return wfProcessId;
    }

    public void setWfProcessId(String wfProcessId) {
        this.wfProcessId = wfProcessId;
    }

    public UUID getAutomaticRole() {
        return roleTreeNode;
    }

    public void setAutomaticRole(UUID automaticRole) {
        this.roleTreeNode = automaticRole;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String addToLog(String text) {
        if (text != null) {
            StringBuilder builder = new StringBuilder();
            if (this.log != null) {
                builder.append(this.log);
                builder.append("\n" + IdmRoleRequestDto.LOG_SEPARATOR + "\n");
            }
            builder.append(text);
            this.setLog(builder.toString());
        }
        return this.getLog();
    }

    public Boolean getDuplicate() {
        return duplicate;
    }

    public void setDuplicate(Boolean duplicate) {
        this.duplicate = duplicate;
    }

    @JsonIgnore
    public DuplicateRolesDto getDuplicates() {
        DuplicateRolesDto duplicates = (DuplicateRolesDto) this.getEmbedded().get(AbstractConceptRoleRequestDto.DUPLICATES);
        if (duplicates == null) {
            return new DuplicateRolesDto();
        }
        return duplicates;
    }

    public void setDuplicates(DuplicateRolesDto duplicates) {
        this.getEmbedded().put(AbstractConceptRoleRequestDto.DUPLICATES, duplicates);
    }

    public OperationResultDto getSystemState() {
        return systemState;
    }

    public void setSystemState(OperationResultDto systemState) {
        this.systemState = systemState;
    }

    /**
     * Concept for business role.
     *
     * @return directly assigned role
     * @since 10.6.0
     */
    public UUID getDirectRole() {
        return directRole;
    }

    /**
     * Concept for business role.
     *
     * @param directRole directly assigned role concept
     * @since 10.6.0
     */
    public void setDirectRole(UUID directRole) {
        this.directRole = directRole;
    }

    /**
     * Concept for business role concept.
     *
     * @return directly assigned role concept
     * @since 10.6.0
     */
    public UUID getDirectConcept() {
        return directConcept;
    }

    /**
     * Concept for business role concept.
     *
     * @param directConcept directly assigned role concept
     * @since 10.6.0
     */
    public void setDirectConcept(UUID directConcept) {
        this.directConcept = directConcept;
    }

    /**
     * Concept for business role definition.
     *
     * @return business role definition
     * @since 10.6.0
     */
    public UUID getRoleComposition() {
        return roleComposition;
    }

    /**
     * Concept for business role definition.
     *
     * @param roleComposition business role definition
     * @since 10.6.0
     */
    public void setRoleComposition(UUID roleComposition) {
        this.roleComposition = roleComposition;
    }

    public UUID getRoleSystem() {
        return roleSystem;
    }

    public void setRoleSystem(UUID roleSystem) {
        this.roleSystem = roleSystem;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AbstractConceptRoleRequestDto)) {
            return false;
        }
        AbstractConceptRoleRequestDto other = (AbstractConceptRoleRequestDto) obj;

        if (operation != other.operation) {
            return false;
        }
        if (role == null) {
            if (other.role != null) {
                return false;
            }
        } else if (!role.equals(other.role)) {
            return false;
        }
        if (state != other.state) {
            return false;
        }
        if (validFrom == null) {
            if (other.validFrom != null) {
                return false;
            }
        } else if (!validFrom.equals(other.validFrom)) {
            return false;
        }
        if (validTill == null) {
            if (other.validTill != null) {
                return false;
            }
        } else if (!validTill.equals(other.validTill)) {
            return false;
        }
        if (wfProcessId == null) {
            if (other.wfProcessId != null) {
                return false;
            }
        } else if (!wfProcessId.equals(other.wfProcessId)) {
            return false;
        }
        if (roleSystem == null) {
            return other.roleSystem == null;
        } else return roleSystem.equals(other.roleSystem);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((operation == null) ? 0 : operation.hashCode());
        result = prime * result + ((role == null) ? 0 : role.hashCode());
        result = prime * result + ((roleRequest == null) ? 0 : roleRequest.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result = prime * result + ((validFrom == null) ? 0 : validFrom.hashCode());
        result = prime * result + ((validTill == null) ? 0 : validTill.hashCode());
        result = prime * result + ((wfProcessId == null) ? 0 : wfProcessId.hashCode());
        return result;
    }

    private void readObject(ObjectInputStream ois) throws Exception {
        ObjectInputStream.GetField readFields = ois.readFields();
        //
        roleRequest = (UUID) readFields.get("roleRequest", null);
        role = (UUID) readFields.get("role", null);
        roleTreeNode = (UUID) readFields.get("roleTreeNode", null);
        validFrom = DtoUtils.toLocalDate(readFields.get("validFrom", null));
        validTill = DtoUtils.toLocalDate(readFields.get("validTill", null));
        operation = (ConceptRoleRequestOperation) readFields.get("operation", null);
        state = (RoleRequestState) readFields.get("state", null);
        wfProcessId = (String) readFields.get("wfProcessId", null);
        log = (String) readFields.get("log", null);
        valid = readFields.get("valid", false);
        duplicate = (Boolean) readFields.get("duplicate", null);
        systemState = (OperationResultDto) readFields.get("systemState", null);
        roleSystem = (UUID) readFields.get("roleSystem", null);
    }

    public abstract UUID getRoleAssignmentUuid();

    public abstract void setRoleAssignmentUuid(UUID id);

    public abstract UUID getOwnerUuid();

    public abstract void setOwnerUuid(UUID id);

    public abstract AbstractConceptRoleRequestDto copy();

    public abstract Class<? extends Identifiable> getOwnerType();
}
