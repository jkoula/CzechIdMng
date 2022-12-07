package eu.bcvsolutions.idm.acc.rest.impl;

import eu.bcvsolutions.idm.acc.DefaultAccTestHelper;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountRoleAssignmentFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccountRoleAssignment;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleAssignmentService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.RoleBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.identity.IdentityRoleByRoleEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.role.RoleCanBeRequestedEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractRoleAssignmentControllerRestTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Component
public class AccAccountRoleAssignmentControllerRestTest extends AbstractRoleAssignmentControllerRestTest<AccAccountRoleAssignmentDto, AccAccountRoleAssignmentFilter> {

    @Autowired
    AccAccountRoleAssignmentController controller;

    @Autowired
    DefaultAccTestHelper accHelper;

    @Autowired
    IdmIdentityService identityService;

    @Autowired
    AccAccountService accountService;

    @Autowired
    LookupService lookupService;

    @Autowired
    AccAccountRoleAssignmentService accountRoleAssignmentService;


    @Override
    protected AbstractReadWriteDtoController<AccAccountRoleAssignmentDto, ?> getController() {
        return controller;
    }

    @Override
    protected AccAccountRoleAssignmentDto prepareDto() {
        AccAccountRoleAssignmentDto dto = new AccAccountRoleAssignmentDto();
        dto.setAccAccount(accHelper.createIdentityAccount(accHelper.createTestResourceSystem(true), getHelper().createIdentity()).getAccount());
        dto.setRole(getHelper().createRole().getId());
        dto.setValidFrom(LocalDate.now());
        dto.setValidTill(LocalDate.now().plusDays(1));
        dto.setExternalId(getHelper().createName());
        return dto;
    }

    @Override
    public void findByOwnerId() {

    }

    @Override
    protected List<AccAccountRoleAssignmentDto> createTestInvalidRoleAssignments(IdmIdentityDto identity) {
        List<AccAccountRoleAssignmentDto> result = new ArrayList<>();
        final SysSystemDto testResourceSystem = accHelper.createTestResourceSystem(true);
        final AccIdentityAccountDto identityAccount = accHelper.createIdentityAccount(testResourceSystem, identity);
        final AccAccountDto accAccountDto = accountService.get(identityAccount.getAccount());
        final AccAccountRoleAssignmentDto invalidByDate = accHelper.createAccountRoleAssignment(accAccountDto, getHelper().createRole(), null, LocalDate.now().minusDays(2));// inValidByDate
        result.add(invalidByDate);
        return result;
    }

    @Override
    protected IdmAuthorizationPolicyDto getPolicyForRole(UUID roleId) {
        getHelper().createAuthorizationPolicy(
                roleId,
                CoreGroupPermission.ROLE,
                IdmRole.class,
                RoleCanBeRequestedEvaluator.class,
                RoleBasePermission.CANBEREQUESTED, IdmBasePermission.UPDATE, IdmBasePermission.READ);

        ConfigurationMap evaluatorProperties = new ConfigurationMap();
        evaluatorProperties.put(IdentityRoleByRoleEvaluator.PARAMETER_CAN_BE_REQUESTED_ONLY, false);
        return getHelper().createAuthorizationPolicy(
                roleId,
                AccGroupPermission.ACCOUNTROLEASSIGNMENT,
                AccAccountRoleAssignment.class,
                IdentityRoleByRoleEvaluator.class,
                evaluatorProperties);
    }

    @Override
    protected AccAccountRoleAssignmentDto createRoleAssignment(IdmIdentityDto identity, IdmRoleDto roleOne) {
        final SysSystemDto testResourceSystem = accHelper.createTestResourceSystem(true);
        final AccIdentityAccountDto identityAccount = accHelper.createIdentityAccount(testResourceSystem, identity);
        return createRoleAssignment(identityAccount.getAccount(), roleOne);
    }

    @Override
    protected AccAccountRoleAssignmentDto getEmptyRoleAssignment(UUID id) {
        AccAccountRoleAssignmentDto dto = new AccAccountRoleAssignmentDto();
        dto.setAccAccount(id);
        return dto;
    }

    @Override
    protected String getOwnerCode(AccAccountRoleAssignmentDto roleAssignment) {
        final AccAccountDto accAccountDto = accountService.get(roleAssignment.getAccAccount());
        return accAccountDto.getUid();
    }

    @Override
    protected AccAccountRoleAssignmentFilter getFilter() {
        return new AccAccountRoleAssignmentFilter();
    }

    @Override
    protected AccAccountRoleAssignmentDto createRoleAssignment(UUID owner, IdmRoleDto roleOne) {
        accHelper.assignRoleToAccountViaRequest(accountService.get(owner), true, roleOne.getId());

        AccAccountRoleAssignmentFilter filter = new AccAccountRoleAssignmentFilter();
        filter.setAccountId(owner);
        filter.setRoleId(roleOne.getId());

        return accountRoleAssignmentService.find(filter, null, null).getContent().get(0);
    }

    @Override
    protected AccAccountRoleAssignmentDto createRoleAssignment(IdmRoleDto roleOne){
        return createRoleAssignment(getHelper().createIdentity(), roleOne);
    }

    @Override
    protected AccAccountRoleAssignmentDto createRoleAssignment(){
        return createRoleAssignment(getHelper().createRole());
    }
}
