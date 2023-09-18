package eu.bcvsolutions.idm.doc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.bcvsolutions.idm.core.eav.repository.AbstractFormValueRepository;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormValueService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.doc.entity.DocDocument;
import eu.bcvsolutions.idm.doc.entity.eav.DocDocumentFormValue;

@Configuration
public class DocFormableConfiguration {

	@Bean
    public AbstractFormValueService<DocDocument, DocDocumentFormValue> documentFormValueService(
            AbstractFormValueRepository<DocDocument, DocDocumentFormValue> repository) {

        return new AbstractFormValueService<>(repository) {

            /**
             * Document form values supports authorization policies.
             */
            @Override
            public AuthorizableType getAuthorizableType() {
                return new AuthorizableType(CoreGroupPermission.FORMVALUE, getEntityClass());
            }
        };
    }
}
