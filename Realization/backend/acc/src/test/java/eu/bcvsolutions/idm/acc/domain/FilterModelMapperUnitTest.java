package eu.bcvsolutions.idm.acc.domain;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.utils.ReflectionUtils;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;
import org.junit.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Component
public class FilterModelMapperUnitTest extends AbstractIntegrationTest {

    @Autowired
    ModelMapper modelMapper;

    @Test
    public void testBasicMapping() {
        IdmRequestIdentityRoleFilter f1 = new IdmRequestIdentityRoleFilter();


        final IdmRequestIdentityRoleFilter map = modelMapper.map(f1, IdmRequestIdentityRoleFilter.class);
    }

}
