package eu.bcvsolutions.idm.core.eav.api.dto.filter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;

/**
 * Form values filter.
 * 
 * @author Radek Tomiška
 * @param <O> value owner
 */
public class IdmFormValueFilter<O extends FormableEntity> extends DataFilter {

	public static final String PARAMETER_ATTRIBUTE_ID = "attributeId"; // list - OR
	public static final String PARAMETER_DEFINITION_ID = "definitionId";
	public static final String PARAMETER_OWNER = "owner";
	public static final String PARAMETER_PERSISTENT_TYPE = "persistentType";
	public static final String PARAMETER_STRING_VALUE = "stringValue"; // equals
	public static final String PARAMETER_SHORT_TEXT_VALUE = "shortTextValue"; // equals
	public static final String PARAMETER_STRING_VALUE_LIKE = "stringValueLike"; // like
	public static final String PARAMETER_SHORT_TEXT_VALUE_LIKE = "shortTextValueLike"; // like
	public static final String PARAMETER_BOOLEAN_VALUE = "booleanValue"; // equals
	public static final String PARAMETER_LONG_VALUE = "longValue"; // equals
	public static final String PARAMETER_DOUBLE_VALUE = "doubleValue"; // equals
	public static final String PARAMETER_DATE_VALUE = "dateValue"; // equals
	public static final String PARAMETER_DATE_VALUE_FROM = "dateValueFrom"; // => date value from
	public static final String PARAMETER_DATE_VALUE_TILL = "dateValueTill"; // => date value till
	public static final String PARAMETER_UUID_VALUE = "uuidValue"; // equals
	public static final String PARAMETER_ADD_OWNER_DTO = "addOwnerDto"; // convert owner into embedded dtos

	public IdmFormValueFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmFormValueFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmFormValueFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(IdmFormValueDto.class, data, parameterConverter);
	}

	public UUID getDefinitionId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_DEFINITION_ID);
	}

	public void setDefinitionId(UUID definitionId) {
		set(PARAMETER_DEFINITION_ID, definitionId);
	}

	public UUID getAttributeId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_ATTRIBUTE_ID);
	}

	public void setAttributeId(UUID attributeId) {
		set(PARAMETER_ATTRIBUTE_ID, attributeId);
	}
	
	/**
	 * Multiple attributes can be find - OR.
	 * 
	 * @return
	 * @since 10.3.0
	 */
	public List<UUID> getAttributeIds() {
		return getParameterConverter().toUuids(getData(), PARAMETER_ATTRIBUTE_ID);
	}
	
	/**
	 * Multiple attributes can be find - OR.
	 * 
	 * @param attributeIds
	 * @since 10.3.0
	 */
	public void setAttributeIds(List<UUID> attributeIds) {
		put(PARAMETER_ATTRIBUTE_ID, attributeIds);
	}

	@SuppressWarnings("unchecked")
	public O getOwner() {
		return (O) getData().getFirst(PARAMETER_OWNER);
	}

	public void setOwner(O owner) {
		set(PARAMETER_OWNER, owner);
	}

	public PersistentType getPersistentType() {
		return getParameterConverter().toEnum(getData(), PARAMETER_PERSISTENT_TYPE, PersistentType.class);
	}

	public void setPersistentType(PersistentType persistentType) {
		set(PARAMETER_PERSISTENT_TYPE, persistentType);
	}

	public String getStringValue() {
		return getParameterConverter().toString(getData(), PARAMETER_STRING_VALUE);
	}

	public void setStringValue(String stringValue) {
		set(PARAMETER_STRING_VALUE, stringValue);
	}

	public String getShortTextValue() {
		return getParameterConverter().toString(getData(), PARAMETER_SHORT_TEXT_VALUE);
	}

	public void setShortTextValue(String shortTextValue) {
		set(PARAMETER_SHORT_TEXT_VALUE, shortTextValue);
	}
	
	/**
	 * String value - like operator is used.
	 * 
	 * @return string value
	 * @since 11.2.0
	 */
	public String getStringValueLike() {
		return getParameterConverter().toString(getData(), PARAMETER_STRING_VALUE_LIKE);
	}

	/**
	 * String value - like operator is used.
	 * 
	 * @param stringValueLike string value
	 * @since 11.2.0
	 */
	public void setStringValueLike(String stringValueLike) {
		set(PARAMETER_STRING_VALUE_LIKE, stringValueLike);
	}

	/**
	 * Short text value - like operator is used.
	 * 
	 * @return short text value
	 * @since 11.2.0
	 */
	public String getShortTextValueLike() {
		return getParameterConverter().toString(getData(), PARAMETER_SHORT_TEXT_VALUE_LIKE);
	}

	/**
	 * Short text value - like operator is used.
	 * 
	 * @param shortTextValueLike value
	 * @since 11.2.0
	 */
	public void setShortTextValueLike(String shortTextValueLike) {
		set(PARAMETER_SHORT_TEXT_VALUE_LIKE, shortTextValueLike);
	}

	public Boolean getBooleanValue() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_BOOLEAN_VALUE);
	}

	public void setBooleanValue(Boolean booleanValue) {
		set(PARAMETER_BOOLEAN_VALUE, booleanValue);
	}

	public Long getLongValue() {
		return getParameterConverter().toLong(getData(), PARAMETER_LONG_VALUE);
	}

	public void setLongValue(Long longValue) {
		set(PARAMETER_LONG_VALUE, longValue);
	}

	public BigDecimal getDoubleValue() {
		return getParameterConverter().toBigDecimal(getData(), PARAMETER_DOUBLE_VALUE);
	}

	public void setDoubleValue(BigDecimal doubleValue) {
		set(PARAMETER_DOUBLE_VALUE, doubleValue);
	}

	public ZonedDateTime getDateValue() {
		return getParameterConverter().toDateTime(getData(), PARAMETER_DATE_VALUE);
	}

	public void setDateValue(ZonedDateTime dateValue) {
		set(PARAMETER_DATE_VALUE, dateValue);
	}
	
	/**
	 * Date value from.
	 * 
	 * @return date value
	 * @since 11.2.0
	 */
	public ZonedDateTime getDateValueFrom() {
		return getParameterConverter().toDateTime(getData(), PARAMETER_DATE_VALUE_FROM);
	}

	/**
	 * Date value from.
	 * 
	 * @param from date value
	 * @since 11.2.0
	 */
	public void setDateValueFrom(ZonedDateTime from) {
		set(PARAMETER_DATE_VALUE_FROM, from);
	}

	/**
	 * Date value till.
	 * 
	 * @return date value
	 * @since 11.2.0
	 */
	public ZonedDateTime getDateValueTill() {
		return getParameterConverter().toDateTime(getData(), PARAMETER_DATE_VALUE_TILL);
	}

	/**
	 * Date value till.
	 * 
	 * @param till date value
	 * @since 11.2.0
	 */
	public void setDateValueTill(ZonedDateTime till) {
		set(PARAMETER_DATE_VALUE_TILL, till);
	}

	public UUID getUuidValue() {
		return getParameterConverter().toUuid(getData(), PARAMETER_UUID_VALUE);
	}

	public void setUuidValue(UUID uuidValue) {
		set(PARAMETER_UUID_VALUE, uuidValue);
	}
	
	/**
	 * Convert owner into embedded dtos.
	 * 
	 * @return true - owner will be in embeded dtos. false by default.
	 * @since 11.2.0
	 */
	public boolean isAddOwnerDto() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_ADD_OWNER_DTO, false);
	}
	
	/**
	 * Convert owner into embedded dtos.
	 * 
	 * @param addOwnerDto true - owner will be in embeded dtos. false by default.
	 * @since 11.2.0
	 */
	public void setAddOwnerDto(boolean addOwnerDto) {
		set(PARAMETER_ADD_OWNER_DTO, addOwnerDto);
	}
}
