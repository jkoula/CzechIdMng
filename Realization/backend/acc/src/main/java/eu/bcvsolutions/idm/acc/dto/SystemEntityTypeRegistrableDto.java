package eu.bcvsolutions.idm.acc.dto;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

@Relation(collectionRelation = "systemEntityTypes")
public class SystemEntityTypeRegistrableDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	public static final String EMBEDDED_TYPE = "systemEntityType";
	
	private String systemEntityCode;
	private Class<? extends AbstractDto> entityType;
	private boolean isSupportsProvisioning;
	private boolean isSupportsSync;
	private String module;
	
	public String getSystemEntityCode() {
		return systemEntityCode;
	}
	
	public void setSystemEntityCode(String systemEntityCode) {
		this.systemEntityCode = systemEntityCode;
	}
	
	public Class<? extends AbstractDto> getEntityType() {
		return entityType;
	}
	
	public void setEntityType(Class<? extends AbstractDto> entityType) {
		this.entityType = entityType;
	}
	
	public boolean isSupportsProvisioning() {
		return isSupportsProvisioning;
	}
	
	public void setSupportsProvisioning(boolean isSupportsProvisioning) {
		this.isSupportsProvisioning = isSupportsProvisioning;
	}
	
	public boolean isSupportsSync() {
		return isSupportsSync;
	}
	
	public void setSupportsSync(boolean isSupportsSync) {
		this.isSupportsSync = isSupportsSync;
	}
	
	public String getModule() {
		return module;
	}
	
	public void setModule(String module) {
		this.module = module;
	}
}
