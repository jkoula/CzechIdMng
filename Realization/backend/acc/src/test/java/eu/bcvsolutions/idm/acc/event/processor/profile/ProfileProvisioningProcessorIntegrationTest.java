package eu.bcvsolutions.idm.acc.event.processor.profile;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.impl.IdentitySynchronizationExecutor;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmProfileDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmProfileService;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test provisioning after identity profile is saved.
 * 
 * @author Radek Tomiška
 */
public class ProfileProvisioningProcessorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private SysProvisioningArchiveService provisioningArchiveService;
	@Autowired private IdmIdentityService identityService;
	@Autowired private AttachmentManager attachmentManager;
	@Autowired private IdmProfileService profileService;
	
	@Test
	public void testProvisioningAfterProfileIsSaved() {
		SysSystemDto system = getHelper().createTestResourceSystem(true);
		//
		// check before
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		List<SysProvisioningArchiveDto> content = provisioningArchiveService.find(filter, null).getContent();
		Assert.assertTrue(content.isEmpty());
		//
		// create identity
		IdmIdentityDto identity = getHelper().createIdentity();
		getHelper().createIdentityAccount(system, identity);
		//
		// save identity with account, invoke provisioning = create
		identity = identityService.save(identity);
		//
		content = provisioningArchiveService.find(filter, null).getContent();
		Assert.assertEquals(1, content.size());
		SysProvisioningArchiveDto sysProvisioningArchiveDto = content.get(0);
		Assert.assertEquals(ProvisioningEventType.CREATE, sysProvisioningArchiveDto.getOperationType());
		Assert.assertEquals(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, sysProvisioningArchiveDto.getEntityType());
		Assert.assertEquals(identity.getId(), sysProvisioningArchiveDto.getEntityIdentifier());
		//
		IdmProfileDto profile = getHelper().createProfile(identity);
		//
		// check after create profile - without image
		content = provisioningArchiveService.find(filter, null).getContent();
		Assert.assertEquals(1, content.size());
		//
		IdmAttachmentDto attachment = new IdmAttachmentDto();
		attachment.setOwnerType(AttachmentManager.TEMPORARY_ATTACHMENT_OWNER_TYPE);
		attachment.setName("name-" + UUID.randomUUID());
		attachment.setMimetype(AttachableEntity.DEFAULT_MIMETYPE);
		attachment.setEncoding(AttachableEntity.DEFAULT_ENCODING);
		attachment.setVersionNumber(1);
		attachment.setVersionLabel("1.0");
		attachment.setContentId(UUID.randomUUID());
		attachment.setContentPath("mock");
		attachment.setFilesize(1L);
		attachment.setInputData(IOUtils.toInputStream("mock"));
		attachment = attachmentManager.saveAttachment(profile, attachment);
		profile.setImage(attachment.getId());
		profile = profileService.save(profile);
		//
		content = provisioningArchiveService.find(filter, null).getContent();
		Assert.assertEquals(2, content.size());
		sysProvisioningArchiveDto = content.stream().max(Comparator.comparing(SysProvisioningArchiveDto::getCreated)).orElse(null);
		Assert.assertEquals(ProvisioningEventType.UPDATE, sysProvisioningArchiveDto.getOperationType());
		Assert.assertEquals(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, sysProvisioningArchiveDto.getEntityType());
		Assert.assertEquals(identity.getId(), sysProvisioningArchiveDto.getEntityIdentifier());
		//
		attachment = new IdmAttachmentDto();
		attachment.setOwnerType(AttachmentManager.TEMPORARY_ATTACHMENT_OWNER_TYPE);
		attachment.setName("name-" + UUID.randomUUID());
		attachment.setMimetype(AttachableEntity.DEFAULT_MIMETYPE);
		attachment.setEncoding(AttachableEntity.DEFAULT_ENCODING);
		attachment.setVersionNumber(1);
		attachment.setVersionLabel("1.0");
		attachment.setContentId(UUID.randomUUID());
		attachment.setContentPath("mock");
		attachment.setFilesize(1L);
		attachment.setInputData(IOUtils.toInputStream("mock"));
		attachment = attachmentManager.saveAttachment(profile, attachment);
		profile.setImage(attachment.getId());
		profile = profileService.save(profile);
		content = provisioningArchiveService.find(filter, null).getContent();
		Assert.assertEquals(3, content.size());
		sysProvisioningArchiveDto = content.stream().max(Comparator.comparing(SysProvisioningArchiveDto::getCreated)).orElse(null);
		Assert.assertEquals(ProvisioningEventType.UPDATE, sysProvisioningArchiveDto.getOperationType());
		Assert.assertEquals(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, sysProvisioningArchiveDto.getEntityType());
		Assert.assertEquals(identity.getId(), sysProvisioningArchiveDto.getEntityIdentifier());
		//
		profile.setImage(null);
		profile = profileService.save(profile);
		content = provisioningArchiveService.find(filter, null).getContent();
		Assert.assertEquals(4, content.size());
		sysProvisioningArchiveDto = content.stream().max(Comparator.comparing(SysProvisioningArchiveDto::getCreated)).orElse(null);
		Assert.assertEquals(ProvisioningEventType.UPDATE, sysProvisioningArchiveDto.getOperationType());
		Assert.assertEquals(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, sysProvisioningArchiveDto.getEntityType());
		Assert.assertEquals(identity.getId(), sysProvisioningArchiveDto.getEntityIdentifier());
		//
		profile.setImage(attachment.getId());
		profile = profileService.save(profile);
		profileService.delete(profile);
		content = provisioningArchiveService.find(filter, null).getContent();
		Assert.assertEquals(6, content.size());
		sysProvisioningArchiveDto = content.stream().max(Comparator.comparing(SysProvisioningArchiveDto::getCreated)).orElse(null);
		Assert.assertEquals(ProvisioningEventType.UPDATE, sysProvisioningArchiveDto.getOperationType());
		Assert.assertEquals(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, sysProvisioningArchiveDto.getEntityType());
		Assert.assertEquals(identity.getId(), sysProvisioningArchiveDto.getEntityIdentifier());
		//
		identityService.delete(identity);
	}
	
	@Override
	protected TestHelper getHelper() {
		return (TestHelper) super.getHelper();
	}
}
