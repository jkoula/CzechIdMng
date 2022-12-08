package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.DefaultAccTestHelper;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountRoleAssignmentFilter;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmRequestIdentityRoleService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public class RequestIdentityRoleServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private IdmRequestIdentityRoleService service;

    @Autowired
    DefaultAccTestHelper accTestHelper;

    @Test
    public void testFilteringByAccountId() {
        final AccAccountDto account1 = accTestHelper.createAccount();
        final AccAccountDto account2 = accTestHelper.createAccount();

        final IdmRoleDto role1 = accTestHelper.createRole();
        final IdmRoleDto role2 = accTestHelper.createRole();
        final IdmRoleDto role3 = accTestHelper.createRole();

        final IdmIdentityDto identity = accTestHelper.createIdentity();
        final IdmIdentityContractDto primeContract = accTestHelper.getPrimeContract(identity.getId());

        accTestHelper.assignRoleToAccountViaRequest(account1,null, null, true, role1.getId(), role2.getId());
        accTestHelper.assignRoleToAccountViaRequest(account2,null, null, true, role3.getId());
        accTestHelper.assignRoles(primeContract, role1, role2, role3);

        IdmRequestIdentityRoleFilter filter1 = new IdmRequestIdentityRoleFilter();
        filter1.set(AccAccountRoleAssignmentFilter.PARAMETER_ACCOUNT_ID, account1.getId());
        filter1.setOwnerType(AccAccountDto.class);
        filter1.setOnlyAssignments(true);

        final Page<IdmRequestIdentityRoleDto> idmRequestIdentityRoleDtos = service.find(filter1, null);
        Assert.assertEquals(2, idmRequestIdentityRoleDtos.getTotalElements());
        Assert.assertTrue(containsRole(role1, idmRequestIdentityRoleDtos));
        Assert.assertTrue(containsRole(role2, idmRequestIdentityRoleDtos));

        IdmRequestIdentityRoleFilter filter2 = new IdmRequestIdentityRoleFilter();
        filter2.set(AccAccountRoleAssignmentFilter.PARAMETER_ACCOUNT_ID, account2.getId());
        filter2.setOwnerType(AccAccountDto.class);
        filter2.setOnlyAssignments(true);


        final Page<IdmRequestIdentityRoleDto> idmRequestIdentityRoleDtos2 = service.find(filter2, null);
        Assert.assertEquals(1, idmRequestIdentityRoleDtos2.getTotalElements());
        Assert.assertTrue(containsRole(role3, idmRequestIdentityRoleDtos2));

    }

    private static boolean containsRole(IdmRoleDto role1, Page<IdmRequestIdentityRoleDto> idmRequestIdentityRoleDtos) {
        return idmRequestIdentityRoleDtos.stream().anyMatch(idmRequestIdentityRoleDto -> idmRequestIdentityRoleDto.getRole().equals(role1.getId()));
    }

}
