package eu.bcvsolutions.idm.core.api.dto;

import java.util.List;

import org.springframework.hateoas.core.Relation;

/**
 * DTO with information about available service that can be used for example in scripts.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Ondrej Husnik
 * @author Radek Tomiška
 */
@Relation(collectionRelation = "availableServices")
public class AvailableServiceDto extends AbstractComponentDto {

	private static final long serialVersionUID = 1L;
	private String serviceName;
	private String packageName;
	private String tableName;
	private String entityClass;
	private String dtoClass;
	private List<AvailableMethodDto> methods;
	
	public AvailableServiceDto() {
	}

	public AvailableServiceDto(String serviceName) {
		this.serviceName = serviceName;
	}
	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	} 
	
	public List<AvailableMethodDto> getMethods() {
		return this.methods;
	}
	
	public void setMethods(List<AvailableMethodDto> methods) {
		this.methods = methods;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	/**
	 * Controlled database table.
	 * 
	 * @return table name
	 * @since 11.1.0
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Controlled database table.
	 * 
	 * @param tableName table name
	 * @since 11.1.0
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Controlled entity class.
	 * 
	 * @return canonical name
	 * @since 11.1.0
	 */
	public String getEntityClass() {
		return entityClass;
	}

	/**
	 * Controlled entity class.
	 * 
	 * @param entityClass canonical name
	 * @since 11.1.0
	 */
	public void setEntityClass(String entityClass) {
		this.entityClass = entityClass;
	}

	/**
	 * Controlled dto class.
	 * 
	 * @return canonical name
	 * @since 11.1.0
	 */
	public String getDtoClass() {
		return dtoClass;
	}

	/**
	 * Controlled dto class.
	 * 
	 * @param dtoClass canonical name
	 * @since 11.1.0
	 */
	public void setDtoClass(String dtoClass) {
		this.dtoClass = dtoClass;
	}
}
