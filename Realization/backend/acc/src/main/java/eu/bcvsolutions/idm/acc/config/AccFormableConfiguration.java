package eu.bcvsolutions.idm.acc.config;

import eu.bcvsolutions.idm.acc.entity.AccAccountConceptRoleRequest;
import eu.bcvsolutions.idm.acc.entity.AccAccountConceptRoleRequestFormValue;
import eu.bcvsolutions.idm.acc.entity.AccAccountRoleAssignment;
import eu.bcvsolutions.idm.acc.entity.AccAccountRoleAssignmentFormValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.bcvsolutions.idm.acc.eav.entity.AccAccountFormValue;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.core.eav.repository.AbstractFormValueRepository;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormValueService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

@Configuration
public class AccFormableConfiguration {

	@Bean
    public AbstractFormValueService<AccAccount, AccAccountFormValue> certificateFormValueService(
            AbstractFormValueRepository<AccAccount, AccAccountFormValue> repository) {

        return new AbstractFormValueService<AccAccount, AccAccountFormValue>(repository) {

            /**
             * Account form values supports authorization policies.
             */
            @Override
            public AuthorizableType getAuthorizableType() {
                return new AuthorizableType(CoreGroupPermission.FORMVALUE, getEntityClass());
            }
        };
    }

    @Bean
    public AbstractFormValueService<AccAccountConceptRoleRequest, AccAccountConceptRoleRequestFormValue> conceptFormValueService(
            AbstractFormValueRepository<AccAccountConceptRoleRequest, AccAccountConceptRoleRequestFormValue> repository) {

        return new AbstractFormValueService<>(repository) {
            @Override
            public AuthorizableType getAuthorizableType() {
                return new AuthorizableType(CoreGroupPermission.FORMVALUE, getEntityClass());
            }
        };
    }

    @Bean
    public AbstractFormValueService<AccAccountRoleAssignment, AccAccountRoleAssignmentFormValue> accountRoleAssignmentFormValueService(
            AbstractFormValueRepository<AccAccountRoleAssignment, AccAccountRoleAssignmentFormValue> repository) {

        return new AbstractFormValueService<>(repository) {
            @Override
            public AuthorizableType getAuthorizableType() {
                return new AuthorizableType(CoreGroupPermission.FORMVALUE, getEntityClass());
            }
        };
    }

}
