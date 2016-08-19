package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

@Service
public class DefaultIdmIdentityService implements IdmIdentityService {
	public static String ADD_ROLE_TO_IDENTITY_WORKFLOW = "changeIdentityRoles";

	@Autowired
	private IdmIdentityRepository identityRepository;

	@Autowired
	private WorkflowProcessInstanceService workflowProcessInstanceService;
	
	@Override
	public ProcessInstance changePermissions(IdmIdentity identity){
		Map<String, Object> variables = new HashMap<>();
		variables.put(WorkflowProcessInstanceService.APPLICANT_IDENTIFIER, identity.getId());
		//check duplication
		//checkDuplicationWorkflow(identity, variables);
		return workflowProcessInstanceService.startProcess(ADD_ROLE_TO_IDENTITY_WORKFLOW, IdmIdentity.class.getSimpleName(), identity.getUsername(), identity.getId(), variables);	
	}

	@Override
	public IdmIdentity getByUsername(String username) {
		return identityRepository.findOneByUsername(username);
	}

	@Override
	public IdmIdentity get(Long id) {
		IdmIdentity entity = identityRepository.findOne(id);
		entity.getRoles();
		return entity;
	}
	
	/**
	 * Find all identities usernames by assigned role
	 * @param roleId
	 * @return String with all found usernames separate with comma 
	 */
	public String findAllByRole(Long roleId){
		List<IdmIdentity> identities =  identityRepository.findAllByRole(roleId);
		if(identities == null){
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for(IdmIdentity i : identities){
			sb.append(i.getUsername());
			sb.append(",");
		}
		return sb.toString();
	}

	@Override
	public String getNiceLabel(IdmIdentity identity) {
		if (identity == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		if (identity.getTitleBefore() != null) {
			sb.append(identity.getTitleBefore()).append(" ");
		}
		if (identity.getFirstName() != null) {
			sb.append(identity.getFirstName()).append(" ");
		}
		if (identity.getLastName() != null) {
			sb.append(identity.getLastName()).append(" ");
		}
		if (identity.getTitleAfter() != null) {
			sb.append(identity.getTitleAfter()).append(" ");
		}
		return sb.toString().trim();
	}

}
