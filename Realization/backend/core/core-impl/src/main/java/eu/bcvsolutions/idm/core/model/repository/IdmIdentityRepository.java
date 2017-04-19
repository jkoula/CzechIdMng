package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.rest.projection.IdmIdentityExcerpt;

/**
 * Repository for identities
 * 
 * @author Radek Tomiška 
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "identities", //
		path = "identities", //
		itemResourceRel = "identity", //
		excerptProjection = IdmIdentityExcerpt.class,
		exported = false // we are using repository metadata, but we want expose rest endpoint manually
	)
public interface IdmIdentityRepository extends AbstractEntityRepository<IdmIdentity, IdentityFilter> {

	IdmIdentity findOneByUsername(@Param("username") String username);

	/**
	 * @deprecated use criteria api
	 */
	@Override
	@Deprecated
	@Query(value = "select e from IdmIdentity e"
	        + " where"
			+ " ("
	        	// naive "fulltext"
				+ " lower(e.username) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
		        + " or lower(e.firstName) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
		        + " or lower(e.lastName) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
		        + " or lower(e.email) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
		        + " or lower(e.description) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
	        + " )"
	        + " and"
	        + " ("
		        + " (?#{[0].subordinatesFor} is null and ?#{[0].subordinatesByTreeType} is null)"
		        // manager as guarantee
		        + " or ((?#{[0].subordinatesByTreeType} is null) and exists(from IdmIdentityContract ic where ic.identity = e and ic.guarantee = ?#{[0].subordinatesFor}))"
		        // manager from tree structure - only direct subordinate are supported now
		        + " or exists(from IdmIdentityContract ic where ic.identity = e and ic.workPosition.parent IN (select vic.workPosition from IdmIdentityContract vic where vic.identity = ?#{[0].subordinatesFor} and (?#{[0].subordinatesByTreeType} is null or vic.workPosition.treeType = ?#{[0].subordinatesByTreeType}) ))"
	        + " )"
	        + " and"
	        + "	("
		        + " (?#{[0].managersFor} is null and ?#{[0].managersByTreeType} is null)"
		        // manager as guarantee
	        	+ " or ((?#{[0].managersByTreeType} is null) and exists(from IdmIdentityContract ic where ic.identity = ?#{[0].managersFor} and e = ic.guarantee))"
	        	// manager from tree structure - only direct managers are supported now
	        	+ " or exists(from IdmIdentityContract ic where ic.identity = e and ic.workPosition IN (select vic.workPosition.parent from IdmIdentityContract vic where (?#{[0].managersFor} is null or vic.identity = ?#{[0].managersFor}) and (?#{[0].managersByTreeType} is null or vic.workPosition.treeType = ?#{[0].managersByTreeType}) ))"
	        + " )"
	        + " and"
	        + " ("
	        	+ " ?#{[0].managersByTreeNode} is null"
	        	// managers by tree node (working position)
	        	+ " or exists(from IdmIdentityContract ic where ic.identity = e and ic.workPosition IN (select vic.workPosition.parent from IdmIdentityContract vic where vic.workPosition = ?#{[0].managersByTreeNode} ))"
	        + " )"
	        + " and "
	        + " ("
	        	// identity with any of given role (OR)
	        	+ " ?#{[0].roles == null ? 0 : [0].roles.size()} = 0"
	        	+ " or exists (from IdmIdentityRole ir where ir.identityContract.identity = e and ir.role.id IN (?#{T(eu.bcvsolutions.idm.core.api.utils.RepositoryUtils).queryEntityIds([0].roles)}))"
	        + " )"
	  	    + " and "
	  	    + " ("
	  	    	+ " ?#{[0].property} is null "
	  	    		+ "or (?#{[0].property} = 'username' and e.username = ?#{[0].value}) "
	  	    		+ "or (?#{[0].property} = 'firstName' and e.firstName = ?#{[0].value}) "
	  	    		+ "or (?#{[0].property} = 'lastName' and e.lastName = ?#{[0].value}) "
	  	    		+ "or (?#{[0].property} = 'email' and e.email = ?#{[0].value}) "
	        + " )"
	        	// identities on selected structure recursively
	        + " and"
	        + "	("
	        	+ " ?#{[0].treeNode} is null or ?#{[0].recursively == true ? 'true' : 'false'} = 'false' or exists(from IdmIdentityContract ic where ic.identity = e and ic.workPosition.forestIndex.lft BETWEEN ?#{[0].treeNode == null ? null : [0].treeNode.lft} and ?#{[0].treeNode == null ? null : [0].treeNode.rgt})"
	        + "	)"
	        	// identities on selected structure only
	        + " and"
	        + "	("
	        	+ " ?#{[0].treeNode} is null or ?#{[0].recursively == false ? 'true' : 'false'} = 'false' or exists(from IdmIdentityContract ic where ic.identity = e and ic.workPosition = ?#{[0].treeNode})"
	        + "	)"
	        	// identities related to tree type structure
	        + " and"
	        + "	("
	        	+ " ?#{[0].treeTypeId} is null or exists(from IdmIdentityContract ic where ic.identity = e and ic.workPosition.treeType.id = ?#{[0].treeTypeId})"
	        + "	)")
	Page<IdmIdentity> find(IdentityFilter filter, Pageable pageable);
	
	@Transactional(timeout = 5, readOnly = true)
	@Query(value = "SELECT e FROM IdmIdentity e"
			+ " JOIN e.contracts contracts"
			+ " JOIN contracts.roles roles"
			+ " WHERE"
	        + " roles.role = :role")
	List<IdmIdentity> findAllByRole(@Param(value = "role") IdmRole role);
}
