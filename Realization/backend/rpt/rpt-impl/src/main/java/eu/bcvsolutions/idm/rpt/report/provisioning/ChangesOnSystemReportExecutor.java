package eu.bcvsolutions.idm.rpt.report.provisioning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperation;
import eu.bcvsolutions.idm.acc.domain.SysValueChangeType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysAttributeDifferenceDto;
import eu.bcvsolutions.idm.acc.dto.SysAttributeDifferenceValueDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.eav.domain.AccFaceType;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping_;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.impl.IcConnectorObjectImpl;
import eu.bcvsolutions.idm.rpt.api.domain.RptResultCode;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.exception.ReportGenerateException;
import eu.bcvsolutions.idm.rpt.api.exception.ReportRenderException;
import eu.bcvsolutions.idm.rpt.api.executor.AbstractReportExecutor;
import eu.bcvsolutions.idm.rpt.dto.RptChangesOnSystemRecordDto;
import eu.bcvsolutions.idm.rpt.dto.RptChangesOnSystemState;

/**
 * Report for comparison values in IdM and system.
 *
 * @author Ondrej Husnik
 * @since 12.0.0
 */
@Component(ChangesOnSystemReportExecutor.REPORT_NAME)
@Description("Compare values in IdM with values in system")
public class ChangesOnSystemReportExecutor extends AbstractReportExecutor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ChangesOnSystemReportExecutor.class);

	public static final String REPORT_NAME = "changes-on-system-report";
	public static final String PARAMETER_SYSTEM = "system";
	public static final String PARAMETER_MAPPING_ATTRIBUTES = "mappingAttributes";
	public static final String PARAMETER_SYSTEM_MAPPING = "systemMapping";
	public static final String PARAMETER_ONLY_IDENTITY = "identities";
	public static final String PARAMETER_TREE_NODE = "treeNode";
	public static final String PARAMETER_SKIP_UNCHANGED_VALUES = "skipUnchangedValues";
	// output JSON file keys
	public static final String ATTRIBUTE_NAME_JSON_KEY = "selectedAttributeNames";
	public static final String RECORDS_JSON_KEY = "records";
	//
	@Autowired private SysSystemService systemService;
	@Autowired private IdmIdentityService identityService;
	@Autowired private AccAccountService accountService;
	@Autowired private SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired private ProvisioningService provisioningService;
	@Autowired private SysSystemMappingService systemMappingService;
	@Autowired private AccIdentityAccountService identityAccountService;
	@Autowired private SysProvisioningArchiveService provisioningArchiveService;

	/**
	 * Report ~ executor name
	 */
	@Override
	public String getName() {
		return REPORT_NAME;
	}

	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto mappingSelect = new IdmFormAttributeDto(PARAMETER_MAPPING_ATTRIBUTES, "MappingAttributes",
				PersistentType.TEXT);
		mappingSelect.setFaceType(AccFaceType.SYSTEM_MAPPING_ATTRIBUTE_FILTERED_SELECT);
		//
		IdmFormAttributeDto skipUnchanged = new IdmFormAttributeDto(PARAMETER_SKIP_UNCHANGED_VALUES, "Skip unchanged values",
				PersistentType.BOOLEAN);
		//		
		IdmFormAttributeDto identities = new IdmFormAttributeDto(PARAMETER_ONLY_IDENTITY, "Identities",
				PersistentType.UUID);
		identities.setFaceType(BaseFaceType.IDENTITY_SELECT);
		identities.setMultiple(true);
		//
		IdmFormAttributeDto treeNode = new IdmFormAttributeDto(PARAMETER_TREE_NODE, "Treenode", PersistentType.UUID);
		treeNode.setFaceType(BaseFaceType.TREE_NODE_SELECT);
		//
		return Lists.newArrayList(mappingSelect, skipUnchanged, identities, treeNode);
	}

	@Override
	protected IdmAttachmentDto generateData(RptReportDto report) {
		// get system related configuration
		MultiValueMap<String, UUID> configuration = parseAttributeConfig(report);
		SysSystemDto systemDto = getSystemById(configuration.getFirst(PARAMETER_SYSTEM));
		SysSystemMappingDto systemMapping = getSystemMappingById(configuration.getFirst(PARAMETER_SYSTEM_MAPPING));
		List<SysSystemAttributeMappingDto> attributes = getAttributesById(configuration.get(PARAMETER_MAPPING_ATTRIBUTES), systemMapping);
		List<String> selectedAttributeNames = getSelectedAttributeNames(attributes)
				.stream()
				.sorted()
				.collect(Collectors.toList());
		// list of identities to report
		Set<UUID> identities = getReportedIdentities(report);
		boolean skipUnchangedMultivalue = getSkipUnchangedValues(report);		
		AccAccountFilter filterAccount = new AccAccountFilter();
		filterAccount.setSystemId(systemDto.getId());
		filterAccount.setEntityType(SystemEntityType.IDENTITY);

		File temp = getAttachmentManager().createTempFile();
		try (FileOutputStream outputStream = new FileOutputStream(temp)) {
			JsonGenerator jGenerator = getMapper().getFactory().createGenerator(outputStream, JsonEncoding.UTF8);
			try {
				// start of root object
				jGenerator.writeStartObject();
				// write attribute names
				jGenerator.writeFieldName(ATTRIBUTE_NAME_JSON_KEY);
				getMapper().writeValue(jGenerator, selectedAttributeNames);
				// create and write records
				jGenerator.writeFieldName(RECORDS_JSON_KEY);
				jGenerator.writeStartArray();
				createReportData(jGenerator, filterAccount, identities, systemDto.getId(), selectedAttributeNames, skipUnchangedMultivalue);
				jGenerator.writeEndArray();
				// end of root object
				jGenerator.writeEndObject();
			} finally {
				jGenerator.close();
			}

			return createAttachment(report, new FileInputStream(temp));
		} catch (IOException e) {
			throw new ReportGenerateException(report.getName(), e);
		} finally {
			FileUtils.deleteQuietly(temp);
		}
	}

	/**
	 * Create key for given account and identity
	 *
	 * @param account
	 * @param identity
	 * @return
	 */
	private String createKey(AccAccountDto account, IdmIdentityDto identity) {
		StringBuilder result = new StringBuilder();
		if (identity != null) {
			result.append(identity.getUsername());
		}
		if (account != null) {
			result.append(" (");
			result.append(account != null ? account.getUid() : "");
			result.append(')');
		}
		return result.toString();
	}

	/**
	 * Get system from report configuration
	 *
	 * @param report
	 * @return
	 */
	private SysSystemDto getSystemById(UUID systemId) {
		SysSystemDto dto = systemService.get(systemId);
		if (dto == null) {
			throw new ResultCodeException(RptResultCode.REPORT_WRONG_CONFIGURATION, ImmutableMap.of("attribute", Objects.toString(systemId)));
		}
		return dto;
	}

	/**
	 * Get system mapping from report configuration
	 *
	 * @param report
	 * @return
	 */
	private SysSystemMappingDto getSystemMappingById(UUID mappingId) {
		SysSystemMappingDto dto = systemMappingService.get(mappingId);
		if (dto == null) {
			throw new ResultCodeException(RptResultCode.REPORT_WRONG_CONFIGURATION, ImmutableMap.of("attribute", Objects.toString(mappingId)));
		}
		return dto;
	}

	/**
	 * Get defined attributes for report
	 *
	 * @param list of attribute Ids
	 * @return
	 */
	private List<SysSystemAttributeMappingDto> getAttributesById(List<UUID> attributeIds, SysSystemMappingDto mapping) {
		if (CollectionUtils.isEmpty(attributeIds)) {
			SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
			filter.setSystemMappingId(mapping.getId());
			filter.setDisabledAttribute(Boolean.FALSE);
			//
			return systemAttributeMappingService.find(filter, null).getContent();
		}
		//
		return attributeIds
				.stream()
				.map(systemAttributeMappingService::get)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	/**
	 * Get selected identities only
	 *
	 * @param report
	 * @return
	 */
	private Set<UUID> getSelectedIdentities(RptReportDto report) {
		IdmFormInstanceDto formInstance = new IdmFormInstanceDto(report, getFormDefinition(), report.getFilter());
		List<Serializable> identitiesAsSerializable = formInstance.toPersistentValues(PARAMETER_ONLY_IDENTITY);
		if (CollectionUtils.isEmpty(identitiesAsSerializable)) {
			return new HashSet<UUID>();
		}
		//
		return identitiesAsSerializable
			.stream()
			.filter(Objects::nonNull)
			.map(DtoUtils::toUuid)
			.collect(Collectors.toSet());
	}
	
	/**
	 * Get tree node from the report configuration.
	 * @param report
	 * @return
	 */
	private UUID getTreeNode(RptReportDto report) {
		IdmFormInstanceDto formInstance = new IdmFormInstanceDto(report, getFormDefinition(), report.getFilter());
		Serializable treeNodeIdAsSerializable = formInstance.toSinglePersistentValue(PARAMETER_TREE_NODE);
		//
		return DtoUtils.toUuid(treeNodeIdAsSerializable);
	}
	

	/**
	 * Returns the union of identities selected either by tree node or directly.
	 * 
	 * Returns tri-state value - Non-empty set returns selected identities - Empty
	 * set indicates that identities were specified but none were found - Null value
	 * indicates no identity specification
	 * 
	 * @param report
	 * @return
	 */
	private Set<UUID> getReportedIdentities(RptReportDto report) {
		Set<UUID> identities = getSelectedIdentities(report);
		UUID treeNode = getTreeNode(report);

		if (treeNode == null) {
			if (identities.isEmpty()) {
				return null;
			}
			return identities;
		}

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setTreeNode(treeNode);
		identityFilter.setRecursively(true);
		identities.addAll(identityService.findIds(identityFilter, null).getContent());

		return identities;
	}
	
	/*
	 * If checked unchanged values of the multivalue attributes are not reported
	 * 
	 */
	private boolean getSkipUnchangedValues(RptReportDto report) {
		IdmFormInstanceDto formInstance = new IdmFormInstanceDto(report, getFormDefinition(), report.getFilter());
		Serializable value = formInstance.toSinglePersistentValue(PARAMETER_SKIP_UNCHANGED_VALUES);
		if (value instanceof Boolean) {
			return ((Boolean) value).booleanValue();
		}
		return false;
	}

	/**
	 * Parser of the System, mapping and mappingAttribute configuration.
	 * Configuration is sent from FE in a String using JSON form.
	 * 
	 * TODO: create json java POJO representation and use it as result type
	 * 
	 * @param report
	 * @return
	 */
	private MultiValueMap<String, UUID> parseAttributeConfig(RptReportDto report) {
		IdmFormInstanceDto formInstance = new IdmFormInstanceDto(report, getFormDefinition(), report.getFilter());
		Serializable value = formInstance.toSinglePersistentValue(PARAMETER_MAPPING_ATTRIBUTES);
		MultiValueMap<String, UUID> output = new LinkedMultiValueMap<String, UUID>();
		//
		if (!(value instanceof String)) {
			throw new ResultCodeException(RptResultCode.REPORT_WRONG_CONFIGURATION, ImmutableMap.of("attribute","all")); 
		}
		//
		// TODO: create json java POJO representation
		String stringValue = (String) value;
		try {
			String nodeValue;
			JsonNode treeNode = getMapper().readTree(stringValue);

			// get system id form config
			JsonNode node = treeNode.get(PARAMETER_SYSTEM);
			if (node == null) {
				throw new ResultCodeException(RptResultCode.REPORT_WRONG_CONFIGURATION, ImmutableMap.of("attribute", PARAMETER_SYSTEM));
			}
			nodeValue = node.asText();
			output.add(PARAMETER_SYSTEM, DtoUtils.toUuid(nodeValue));

			// get system mapping id form config
			node = treeNode.get(PARAMETER_SYSTEM_MAPPING);
			if (node == null) {
				throw new ResultCodeException(RptResultCode.REPORT_WRONG_CONFIGURATION, ImmutableMap.of("attribute", PARAMETER_SYSTEM_MAPPING));
			}
			nodeValue = node.asText();
			output.add(PARAMETER_SYSTEM_MAPPING, UUID.fromString(nodeValue));

			// get system mapping attributes id form config
			JsonNode nodeArray = treeNode.get(PARAMETER_MAPPING_ATTRIBUTES);
			if (nodeArray == null || !nodeArray.isArray()) {
				throw new ResultCodeException(RptResultCode.REPORT_WRONG_CONFIGURATION, ImmutableMap.of("attribute", PARAMETER_MAPPING_ATTRIBUTES));
			}
			for (JsonNode item : nodeArray) {
				output.add(PARAMETER_MAPPING_ATTRIBUTES, UUID.fromString(item.asText()));
			}
		} catch (Exception ex) {
			throw new ReportRenderException(report.getName(), ex);
		}
		return output;
	}

	/*********************************************************************
	 * ************* New methods
	 ******************************************/

	/**
	 * Find the identity and account from the AccIdentityAccountDto binding
	 * 
	 * @param accountId
	 * @param systemId
	 * @return
	 */
	private Pair<AccAccountDto, IdmIdentityDto> findIdentityAndAccount(UUID systemId, UUID accountId, UUID identityId) {
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setAccountId(accountId);
		filter.setIdentityId(identityId);
		filter.setSystemId(systemId);
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(filter, null).getContent();
		if (identityAccounts.isEmpty()) {
			AccAccountDto account = accountService.get(accountId);
			return Pair.of(account, null);
		}
		IdmIdentityDto identity = getLookupService().lookupEmbeddedDto(identityAccounts.get(0),
				AccIdentityAccount_.identity);
		AccAccountDto account = getLookupService().lookupEmbeddedDto(identityAccounts.get(0),
				AccIdentityAccount_.account);
		return Pair.of(account, identity);
	}

	/**
	 * Starts the provisioning in the dry run for single account. Results of the
	 * calculated provisioning is used for differences evaluation and highlight.
	 * 
	 * @param account
	 * @param identity
	 * @param selectedAttributeNames
	 * @return
	 */
	private List<SysAttributeDifferenceDto> createAccountDifferences(AccAccountDto account, IdmIdentityDto identity,
			List<String> selectedAttributeNames) {
		EventContext<AccAccountDto> eventCtx = provisioningService.doProvisioning(account, identity,
				ImmutableMap.of(ProvisioningService.DRY_RUN_PROPERTY_NAME, Boolean.TRUE));
		EventResult<AccAccountDto> result = eventCtx.getLastResult();
		if (result == null) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_FAILED, ImmutableMap.of("name", account.getId(),
					"system", account.getSystem(), "operationType", "DRY_RUN", "objectClass", ""));
		}
		ProvisioningOperation provisioningOperation = (ProvisioningOperation) result.getEvent().getProperties()
				.get(EventResult.EVENT_PROPERTY_RESULT);

		if (provisioningOperation == null) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_FAILED, ImmutableMap.of("name", account.getId(),
					"system", account.getSystem(), "operationType", "DRY_RUN", "objectClass", ""));
		}

		ProvisioningContext context = provisioningOperation.getProvisioningContext();
		context = filterAttributes(context, selectedAttributeNames);

		if (context == null) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_FAILED, ImmutableMap.of("name", account.getId(),
					"system", account.getSystem(), "operationType", "DRY_RUN", "objectClass", ""));
		}

		List<SysAttributeDifferenceDto> differences = provisioningArchiveService
				.evaluateProvisioningDifferences(context.getSystemConnectorObject(), context.getConnectorObject());

		return differences;
	}

	/**
	 * Creates report object for saving to output json file. It invokes record
	 * generating for every account/identity
	 * 
	 * @param changeDataDto
	 * @param accountFilter
	 * @param identityIds
	 * @param systemId
	 * @param attributes
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	private void createReportData(
			JsonGenerator jGenerator,
			AccAccountFilter accountFilter,
			Set<UUID> identityIds,
			UUID systemId,
			List<String> selectedAttributeNames,
			boolean skipUnchangedMultivalue) throws IOException {

		if (identityIds == null) { // null indicates that no identity explicitly specified from report config
			List<UUID> accountIds = accountService.findIds(accountFilter, null).getContent();
			IdmIdentityDto identity = null;
			String key = null;

			for (UUID accountId : accountIds) {
				Pair<AccAccountDto, IdmIdentityDto> pair = findIdentityAndAccount(systemId, accountId, null);
				AccAccountDto account = pair.getLeft();
				identity = pair.getRight();
				key = createKey(account, identity);
				RptChangesOnSystemRecordDto record = new RptChangesOnSystemRecordDto();
				record.setIdentifier(key);
				if (identity == null) {
					record.setState(RptChangesOnSystemState.NO_ENTITY_FOR_ACCOUNT);
					record.setAttributeDifferences(new ArrayList<SysAttributeDifferenceDto>());
					// dry run provisioning cannot be performed without identity
				} else {
					try {
						List<SysAttributeDifferenceDto> differences = createAccountDifferences(account, identity,
								selectedAttributeNames);
						differences = removeUnchangedMultivalues(differences, skipUnchangedMultivalue);
						record.setAttributeDifferences(differences);
						record.setState(createRecordState(differences));
					} catch (Exception e) {
						record.setState(RptChangesOnSystemState.FAILED);
						record.setIdentifier(key);
						record.setError(ExceptionUtils.getStackTrace(e));
					}
				}
				getMapper().writeValue(jGenerator, record);
			}
		} else {
			for (UUID identityId : identityIds) {
				IdmIdentityDto identity = identityService.get(identityId);
				if (identity == null) {
					LOG.info("Wrong user UUID [{}] inserted.", identityId);
					continue;
				}
				accountFilter.setIdentityId(identityId);
				List<AccAccountDto> accounts = accountService.find(accountFilter, null).getContent();

				if (accounts.isEmpty()) {
					String key = createKey(null, identity);
					RptChangesOnSystemRecordDto record = new RptChangesOnSystemRecordDto();
					record.setIdentifier(key);
					record.setState(RptChangesOnSystemState.NO_ACCOUNT_FOR_ENTITY);
					record.setAttributeDifferences(new ArrayList<SysAttributeDifferenceDto>());
					getMapper().writeValue(jGenerator, record);
					continue;
				}
				for (AccAccountDto account : accounts) {
					String key = createKey(account, identity);
					try {
						List<SysAttributeDifferenceDto> differences = createAccountDifferences(account, identity,
								selectedAttributeNames);
						differences = removeUnchangedMultivalues(differences, skipUnchangedMultivalue);
						RptChangesOnSystemRecordDto record = new RptChangesOnSystemRecordDto();
						record.setIdentifier(key);
						record.setAttributeDifferences(differences);
						record.setState(createRecordState(differences));
						getMapper().writeValue(jGenerator, record);
					} catch (Exception e) {
						RptChangesOnSystemRecordDto record = new RptChangesOnSystemRecordDto();
						record.setState(RptChangesOnSystemState.FAILED);
						record.setIdentifier(key);
						record.setError(ExceptionUtils.getStackTrace(e));
						getMapper().writeValue(jGenerator, record);
					}
				}
			}
		}
	}
	
	/*
	 * Method removes unchanged values of multivalue attributes.
	 * It increases clarity of the output. 
	 */
	private List<SysAttributeDifferenceDto> removeUnchangedMultivalues(List<SysAttributeDifferenceDto> differences, boolean removeUnchanged) {
		if (CollectionUtils.isEmpty(differences) || !removeUnchanged) {
			return differences;
		}
		List<SysAttributeDifferenceDto> result = new ArrayList<>();
		for (SysAttributeDifferenceDto value : differences) {
			if(!value.isMultivalue()) {
				result.add(value);
				continue;
			}
			
			if (!value.isChanged()) {
				value.setValues(new ArrayList<SysAttributeDifferenceValueDto>());
				result.add(value);
				continue;
			}
			
			List<SysAttributeDifferenceValueDto> subValues = value.getValues();
			subValues = subValues.stream().filter(val -> {return val.getChange() != null;}).collect(Collectors.toList());
			value.setValues(subValues);
			result.add(value);			
		}
		return result;
	}

	/**
	 * Removes attributes which are not listed in the report configuration. If none
	 * attributes are explicitly selected all attributes are reported.
	 * 
	 * @param context
	 * @param selectedAttributes
	 * @return
	 */
	private ProvisioningContext filterAttributes(ProvisioningContext context, List<String> selectedAttributes) {
		if (context == null || selectedAttributes == null || selectedAttributes.isEmpty()) {
			return context;
		}

		IcConnectorObjectImpl systemObj = (IcConnectorObjectImpl) context.getSystemConnectorObject();
		if (systemObj != null) {
			List<IcAttribute> attributes = systemObj.getAttributes().stream()
					.filter(attr -> selectedAttributes.contains(attr.getName())).collect(Collectors.toList());
			systemObj.setAttributes(attributes);
		}

		IcConnectorObjectImpl changesObj = (IcConnectorObjectImpl) context.getConnectorObject();
		if (changesObj != null) {
			List<IcAttribute> attributes = changesObj.getAttributes().stream()
					.filter(attr -> selectedAttributes.contains(attr.getName())).collect(Collectors.toList());
			changesObj.setAttributes(attributes);
		}

		return context;
	}

	/**
	 * Prepares the set of attribute names for following filtering.
	 * 
	 * @param attributes
	 * @return
	 */
	private List<String> getSelectedAttributeNames(List<SysSystemAttributeMappingDto> attributes) {
		List<String> result = new ArrayList<String>();
		for (SysSystemAttributeMappingDto attr : attributes) {
			SysSchemaAttributeDto dto = (SysSchemaAttributeDto) getLookupService().lookupEmbeddedDto(attr,
					SysSystemAttributeMapping_.schemaAttribute);
			if (dto != null) {
				result.add(dto.getName());
			}
		}
		return result;
	}

	/**
	 * Deduces record state based on result of differences evaluation
	 * 
	 * @param differences
	 * @return
	 */
	private RptChangesOnSystemState createRecordState(List<SysAttributeDifferenceDto> differences) {
		if (differences == null) {
			return RptChangesOnSystemState.NO_CHANGE;
		}
		// check if added state
		boolean isAdded = false;
		for (SysAttributeDifferenceDto difference : differences) {
			SysAttributeDifferenceValueDto value = difference.getValue();
			if (!difference.isMultivalue() && value != null) {
				if (value.getChange()==SysValueChangeType.ADDED) {
					isAdded = true;
					continue;
				}
				isAdded = false;
				break;
			}
		}
		if (isAdded) {
			return RptChangesOnSystemState.ADDED;
		}
				
		for (SysAttributeDifferenceDto difference : differences) {
			if (difference.isChanged()) {
				return RptChangesOnSystemState.CHANGED;
			}
		}
		return RptChangesOnSystemState.NO_CHANGE;
	}

}