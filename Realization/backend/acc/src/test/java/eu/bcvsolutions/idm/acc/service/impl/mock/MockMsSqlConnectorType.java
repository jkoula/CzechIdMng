package eu.bcvsolutions.idm.acc.service.impl.mock;

import eu.bcvsolutions.idm.acc.connector.MsSqlConnectorType;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;
import java.util.List;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Mock AD wizard for MSSQL. Only for using in tests. (We do not have MS SQL driver in test environment).
 *
 * @author Vít Švanda
 * @since 11.2.0
 */
@Component(MockMsSqlConnectorType.NAME)
public class MockMsSqlConnectorType extends MsSqlConnectorType {
	
	// Connector type ID.
	public static final String NAME = "mock-ms-sql-connector-type";
	private MockSysSystemService systemService;
	@Autowired
	private SysSchemaObjectClassService schemaObjectClassService;

	@Override
	public boolean supports() {
		return true;
	}

	@Override
	protected SysSystemService getSystemService() {
		return initMockSysSystemService();
	}

	private MockSysSystemService initMockSysSystemService(){
		if (systemService == null) {
			systemService = AutowireHelper.createBean(MockSysSystemService.class);
			systemService.setGetSchemaCallBack(new GetSchemaCallBack() {
				@Override
				public List<SysSchemaObjectClassDto> call(SysSystemDto systemDto) {
					if (systemDto != null) {
						SysSchemaObjectClassFilter schemaFilter = new SysSchemaObjectClassFilter();
						schemaFilter.setSystemId(systemDto.getId());

						List<SysSchemaObjectClassDto> content = schemaObjectClassService.find(schemaFilter, null).getContent();
						if (content.size() > 0) {
							return content;
						}
						
						SysSchemaObjectClassDto schemaObjectClassDto = new SysSchemaObjectClassDto();
						schemaObjectClassDto.setObjectClassName(IcObjectClassInfo.ACCOUNT);
						schemaObjectClassDto.setSystem(systemDto.getId());
						schemaObjectClassDto = schemaObjectClassService.save(schemaObjectClassDto);
						
						return Lists.newArrayList(schemaObjectClassDto);
					}
					return null;
				}
			});
		}
		return systemService;
	}

	@Override
	public String getConnectorName() {
		return super.getConnectorName();
	}

	public interface GetSchemaCallBack {
		List<SysSchemaObjectClassDto> call(SysSystemDto systemDto);
	}

	@Override
	public int getOrder() {
		return 172;
	}
}
