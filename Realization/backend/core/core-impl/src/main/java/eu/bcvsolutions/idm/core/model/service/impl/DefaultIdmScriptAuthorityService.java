package eu.bcvsolutions.idm.core.model.service.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.ScriptAuthorityType;
import eu.bcvsolutions.idm.core.api.dto.AvailableMethodDto;
import eu.bcvsolutions.idm.core.api.dto.AvailableServiceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptAuthorityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.AvailableServiceFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptAuthorityFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmScriptAuthorityService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmScriptAuthority;
import eu.bcvsolutions.idm.core.model.entity.IdmScriptAuthority_;
import eu.bcvsolutions.idm.core.model.entity.IdmScript_;
import eu.bcvsolutions.idm.core.model.repository.IdmScriptAuthorityRepository;

/**
 * Default implementation for {@link IdmScriptAuthorityService}.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Ondrej Husnik 
 */
@Service("scriptAuthorityService")
public class DefaultIdmScriptAuthorityService 
		extends AbstractReadWriteDtoService<IdmScriptAuthorityDto, IdmScriptAuthority, IdmScriptAuthorityFilter> 
		implements IdmScriptAuthorityService {
	
	private final ApplicationContext applicationContext;
	private List<AvailableServiceDto> services;
	
	@Autowired
	public DefaultIdmScriptAuthorityService(
			IdmScriptAuthorityRepository repository,
			ApplicationContext applicationContext) {
		super(repository);
		//
		Assert.notNull(applicationContext, "Context is required.");
		//
		this.applicationContext = applicationContext;
	}
	
	@Override
	@Transactional
	public IdmScriptAuthorityDto saveInternal(IdmScriptAuthorityDto dto) {
		// check if class is accessible
		if (dto.getType() == ScriptAuthorityType.CLASS_NAME) {
			try {
				Class.forName(dto.getClassName());
			} catch (ClassNotFoundException e) {
				throw new ResultCodeException(
						CoreResultCode.GROOVY_SCRIPT_NOT_ACCESSIBLE_CLASS,
						ImmutableMap.of("class", dto.getClassName()), e);
			}
		} else {
			// check service name with available services
			if (!isServiceReachable(dto.getService(), dto.getClassName())) {
				throw new ResultCodeException(
						CoreResultCode.GROOVY_SCRIPT_NOT_ACCESSIBLE_SERVICE,
						ImmutableMap.of("service", dto.getService()));
			}
		}
		return super.saveInternal(dto);
	}

	@Override
	public void deleteAllByScript(UUID scriptId) {
		Assert.notNull(scriptId, "Script identifier is required.");
		//
		IdmScriptAuthorityFilter filter = new IdmScriptAuthorityFilter();
		filter.setScriptId(scriptId);
		// remove internal by id each script authority
		find(filter, null)
			.getContent()
			.forEach(scriptAuthority -> this.deleteInternalById(scriptAuthority.getId()));
	}

	@Override
	public List<AvailableServiceDto> findServices(String serviceName) {
		List<AvailableServiceDto> result = new ArrayList<>();
		// BaseDtoService, not all services implemented this
		if (this.services != null && !this.services.isEmpty()) {
			return this.services;
		}
		Map<String, ScriptEnabled> services = applicationContext.getBeansOfType(ScriptEnabled.class);
		//
		for (Entry<String, ScriptEnabled> entry : services.entrySet()) {
			if (serviceName == null || serviceName.isEmpty()) {
				result.add(new AvailableServiceDto(entry.getKey()));
			} else if (entry.getKey().matches(".*" + serviceName + ".*")) {
				result.add(new AvailableServiceDto(entry.getKey()));
			}
		}
		//
		Collections.sort(result, new Comparator<AvailableServiceDto>(){
		    public int compare(AvailableServiceDto o1, AvailableServiceDto o2) {
		        return o1.getServiceName().compareToIgnoreCase(o2.getServiceName());
		    }
		});
		//
		this.services = result;
		return this.services;
	}
	
	@Override
	public List<AvailableServiceDto> findServices(AvailableServiceFilter filter) {
		List<AvailableServiceDto> result = new ArrayList<>();
		Map<String, ScriptEnabled> services = applicationContext.getBeansOfType(ScriptEnabled.class);

		for (Entry<String, ScriptEnabled> entry : services.entrySet()) {
			Class<?> clazz = AutowireHelper.getTargetClass(entry.getValue());
			String className = clazz.getSimpleName();

			if (filter == null || StringUtils.isEmpty(filter.getText())
					|| className.toLowerCase().contains(filter.getText().toLowerCase())) {
				AvailableServiceDto dto = new AvailableServiceDto();
				dto.setId(entry.getKey());
				dto.setModule(EntityUtils.getModule(clazz));
				dto.setServiceName(className);
				dto.setPackageName(clazz.getCanonicalName());
				dto.setMethods(getServiceMethods(clazz));
				result.add(dto);
			}
		}
		return result;
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmScriptAuthority> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmScriptAuthorityFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		String text = filter.getText();
		if (StringUtils.isNotEmpty(text)) {
			text = text.toLowerCase();
			List<Predicate> textPredicates = new ArrayList<>(2);
			//
			textPredicates.add(builder.like(builder.lower(root.get(IdmScriptAuthority_.script).get(IdmScript_.code)), "%" + text + "%"));
			textPredicates.add(builder.like(builder.lower(root.get(IdmScriptAuthority_.script).get(IdmScript_.name)), "%" + text + "%"));
			//
			predicates.add(builder.or(textPredicates.toArray(new Predicate[textPredicates.size()])));
		}
		//
		UUID scriptId = filter.getScriptId();
		if (scriptId != null) {
			predicates.add(builder.equal(root.get(IdmScriptAuthority_.script).get(IdmScript_.id), scriptId));
		}
		//
		return predicates;
	}
	
	private List<AvailableMethodDto> getServiceMethods(Class<?> service) {	
		List<Method> omittedMethods = Lists.newArrayList(Object.class.getMethods());
		List<Method> methods = Lists.newArrayList(service.getMethods());
		methods.removeAll(omittedMethods);// methods form Object class are not required
		//
		List<AvailableMethodDto> methodDtos = new ArrayList<>(methods.size());
		for (Method method : methods) {
			if (!Modifier.isPublic(method.getModifiers())) {
				continue;
			}
			AvailableMethodDto dto = new AvailableMethodDto();
			dto.setMethodName(method.getName());
			dto.setReturnType(method.getReturnType());
			
			List<Class<?>> params = Arrays
					.asList(method.getParameters())
					.stream()
					.map((param) -> { return param.getType(); })
					.collect(Collectors.toList());
			dto.setArguments(params);
			methodDtos.add(dto);
		}
		methodDtos.sort((AvailableMethodDto d1, AvailableMethodDto d2) -> {return d1.getMethodName().compareTo(d2.getMethodName()); });
		return methodDtos;
	}
	

	@Override
	public boolean isServiceReachable(String serviceName, String className) {
		//
		return !this
				.findServices((String)null)
				.stream()
				.filter(service -> service.getServiceName().equals(serviceName))
				.collect(Collectors.toList()).isEmpty();
	}

}
