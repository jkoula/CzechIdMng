package eu.bcvsolutions.idm.core.eav.api.service;

import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.UniversalSearchDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * Abstract universal search type.
 *
 * @author Vít Švanda
 * @since 11.3.0
 */
public abstract class AbstractUniversalSearchType<DTO extends AbstractDto, F extends BaseFilter> implements
		UniversalSearchType<DTO, F>,
		BeanNameAware {

	private String beanName; // spring bean name - used as id

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

	protected abstract ReadDtoService<DTO, F> getService();

	@Override
	public long count(String text, BasePermission... permission) {
		if (!hasPermissions(permission)) {
			return 0;
		}
		F filter = createFilter(text);
		
		return getService().count(filter, permission);
	}
	
	@Override
	public Page<DTO> find(String text, Pageable pageable, BasePermission... permission) {
		if (!hasPermissions(permission)) {
			return new PageImpl<>(Lists.newArrayList(), pageable, 0);
		}
		F filter = createFilter(text);
		
		pageable = getPageable(pageable);
		
		return getService().find(filter, pageable, permission);
	}

	protected Pageable getPageable(Pageable pageable){
		return pageable;
	}

	protected boolean hasPermissions(BasePermission[] permission) {
		return true;
	}
	
	protected String getNiceLabel(AbstractDto dto) {
		if (dto instanceof Codeable) {
			Codeable codeable = (Codeable) dto;
			return codeable.getCode();
		}
		return dto.toString();
	}

	@Override
	public UniversalSearchDto dtoToUniversalSearchDto(AbstractDto dto) {
			UniversalSearchDto universalSearchDto = new UniversalSearchDto();
			universalSearchDto.setOwnerDto(dto);
			universalSearchDto.setOwnerId(dto.getId());
			universalSearchDto.setOwnerType(dto.getClass().getCanonicalName());
			universalSearchDto.setTrimmed(true);
			universalSearchDto.setNiceLabel(getNiceLabel(dto));
			return  universalSearchDto;
	}


	protected abstract F createFilter(String text);

	@Override
	public String getId() {
		return beanName;
	}
}
