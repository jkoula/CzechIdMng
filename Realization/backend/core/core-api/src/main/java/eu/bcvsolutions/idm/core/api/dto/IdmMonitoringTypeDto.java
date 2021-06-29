package eu.bcvsolutions.idm.core.api.dto;

import java.util.List;

import org.springframework.hateoas.core.Relation;

import com.google.common.collect.Lists;


/**
 * Monitoring type DTO
 *
 * @author Vít Švanda
 * @since 10.4.0
 * @deprecated monitoring refactored from scratch in 11.2.0
 */
@Deprecated(since = "11.1.0")
@Relation(collectionRelation = "monitoringTypes")
public class IdmMonitoringTypeDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	//
	private String type;
	private List<IdmMonitoringResultDto> results;
	private String module;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<IdmMonitoringResultDto> getResults() {
		if (results == null) {
			results = Lists.newArrayList();
		}
		return results;
	}

	public void setResults(List<IdmMonitoringResultDto> results) {
		this.results = results;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}
}
