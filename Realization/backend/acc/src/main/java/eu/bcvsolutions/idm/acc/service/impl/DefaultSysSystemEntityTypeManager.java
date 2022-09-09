package eu.bcvsolutions.idm.acc.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.beust.jcommander.internal.Lists;

import eu.bcvsolutions.idm.acc.dto.SystemEntityTypeRegistrableDto;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityTypeManager;
import eu.bcvsolutions.idm.acc.system.entity.SystemEntityTypeRegistrable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

@Component("systemEntityManager")
public class DefaultSysSystemEntityTypeManager implements SysSystemEntityTypeManager {

	@Autowired
	private List<SystemEntityTypeRegistrable> systemEntityTypes;
	@Autowired
	private ModelMapper mapper;
	
	@Override
	public SystemEntityTypeRegistrable getSystemEntityByCode(String code) {
		for (SystemEntityTypeRegistrable entityType : systemEntityTypes) {
			if (entityType.getSystemEntityCode().equals(code)) {
				return entityType;
			}
		}
		
		return null;
	}

	@Override
	public SystemEntityTypeRegistrable getSystemEntityByClass(Class<? extends AbstractDto> clazz) {
		for (SystemEntityTypeRegistrable entityType : systemEntityTypes) {
			if (entityType.getEntityType().equals(clazz)) {
				return entityType;
			}
		}
		
		return null;
	}

	@Override
	public List<SystemEntityTypeRegistrableDto> getSupportedEntityTypes() {
		List<SystemEntityTypeRegistrableDto> entityTypes = Lists.newArrayList();
		
		for (SystemEntityTypeRegistrable systemEntityType : systemEntityTypes) {
			final SystemEntityTypeRegistrableDto systemEntityTypeDto = new SystemEntityTypeRegistrableDto();
			mapper.map(systemEntityType, systemEntityTypeDto);
			entityTypes.add(systemEntityTypeDto);
		}
		
		return entityTypes;
	}

	@Override
	public SystemEntityTypeRegistrableDto getSystemEntityDtoByCode(String code) {
		return getSystemEntityDtoByCode(code, null);
	}

	@Override
	public SystemEntityTypeRegistrableDto getSystemEntityDtoByCode(String code, String mappingId) {
		SystemEntityTypeRegistrable systemEntityType = this.getSystemEntityByCode(code);
		final SystemEntityTypeRegistrableDto systemEntityTypeDto = new SystemEntityTypeRegistrableDto();
		mapper.map(systemEntityType, systemEntityTypeDto);
		// add additional attributes if there are some
		if (systemEntityTypeDto.getSupportedAttributes() == null) {
			systemEntityTypeDto.setSupportedAttributes(new ArrayList<>());
		}
		if (StringUtils.isNotBlank(mappingId)) {
			systemEntityTypeDto.getSupportedAttributes().addAll(systemEntityType.getAdditionalAttributes(mappingId));
		}
		return systemEntityTypeDto;
	}
}
