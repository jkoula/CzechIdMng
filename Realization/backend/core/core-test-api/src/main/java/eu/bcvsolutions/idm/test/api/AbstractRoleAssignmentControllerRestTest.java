package eu.bcvsolutions.idm.test.api;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseRoleAssignmentFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.RoleBasePermission;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public abstract class AbstractRoleAssignmentControllerRestTest<A extends AbstractRoleAssignmentDto, F extends BaseRoleAssignmentFilter> extends AbstractReadWriteDtoControllerRestTest<A> {

    @Autowired
    private IdmRoleAssignmentService<A, F> service;

    @Autowired private RoleConfiguration roleConfiguration;
    @Autowired private IdmRoleService roleService;
    @Autowired private IdmAuthorizationPolicyService authorizationPolicyService;

    @Test
    public void testFindByText() {
        // username
        A roleAssignment =  createRoleAssignment();
        createRoleAssignment();
        //
        F filter = getFilter();
        filter.setText(getOwnerCode(roleAssignment));
        List<A> results = find(filter);
        //
        Assert.assertEquals(1, results.size());
        Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(roleAssignment.getId())));
    }

    @Test
    public void testFindInvalidRoles() {
        IdmIdentityDto identity = getHelper().createIdentity();
        getHelper().createIdentityRole(identity, getHelper().createRole()); // valid
        final List<A> testInvalidRoleAssignments = createTestInvalidRoleAssignments(identity);

        //
        F filter = getFilter();
        filter.setIdentityId(identity.getId());
        filter.setValid(Boolean.FALSE);
        List<A> results = find(filter);
        //
        Assert.assertEquals(testInvalidRoleAssignments.size(), results.size());
        Assert.assertTrue(results.containsAll(testInvalidRoleAssignments));
        Assert.assertTrue(testInvalidRoleAssignments.containsAll(results));
    }

    @Test
    public void testFindValidRoles() {
        IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
        A validRole = createRoleAssignment(identity, getHelper().createRole()); // valid
        createTestInvalidRoleAssignments(identity);
        //
        IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
        filter.setIdentityId(identity.getId());
        filter.setValid(Boolean.TRUE);
        List<A> results = find(filter);
        //
        Assert.assertEquals(1, results.size());
        Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(validRole.getId())));
    }


    @Test
    public void findDirectRoles() {
        A normal = createRoleAssignment(); // normal
        // not direct
        A notDirectIdentityRole = getEmptyRoleAssignment(normal.getEntity());
        notDirectIdentityRole.setRole(getHelper().createRole().getId());
        notDirectIdentityRole.setDirectRole(normal.getId());
        A notDirect = createDto(notDirectIdentityRole);
        //
        F filter = getFilter();
        filter.setOwnerId(normal.getEntity());
        filter.setDirectRole(Boolean.TRUE);
        List<A> results = find(filter);
        //
        Assert.assertEquals(1, results.size());
        Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(normal.getId())));
        //
        filter.setDirectRole(Boolean.FALSE);
        results = find(filter);
        //
        Assert.assertEquals(1, results.size());
        Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(notDirect.getId())));
        //
        // find by direct role
        filter.setDirectRole(null);
        filter.setDirectRoleId(normal.getId());
        results = find(filter);
        //
        Assert.assertEquals(1, results.size());
        Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(notDirect.getId())));
    }

    @Test
    public void testFindByRoleEnvironment() {
        IdmRoleDto roleOne = getHelper().createRole(null, getHelper().createName(), getHelper().createName());
        IdmRoleDto roleTwo = getHelper().createRole(null, getHelper().createName(), getHelper().createName());
        A createIdentityRole = createRoleAssignment(roleOne);
        createRoleAssignment(createIdentityRole.getEntity(), roleTwo);
        //
        F filter = getFilter();
        filter.setRoleEnvironment(roleOne.getEnvironment());
        List<A> results = find(filter);
        Assert.assertEquals(1, results.size());
        Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(createIdentityRole.getId())));
    }

    @Test
    public void testFindByRoleId() {
        IdmRoleDto roleOne = getHelper().createRole();
        IdmRoleDto roleTwo = getHelper().createRole();
        A createIdentityRole = createRoleAssignment(roleOne);
        createRoleAssignment(createIdentityRole.getEntity(), roleTwo);
        //
        F filter = getFilter();
        filter.setRoleId(roleOne.getId());
        List<A> results = find(filter);
        Assert.assertEquals(1, results.size());
        Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(createIdentityRole.getId())));
    }

    @Test
    public void testFindByRoleText() {
        IdmRoleDto roleOne = getHelper().createRole();
        IdmRoleDto roleTwo = getHelper().createRole();
        A createIdentityRole = createRoleAssignment(roleOne);
        createRoleAssignment(createIdentityRole.getEntity(), roleTwo);
        //
        IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
        filter.setRoleText(roleOne.getCode());
        List<A> results = find(filter);
        Assert.assertEquals(1, results.size());
        Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(createIdentityRole.getId())));
    }

    @Test
    public void testFindByRoleCatalogueId() {
        IdmRoleDto roleOne = getHelper().createRole();
        IdmRoleDto roleTwo = getHelper().createRole();
        IdmRoleCatalogueDto roleCatalogueOne = getHelper().createRoleCatalogue();
        getHelper().createRoleCatalogueRole(roleOne, roleCatalogueOne);
        IdmRoleCatalogueDto roleCatalogueTwo = getHelper().createRoleCatalogue();
        getHelper().createRoleCatalogueRole(roleTwo, roleCatalogueTwo);
        A createIdentityRole = createRoleAssignment(roleOne);
        createRoleAssignment(createIdentityRole.getEntity(), roleTwo);
        //
        IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
        filter.setRoleCatalogueId(roleCatalogueOne.getId());
        List<A> results = find(filter);
        Assert.assertEquals(1, results.size());
        Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(createIdentityRole.getId())));
    }

    @Test
    public void testFindByRoleComposition() {
        IdmRoleDto roleOne = getHelper().createRole();
        IdmRoleDto roleTwo = getHelper().createRole();
        IdmRoleDto roleThree = getHelper().createRole();
        //
        IdmRoleCompositionDto roleCompositionOne = getHelper().createRoleComposition(roleOne, roleTwo);
        getHelper().createRoleComposition(roleTwo, roleThree);
        //
        IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
        A directRole = createRoleAssignment(identity, roleOne);
        //
        F filter = getFilter();
        filter.setIdentityId(identity.getId());
        filter.setRoleCompositionId(roleCompositionOne.getId());
        List<A> results = find(filter);
        //
        Assert.assertEquals(1, results.size());
        Assert.assertTrue(results.stream().anyMatch(ir -> ir.getDirectRole().equals(directRole.getId())
                && ir.getRole().equals(roleTwo.getId())));
    }

    @Test
    public void testFindCanBeRequestedRoles() throws Exception {
        String defaultRoleCode = roleConfiguration.getDefaultRoleCode();
        //
        try {
            // empty property => disable default role
            getHelper().setConfigurationValue(RoleConfiguration.PROPERTY_DEFAULT_ROLE, "");

            IdmRoleDto roleOne = createRole(true);
            IdmRoleDto roleTwo = createRole(false); // other
            //
            IdmIdentityDto identity = getHelper().createIdentity();
            IdmRoleDto permissionRole = getHelper().createRole();
            //
            getHelper().createIdentityRole(getHelper().getPrimeContract(identity),  permissionRole);
            //
            // other identity - their identity roles we will read
            IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
            createRoleAssignment(identityTwo, roleOne);
            createRoleAssignment(identityTwo, roleTwo);
            //
            // create authorization policy - assign to role

            // with update transitively
            IdmAuthorizationPolicyDto transientIdentityRolePolicy = getPolicyForRole(permissionRole.getId());

            //
            IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
            filter.setIdentityId(identityTwo.getId());
            List<A> identityRoles = find("can-be-requested", filter, getAuthentication(identity.getUsername()));
            //
            Assert.assertFalse(identityRoles.isEmpty());
            Assert.assertEquals(1, identityRoles.size());
            Assert.assertTrue(identityRoles.stream().anyMatch(r -> r.getRole().equals(roleOne.getId())));
            //
            List<String> permissions = getPermissions(identityRoles.get(0), getAuthentication(identity.getUsername()));
            //
            Assert.assertEquals(3, permissions.size());
            Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(RoleBasePermission.CANBEREQUESTED.name())));
            Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.UPDATE.name())));
            Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.name())));
            //
            // can be requested only
            ConfigurationMap evaluatorProperties = new ConfigurationMap();
            // this is not ideal, but it is better than having to add dependency on core-impl or moving evaluator to API
            evaluatorProperties.put("can-be-requested-only", true);
            transientIdentityRolePolicy.setEvaluatorProperties(evaluatorProperties);
            authorizationPolicyService.save(transientIdentityRolePolicy);
            //
            identityRoles = find("can-be-requested", filter, getAuthentication(identity.getUsername()));
            //
            Assert.assertFalse(identityRoles.isEmpty());
            Assert.assertEquals(1, identityRoles.size());
            Assert.assertTrue(identityRoles.stream().anyMatch(r -> r.getRole().equals(roleOne.getId())));
            //
            // read authority is not available now
            try {
                getHelper().login(identity);
                //
                Set<String> canBeRequestedPermissions = service.getPermissions(identityRoles.get(0).getId());
                //
                Assert.assertEquals(1, canBeRequestedPermissions.size());
                Assert.assertTrue(canBeRequestedPermissions.stream().anyMatch(p -> p.equals(RoleBasePermission.CANBEREQUESTED.name())));
            } finally {
                logout();
            }
        } finally {
            getHelper().setConfigurationValue(RoleConfiguration.PROPERTY_DEFAULT_ROLE, defaultRoleCode);
        }
    }

    @Test
    public abstract void findByOwnerId();
    protected abstract List<A> createTestInvalidRoleAssignments(IdmIdentityDto identity);

    protected abstract IdmAuthorizationPolicyDto getPolicyForRole(UUID roleId);

    protected abstract A createRoleAssignment(IdmIdentityDto identity, IdmRoleDto roleOne);

    protected abstract A getEmptyRoleAssignment(UUID id);

    protected abstract String getOwnerCode(A roleAssignment);

    protected abstract F getFilter();

    protected abstract A createRoleAssignment(UUID owner, IdmRoleDto roleOne);

    protected A createRoleAssignment(IdmRoleDto roleOne){
        return createRoleAssignment(getHelper().createContract(getHelper().createIdentity()).getId(), roleOne);
    }
    protected A createRoleAssignment(){
        return createRoleAssignment(getHelper().createRole());
    }

    private IdmRoleDto createRole( boolean canBeRequested) {
        IdmRoleDto role = new IdmRoleDto();
        role.setCode(getHelper().createName());
        role.setName(role.getCode());
        role.setCanBeRequested(canBeRequested);
        //
        return roleService.save(role);
    }

    @Test
    @Ignore
    @Override
    public void testSaveFormDefinition() throws Exception {
        // We don't want testing form definition - IdentityRole has extra behavior (role attributes) for Form (definition is changing by role)
    }

    @Test
    @Ignore
    @Override
    public void testSaveFormValue() throws Exception {
        // We don't want testing form definition - IdentityRole has extra behavior (role attributes) for Form (definition is changing by role)
    }

    @Test
    @Ignore
    @Override
    public void testDownloadFormValue() throws Exception {
        // We don't want testing form definition - IdentityRole has extra behavior (role attributes) for Form (definition is changing by role)
    }

    @Test
    @Ignore
    @Override
    public void testPreviewFormValue() throws Exception {
        // We don't want testing form definition - IdentityRole has extra behavior (role attributes) for Form (definition is changing by role)
    }

    @Override
    protected boolean isReadOnly() {
        return true;
    }
}
