package eu.bcvsolutions.idm.configuration.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.configuration.dto.ConfigurationDto;
import eu.bcvsolutions.idm.configuration.entity.IdmConfiguration;
import eu.bcvsolutions.idm.configuration.service.ConfigurationService;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.rest.BaseEntityController;;

/**
 * Configuration controller - add custom methods to configuration repository
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH)
public class ConfigurationController implements BaseEntityController<IdmConfiguration> {
	
	private final ConfigurationService configurationService;
	
	@Autowired
	public ConfigurationController(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
	
	/**
	 * Returns all public configuration properties 
	 * 
	 * @return
	 */
	@RequestMapping(path = "/public/configurations", method = RequestMethod.GET)
	public List<ConfigurationDto> getAllPublicConfigurations() {
		// TODO: resource wrapper + assembler
		return configurationService.getAllPublicConfigurations();
	}
	
	/**
	 * Returns configurations from property files 
	 * 
	 * @return
	 */
	@PostFilter("filterObject.name.startsWith('idm.pub.') or hasAuthority('" + IdmGroupPermission.CONFIGURATIONSECURED_READ + "')")
	@RequestMapping(path = "/configurations/file", method = RequestMethod.GET)
	public List<ConfigurationDto> getAllConfigurationsFromFiles() {
		// TODO: resource wrapper + assembler
		return configurationService.getAllConfigurationsFromFiles();
	}
	
	/**
	 * Returns configurations from property files 
	 * 
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.CONFIGURATIONSECURED_READ + "')")
	@RequestMapping(path = "/configurations/environment", method = RequestMethod.GET)
	public List<ConfigurationDto> getAllConfigurationsFroEnvironment() {
		// TODO: resource wrapper + assembler
		return configurationService.getAllConfigurationsFromEnvironment();
	}

}
