package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityManager;
import eu.bcvsolutions.idm.acc.system.entity.SystemEntityTypeRegistrable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

public class DefaultSysSystemEntityManager implements SysSystemEntityManager {

	@Autowired
	private List<SystemEntityTypeRegistrable> systemEntityTypes;;
	
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
}
