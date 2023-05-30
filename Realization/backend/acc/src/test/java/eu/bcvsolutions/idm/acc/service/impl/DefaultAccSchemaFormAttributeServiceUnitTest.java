package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.repository.AccSchemaFormAttributeRepository;
import eu.bcvsolutions.idm.acc.repository.SysSchemaObjectClassRepository;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.UUID;

import static org.junit.Assert.*;

public class DefaultAccSchemaFormAttributeServiceUnitTest extends AbstractUnitTest {

    @Mock
    AccSchemaFormAttributeRepository repository;
    @Mock
    EntityEventManager entityEventManager;

    // test generating form definition name
    @Test
    public void testCreateFormDefinitionCode() {
        final var service = new DefaultAccSchemaFormAttributeService(repository , entityEventManager);
        final var systemDto = new SysSystemDto(UUID.randomUUID());
        final var objectClass = new SysSchemaObjectClassDto();
        objectClass.setId(UUID.randomUUID());

        String code = service.createFormDefinitionCode(systemDto, objectClass);
        System.out.println(code);
    }


}