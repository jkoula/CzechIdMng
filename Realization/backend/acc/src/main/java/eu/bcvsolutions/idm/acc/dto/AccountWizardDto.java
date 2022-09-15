package eu.bcvsolutions.idm.acc.dto;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.dto.AbstractWizardDto;

/**
 * Dto for work with account in wizard
 * @author Roman Kucera
 */
@Relation(collectionRelation = "accountWizards")
public class AccountWizardDto extends AbstractWizardDto {
}
