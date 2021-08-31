package eu.bcvsolutions.idm.acc.service.impl.mock;

import com.google.common.collect.Sets;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.service.impl.CrossDomainAdUserConnectorTypeTest;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Mock AD wizard for users and Cross domains. Only for using in tests. (We do not have MS AD in test environment).
 *
 * @author Vít Švanda
 * @since 11.2.0
 */
@Component(MockCrossDomainAdUserConnectorType.NAME)
public class MockCrossDomainAdUserConnectorType extends MockAdUserConnectorType {
	
	// Connector type ID.
	public static final String NAME = "mock-cross-domain-ad-connector-type";
	private CrossDomainAdUserConnectorTypeTest.GetConnectorObjectCallback readConnectorObjectCallBack;

	@Override
	public IcConnectorObject readConnectorObject(SysSystemDto system, String uid, IcObjectClass objectClass) {
		if (this.readConnectorObjectCallBack != null) {
			return this.readConnectorObjectCallBack.call(system, uid, objectClass);
		}
		return null;
	}

	@Override
	protected Set<String> searchGroups(String memberAttribute, IcConnectorConfiguration icConfig, IcConnectorInstance connectorInstance, String dn) {
		return Sets.newHashSet("EXTERNAL_ONE");
	}

	@Override
	protected boolean supportsSystemByConnector(SysSystemDto systemDto) {
		return systemDto.getName().startsWith(NAME);
	}

	@Override
	public int getOrder() {
		return 10;
	}

	public void setReadConnectorObjectCallBack(CrossDomainAdUserConnectorTypeTest.GetConnectorObjectCallback callback) {
		this.readConnectorObjectCallBack = callback;
	}
}
