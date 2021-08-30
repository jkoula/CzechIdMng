package eu.bcvsolutions.idm.acc.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmSystemDto;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.impl.IcConnectorInstanceImpl;
import io.swagger.annotations.ApiModelProperty;
import java.util.UUID;
import org.springframework.hateoas.core.Relation;

/**
 * Target system setting - is used for account management and provisioning DTO
 * 
 * @author Svanda
 *
 */
@Relation(collectionRelation = "systems")
public class SysSystemDto extends IdmSystemDto {

	private static final long serialVersionUID = 1L;
	public static final String PROPERTY_REMOTE_SERVER = "remoteServer";

	@ApiModelProperty(notes = "Provisioning is disabled on system - just account uid and ACM is executed. Provisioning operation is not created into queue, wish is not constructed.")
	private boolean disabledProvisioning;  // @since 9.6.0
	private boolean queue;
	private Long version; // Optimistic lock - will be used with ETag
	private boolean remote; // @deprecated @since 10.8.0 - remoteServer is used now
	@Embedded(dtoClass = IdmPasswordPolicyDto.class)
	private UUID passwordPolicyValidate;
	@Embedded(dtoClass = IdmPasswordPolicyDto.class)
	private UUID passwordPolicyGenerate;
	private SysConnectorKeyDto connectorKey;
	private SysConnectorServerDto connectorServer;
	private SysBlockedOperationDto blockedOperation;
	@Embedded(dtoClass = SysConnectorServerDto.class)
	private UUID remoteServer;

	public SysSystemDto() {
	}

	public SysSystemDto(UUID id) {
		super(id);
	}
	
	public boolean isQueue() {
		return queue;
	}

	public void setQueue(boolean queue) {
		this.queue = queue;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public boolean isRemote() {
		return remoteServer != null || remote;
	}

	/**
	 * @deprecated @since 10.8.0 - use remoteServer field.
	 */
	@Deprecated
	public void setRemote(boolean remote) {
		this.remote = remote;
	}

	public UUID getPasswordPolicyValidate() {
		return passwordPolicyValidate;
	}

	public void setPasswordPolicyValidate(UUID passwordPolicyValidate) {
		this.passwordPolicyValidate = passwordPolicyValidate;
	}

	public UUID getPasswordPolicyGenerate() {
		return passwordPolicyGenerate;
	}

	public void setPasswordPolicyGenerate(UUID passwordPolicyGenerate) {
		this.passwordPolicyGenerate = passwordPolicyGenerate;
	}

	public SysConnectorKeyDto getConnectorKey() {
		return connectorKey;
	}

	public void setConnectorKey(SysConnectorKeyDto connectorKey) {
		this.connectorKey = connectorKey;
	}

	public SysConnectorServerDto getConnectorServer() {
		return connectorServer;
	}

	public void setConnectorServer(SysConnectorServerDto connectorServer) {
		this.connectorServer = connectorServer;
	}
	
	/**
	 * @deprecated @since 10.8.0 - remote connector server is not supported here
	 * => use {@link SysSystemService#getConnectorInstance(SysSystemDto)} instead 
	 */
	@JsonIgnore
	@Deprecated
	public IcConnectorInstance getConnectorInstance() {
		return new IcConnectorInstanceImpl(this.getConnectorServer(), this.getConnectorKey(), this.isRemote());
	}

	public SysBlockedOperationDto getBlockedOperation() {
		return blockedOperation;
	}

	public void setBlockedOperation(SysBlockedOperationDto blockedOperation) {
		this.blockedOperation = blockedOperation;
	}
	
	/**
	 * Provisioning is disabled on system - just account uid and ACM is executed. Provisioning operation is not created.
	 * 
	 * @param disabledProvisioning
	 * @since 9.6.0 
	 */
	public void setDisabledProvisioning(boolean disabledProvisioning) {
		this.disabledProvisioning = disabledProvisioning;
	}
	
	/**
	 * Provisioning is disabled on system - just account uid and ACM is executed. Provisioning operation is not created.
	 * 
	 * @return
	 * @since 9.6.0 
	 */
	public boolean isDisabledProvisioning() {
		return disabledProvisioning;
	}
	
	/**
	 * Remote server.
	 * 
	 * @return remote server identifier
	 * @since 10.8.0
	 */
	public UUID getRemoteServer() {
		return remoteServer;
	}
	
	/**
	 * Remote server.
	 * 
	 * @param remoteServer remote server identifier
	 * @since 10.8.0
	 */
	public void setRemoteServer(UUID remoteServer) {
		this.remoteServer = remoteServer;
	}
}
