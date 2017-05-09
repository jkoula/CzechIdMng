package eu.bcvsolutions.idm.core.model.service.impl;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.filter.AuditFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;
import eu.bcvsolutions.idm.core.model.repository.IdmAuditRepository;
import eu.bcvsolutions.idm.core.model.repository.listener.IdmAuditListener;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuditService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Implementation of service for auditing
 * 
 * @see {@link IdmAuditListener}
 * @see {@link IdmAudit}
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service
public class DefaultAuditService extends AbstractReadWriteEntityService<IdmAudit, AuditFilter> implements IdmAuditService {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private IdmAuditRepository auditRepository;
	
	@LazyCollection(LazyCollectionOption.TRUE)
	private List<String> allAuditedEntititesNames;
	
	@Autowired
	public DefaultAuditService(IdmAuditRepository auditRepository) {
		super(auditRepository);
	}
	
	@Override
	public <T> T findRevision(Class<T> classType, UUID entityId, Long revisionNumber) throws RevisionDoesNotExistException  {
		return this.find(classType, entityId, revisionNumber);
	}
	
	private AuditReader getAuditReader() {
		return AuditReaderFactory.get(entityManager);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getPreviousVersion(T entity, Long currentRevId) {
	    IdmAudit previousRevision = this.getPreviousRevision(currentRevId);

	    if (previousRevision != null) {
	        return (T) this.find(entity.getClass(), (UUID)((BaseEntity) entity).getId(), Long.valueOf(previousRevision.getId().toString()));
	    } else {
	        return null;
	    }
	}
	
	@Override
	public <T> List<IdmAudit> findRevisions(Class<T> classType, UUID entityId) {
		AuditFilter filter = new AuditFilter();
		filter.setEntityId(entityId);
		filter.setType(classType.getName());
		Pageable page = new PageRequest(0, Integer.MAX_VALUE, new Sort("timestamp"));
		Page<IdmAudit> result = this.find(filter, page);
		return result.getContent();
	}
	
	@Override
	public <T> T getPreviousVersion(Class<T> entityClass, UUID entityId, Long currentRevisionId) {
	    IdmAudit previousRevision = this.getPreviousRevision(currentRevisionId);

	    if (previousRevision != null) {
	        return this.find(entityClass, entityId, Long.valueOf(previousRevision.getId().toString()));
	    } else {
	    	return this.find(entityClass, entityId, currentRevisionId);
	    }
	}
	
	@Override
	public Object getPreviousVersion(Long currentRevisionId) {
		IdmAudit revision = this.get(currentRevisionId);
		
		Object result = null;

	    IdmAudit previousRevision;
		try {
			previousRevision = this.getPreviousRevision(currentRevisionId);
			
			if (previousRevision != null) {
				result = this.find(Class.forName(previousRevision.getType()), previousRevision.getEntityId(), Long.valueOf(previousRevision.getId().toString()));
		    }
		} catch (ClassNotFoundException e) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("class", revision.getType()), e);
		}
		return result;
	}
	
	@Override
	public <T> T getVersion(Class<T> entityClass, UUID entityId, Long currentRevId) {
		return this.find(entityClass, entityId, currentRevId);
	}

	@Override
	public <T> List<String> getNameChangedColumns(Class<T> entityClass, UUID entityId, Long currentRevId,
			T currentEntity) {
		List<String> changedColumns = new ArrayList<>();
		
		T previousEntity = null;
		
		if (currentRevId == null) {
			// this.getLastRevisionNumber(entityClass, entityId).longValue();
			currentRevId = Long.valueOf((this.getAuditReader().getCurrentRevision(IdmAudit.class, true)).getId().toString());
		}
		previousEntity = this.getPreviousVersion(entityClass, entityId, currentRevId);
		
		if (previousEntity == null) {
			return changedColumns;
		}
		
		Field[] fields = entityClass.getDeclaredFields();
		
		for (Field field : fields) {
			if (field.getAnnotation(Audited.class) != null) {
				Object previousValue;
				Object currentValue;
				try {
					PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(currentEntity, field.getName());
					Assert.notNull(propertyDescriptor, MessageFormat.format("DefaultAuditService: read method for audited field {0}, can't be null.", field.getName()));
					//
					Method readMethod = propertyDescriptor.getReadMethod();
					// check if exists readMethod
					Assert.notNull(readMethod, MessageFormat.format("DefaultAuditService: read method for audited field {0}, can't be null.", field.getName()));
					
					previousValue = readMethod.invoke(previousEntity);
					currentValue = readMethod.invoke(currentEntity);
					
					if (previousValue == null && currentValue == null) {
						continue;
					}
					
					if (previousValue == null || !previousValue.equals(currentValue)) {
						changedColumns.add(field.getName());
					}
				} catch (IllegalArgumentException | IllegalAccessException | 
						NoSuchMethodException | InvocationTargetException ex) {
					throw new IllegalArgumentException(
							MessageFormat.format("For entity class [{0}] with id [{1}] and revision id [{2}], name of changed columns cannot be found.",
									entityClass, entityId, currentRevId), ex);
				}
			}
		}
		return changedColumns;
	}

	@Override
	public Page<IdmAudit> getRevisionsForEntity(String entityClass, UUID entityId, Pageable pageable) {
		AuditFilter filter = new AuditFilter();
		filter.setType(entityClass);
		filter.setEntityId(entityId);
		return this.find(filter, pageable);
	}

	@Override
	public List<String> getAllAuditedEntitiesNames() {
		// load from cache
		if (this.allAuditedEntititesNames != null) {
			return this.allAuditedEntititesNames;
		}
		//
		List<String> result = new ArrayList<>();
		Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();
		for (EntityType<?> entityType : entities) {
			if (entityType.getJavaType() == null) {
				continue;
			}
			// get entities methods and search annotation Audited.
			for (Field field : entityType.getJavaType().getDeclaredFields()) {
				if (field.getAnnotation(Audited.class) != null) {
					result.add(entityType.getJavaType().getSimpleName());
					break;
				}
			}
		}
		// sort entities by name
		Collections.sort(result);
		//
		this.allAuditedEntititesNames = result;
		return result;
	}

	@Override
	public <T> Number getLastRevisionNumber(Class<T> entityClass, UUID entityId) {
		return (Number) this.getAuditReader().createQuery()
			    .forRevisionsOfEntity(entityClass, false, true)
			    .addProjection(AuditEntity.revisionNumber().max())
			    .add(AuditEntity.id().eq(entityId))
			    .getSingleResult();
	}
	
	/**
	 * Is necessary to override method get, because old get transform id to UUID and audits have Long ID.
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public IdmAudit get(Serializable id, BasePermission... permission) {
		Assert.notNull(id, "Id is required");
		AuditFilter filter = new AuditFilter();
		filter.setId(Long.valueOf(id.toString()));
		List<IdmAudit> audits = this.find(filter, null).getContent();
		
		// number founds audits must be exactly 1
		if (audits.isEmpty() || audits.size() != 1) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("audit", id));
		}
		// return only one element
		return audits.get(0);
	}
	
	@Override
	public Map<String, Object> getDiffBetweenVersion(String clazz, Long firstRevId, Long secondRevId) {
		Map<String, Object> result = new HashMap<>();
		IdmAudit firstRevision = this.get(firstRevId);
		// first revision must exist
		if (firstRevision == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("audit", firstRevId));
		}
		
		if (clazz == null) {
			clazz = firstRevision.getType();
		}
		
		IdmAudit secondRevision = null;
		
		if (secondRevId == null) {
			try {
				secondRevision = (IdmAudit)this.getPreviousVersion(Class.forName(clazz), firstRevision.getEntityId(), Long.valueOf(firstRevision.getId().toString()));
			} catch (NumberFormatException e) {
				throw new ResultCodeException(CoreResultCode.BAD_VALUE, ImmutableMap.of("audit", firstRevision.getId()), e);
			} catch (ClassNotFoundException e) {
				throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("audit class", clazz), e);
			}
		} else {
			secondRevision = this.get(secondRevId);
		}
		// check if we have second revision
		if (secondRevision == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("audit", secondRevId));
		}
		
		// check if revision are from same type (class, entity id)
		if (!firstRevision.getEntityId().equals(secondRevision.getEntityId())) {
			throw new ResultCodeException(CoreResultCode.AUDIT_REVISION_NOT_SAME, ImmutableMap.of("revision", clazz));
		}
		
		// now we will receive version from audit tables
		try {
			Object firstVersion = this.getRevision(clazz, firstRevision.getEntityId(), firstRevId);
			Object secondVersion = this.getRevision(clazz, secondRevision.getEntityId(), secondRevId);
			
			List<String> auditedClass = this.getAllAuditedEntitiesNames();
			
			Map<String, Object> firstValues = null;
			Map<String, Object> secondValues = null;
			
			// first revision is DEL, all attributes are null
			if (firstRevision.getModification().equals(RevisionType.DEL.name())) {
				firstValues = Collections.emptyMap();
			} else {
				firstValues = this.getValuesFromVersion(firstVersion, auditedClass);
			}
			
			if (secondRevision.getModification().equals(RevisionType.DEL.name())) {
				secondValues = Collections.emptyMap();
			} else {
				secondValues = this.getValuesFromVersion(secondVersion, auditedClass);
			}
			
			Set<Entry<String, Object>> entries = firstValues.entrySet();
			
			if (entries.isEmpty()) {
				entries = secondValues.entrySet();
			}
			
			for (Entry<String, Object> entry : entries) {
				if (!Objects.equals(entry.getValue(), secondValues.get(entry.getKey()))) {
					result.put(entry.getKey(), secondValues.get(entry.getKey()));
				}
			}
			
		} catch (ClassNotFoundException e) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("audit class", clazz), e);
		}
		
		return result;
	}
	
	public Map<String, Object> getDiffBetweenVersion(Long firstRevId, Long secondRevId) {
		return this.getDiffBetweenVersion(null, firstRevId, secondRevId);
	}
	
	@Override
	public Map<String, Object> getValuesFromVersion(Object revisionObject) {
		return this.getValuesFromVersion(revisionObject, this.getAllAuditedEntitiesNames());
	}
	
	@Override
	public Map<String, Object> getValuesFromVersion(Object revisionObject, List<String> auditedClass) {
		Map<String, Object> revisionValues = new HashMap<>();
		if (revisionObject == null) {
			return Collections.emptyMap();
		}
		
		// for better debug and readable there is no stream
		// we cannot use Introspector.getBeanInfo
		
		// getAllFieldsList get all field also with field from superclass
		List<Field> fields = FieldUtils.getAllFieldsList(revisionObject.getClass());
		
		for (Field field : fields) {
			try {
				
				// check if field has Audited annotation, or class
				if (!field.isAnnotationPresent(Audited.class) && !field.getDeclaringClass().isAnnotationPresent(Audited.class)) {
					continue;
				}
				//
				PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(revisionObject, field.getName());
				//
				// get property descriptor for readMethod
				if (propertyDescriptor == null) {
					continue;
				}
				//
				Method readMethod = propertyDescriptor.getReadMethod();
				Object value = readMethod.invoke(revisionObject);
				//
				// value can be null, but we want it
				if (value == null) {
					revisionValues.put(field.getName(), value);
					continue;
				}
				//
				LazyInitializer hibernateLI = null;
				String className = null;
				if (value instanceof HibernateProxy) {
                    HibernateProxy proxy = (HibernateProxy) value;
                    hibernateLI = proxy.getHibernateLazyInitializer();
                    className = hibernateLI.getEntityName();
                }
				// we have all audited class, then some not audited class (binding) and others primitive types
				if (className != null) {
					// get id from hibernate lazy initializer, entity may no longer exist, but ID in DB is always
					revisionValues.put(field.getName(), hibernateLI.getIdentifier());
				} else {
					revisionValues.put(field.getName(), value);
				}
				//
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new ResultCodeException(CoreResultCode.BAD_REQUEST, ImmutableMap.of("field", field.getName()), e);
			}
		}
		
		return revisionValues;
	}
	
	@Override
	public IdmAudit getPreviousRevision(IdmAudit revision) {
		Assert.notNull(revision, MessageFormat.format("DefaultAuditService: method getPreviousRevision - current revision [{0}] can't be null.", revision));
		
		if (revision.getEntityId() == null) {
			return null;
		}
		
		List<IdmAudit> results = this.auditRepository.getPreviousVersion(revision.getEntityId(), Long.valueOf(revision.getId().toString()), new PageRequest(0, 1)).getContent();
		if (!results.isEmpty() && results.size() == 1) {
			return results.get(0);
		} else {
			return null;
		}
	}
	
	@Override
	public IdmAudit getPreviousRevision(Long revisionId) {
		Assert.notNull(revisionId, MessageFormat.format("DefaultAuditService: method getPreviousRevision - current revision id [{0}] can't be null.", revisionId));
		return this.getPreviousRevision(this.get(revisionId));
	}
	
	/**
	 * Method get version for @param revisionId
	 * 
	 * @param clazz
	 * @param entityId
	 * @param revisionId
	 * @return
	 * @throws ClassNotFoundException
	 */
	private Object getRevision(String clazz, UUID entityId, Long revisionId) throws ClassNotFoundException {
		return this.find(Class.forName(clazz), entityId, revisionId);
	}
	
	/**
	 * Method working with envers, find is realized in audited tables
	 * 
	 * @param entityClass
	 * @param entityId
	 * @param revisionId
	 * @return
	 */
	private <T> T find(Class<T> entityClass, UUID entityId, Long revisionId) {
		AuditReader reader = this.getAuditReader();

		return reader.find(entityClass, entityId, revisionId);
	}
}
