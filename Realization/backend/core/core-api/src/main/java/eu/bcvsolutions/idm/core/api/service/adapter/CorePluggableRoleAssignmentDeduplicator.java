package eu.bcvsolutions.idm.core.api.service.adapter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;

/**
 * Basic assigned role deduplicator. It checks if the owner has a role assigned multiple times.
 * It can check for validity and attributes.
 *
 Check if {@link IdmIdentityRoleDto} <b>ONE</b> is duplicit against {@link IdmIdentityRoleDto} <b>TWO</b>.</br></br>
 * Method check these states:</br>
 * - If {@link IdmIdentityRoleDto} has same {@link IdmRoleDto}</br>
 * - If {@link IdmIdentityRoleDto} has same {@link IdmIdentityContractDto}</br>
 * - If both roles are automatically added (in this case is return always false)</br>
 * - If role <b>ONE</b> is duplicity with validity to role <b>TWO</b>. When are both roles manually added is also check if
 * role <b>TWO</b> is duplicity with validity to role <b>ONE</b>
 * - If {@link IdmIdentityRoleDto} has same definition and values (this can be skipped by parameter @param <b>skipSubdefinition</b>)</br>
 * </br>
 * <b>Beware,</b> for check subdefinition is needed that given identity role has filled <b>_eavs</b> attribute with form instance. Form
 * definition with values is not get by database.
 *
 * @since 9.5.0
 * @see <a href="https://wiki.czechidm.com/devel/documentation/roles/dev/identity-role-deduplication">Documentation link</a> for more information
 *
 * @author Tomáš Doischer
 */
@Component
public class CorePluggableRoleAssignmentDeduplicator implements PluggableRoleAssignmentDeduplicator {

	@Override
	public AbstractRoleAssignmentDto getDuplicated(AbstractRoleAssignmentDto one, AbstractRoleAssignmentDto two, Boolean skipSubdefinition) {
		Assert.notNull(one, "The first assinged role to compare is required.");
		Assert.notNull(two, "The second assinged role to compare is required.");
		//
		if (one instanceof IdmIdentityRoleDto && two instanceof IdmIdentityRoleDto) {
			IdmIdentityRoleDto oneIdentityRole = (IdmIdentityRoleDto) one;
			IdmIdentityRoleDto twoIdentityRole = (IdmIdentityRoleDto) two;
			if (!oneIdentityRole.getIdentityContract().equals(twoIdentityRole.getIdentityContract())) {
				return null;
			}
		}
		//
		if (!one.getRole().equals(two.getRole())) {
			// Role isn't same
			return null;
		}

		// Role-system isn't same.
		if (one.getRoleSystem() == null) {
			if (two.getRoleSystem() != null) {
				return null;
			}
		} else if (!one.getRoleSystem().equals(two.getRoleSystem())) {
			return null;
		}

		AbstractRoleAssignmentDto manually = null;
		AbstractRoleAssignmentDto automatic = null;

		if (isRoleAutomaticOrComposition(one)) {
			automatic = one;
			manually = two;
		}

		if (isRoleAutomaticOrComposition(two)) {
			if (automatic != null) {
				// Automatic role is set from role ONE -> Both identity roles are automatic
				if (one.getDirectRole() == null
						|| two.getDirectRole() == null
						|| one.getRoleComposition() == null
						|| two.getRoleComposition() == null) {
					// role was not created by business role definition
					return null;
				}
				if (Objects.equals(one.getDirectRole(), two.getDirectRole())
						&& Objects.equals(one.getRoleComposition(), two.getRoleComposition())) {
					// #2034 compositon is duplicate
					return getIdentityRoleForRemove(one, two);
				}
				// automatic roles or composition is not duplicate
				return null;
			}
			automatic = two;
			manually = one;
		}

		/// Check duplicity for validity
		AbstractRoleAssignmentDto validityDuplicity = null;
		if (automatic == null) {
			// Check if ONE role is duplicate with TWO and change order
			boolean duplicitOne = isIdentityRoleDatesDuplicit(one, two);
			boolean duplicitTwo = isIdentityRoleDatesDuplicit(two, one);

			if (duplicitOne && duplicitTwo) {
				// Both roles are same call method for decide which role will be removed
				validityDuplicity = getIdentityRoleForRemove(one, two);
			} else if (duplicitOne) {
				// Only role ONE is duplicit with TWO
				validityDuplicity = one;
			} else if (duplicitTwo) {
				// Only role TWO is duplicit with ONE
				validityDuplicity = two;
			}
		} else {
			// In case that we have only manually and automatic compare only from one order
			if (isIdentityRoleDatesDuplicit(manually, automatic)) {
				validityDuplicity = manually;
			}
		}

		// Check subdefinition can be skipped
		// and must be checked after validity
		if (BooleanUtils.isNotTrue(skipSubdefinition)) {
			// Validity must be same and subdefinition also. Then is possible remove role.
			// Subdefinition must be exactly same and isn't different between manually and automatic identity role
			if (validityDuplicity != null && equalsSubdefinitions(one, two)) {
				return validityDuplicity;
			}
		} else {
			// Check for subdefintion is skipped return only duplicity
			return validityDuplicity;
		}

		// No duplicity founded
		return null;
	}

	@Override
	public boolean considerOrder() {
		return true;
	}

	private boolean isIdentityRoleDatesDuplicit(AbstractRoleAssignmentDto one, AbstractRoleAssignmentDto two) {
		LocalDate validTillForFirst = getDateForValidTill(one);
		// Validity role is in interval in a second role
		if (isDatesInRange(one.getValidFrom(), validTillForFirst, two.getValidFrom(), two.getValidTill())) {
			return true;
		}

		// Both role are valid
		if (one.isValid() && two.isValid()) {
			LocalDate validTillForTwo = two.getValidTill();
			if ((validTillForFirst == null && validTillForTwo == null) ||
					(validTillForFirst != null && validTillForTwo != null && validTillForFirst.isEqual(validTillForTwo))) {
				// Valid tills from both identity roles are same
				return true;
			} else if (validTillForFirst != null && validTillForTwo == null) {
				// Second identity role has filled valid till but first not.
				// This mean that role TWO has bigger validity till than ONE
				return false;
			} else if (validTillForFirst != null && validTillForFirst.isBefore(validTillForTwo)) {
				// Valid till from manually role is before automatic, manually role could be removed
				return true;
			}
		}
		return false;
	}

	private LocalDate getDateForValidTill(AbstractRoleAssignmentDto one) {
		if (one instanceof IdmIdentityRoleDto) {
			IdmIdentityRoleDto oneIdentityRole = (IdmIdentityRoleDto) one;
			return getDateForValidTill(oneIdentityRole);
		}

		return one.getValidTill();
	}

	/**
	 * Get valid till for {@link IdmIdentityRoleDto}. Valid till could be set from contract if
	 * date is after valid till from contract.
	 *
	 * @param identityRole
	 * @return
	 */
	protected LocalDate getDateForValidTill(IdmIdentityRoleDto identityRole) {
		LocalDate validTill = identityRole.getValidTill();
		IdmIdentityContractDto identityContractDto = DtoUtils.getEmbedded(identityRole, IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT, IdmIdentityContractDto.class, null);
		LocalDate validTillContract = identityContractDto.getValidTill();

		if (validTill != null && validTillContract != null && validTillContract.isAfter(validTill)) {
			return validTill;
		}

		if (validTillContract == null && validTill != null) {
			return validTill;
		}

		return validTillContract;
	}

	private boolean equalsSubdefinitions(AbstractRoleAssignmentDto one, AbstractRoleAssignmentDto two) {

		List<IdmFormInstanceDto> eavsOne = one.getEavs();
		List<IdmFormInstanceDto> eavsTwo = two.getEavs();

		// Size of form instance doesn't match
		if (eavsOne.size() != eavsTwo.size()) {
			return false;
		}

		// Form instances are empty, subdefiniton are equals
		if (eavsOne.isEmpty()) {
			return true;
		}

		// Now is possible only one form instance for identity role
		// Get form instance from both identity roles
		IdmFormInstanceDto formInstanceOne = eavsOne.get(0);
		IdmFormInstanceDto formInstanceTwo = eavsTwo.get(0);

		List<Serializable> oneValues = Collections.emptyList();
		List<Serializable> twoValues = Collections.emptyList();
		if (formInstanceOne != null) {
			oneValues = eavsOne.get(0) //
					.getValues() //
					.stream() //
					.map(IdmFormValueDto::getValue) //
					.collect(Collectors.toList()); //
		}
		if (formInstanceTwo != null) {
			twoValues = eavsTwo.get(0) //
					.getValues() //
					.stream() //
					.map(IdmFormValueDto::getValue) //
					.collect(Collectors.toList()); //
		}

		// Values doesn't match
		if (oneValues.size() != twoValues.size()) {
			return false;
		}

		// Compare collections
		return CollectionUtils.isEqualCollection(oneValues, twoValues);
	}

	public boolean isRoleAutomaticOrComposition(AbstractRoleAssignmentDto identityRole) {
		return identityRole.getAutomaticRole() != null || identityRole.getDirectRole() != null;
	}

	protected AbstractRoleAssignmentDto getIdentityRoleForRemove(AbstractRoleAssignmentDto one, AbstractRoleAssignmentDto two) {
		// Both roles are same, remove newer
		if (one.getCreated().isAfter(two.getCreated())) {
			return one;
		}
		return two;
	}

	/**
	 * Check if given dates is in range/interval the second ones.
	 *
	 * @param validFrom
	 * @param validTill
	 * @param rangeFrom
	 * @param rangeTill
	 * @return
	 */
	private boolean isDatesInRange(LocalDate validFrom, LocalDate validTill, LocalDate rangeFrom, LocalDate rangeTill) {
		boolean leftIntervalSideOk = false;
		boolean rightIntervalSideOk = false;

		if (rangeFrom == null || (validFrom != null && (rangeFrom.isBefore(validFrom) || rangeFrom.isEqual(validFrom)))) {
			leftIntervalSideOk = true;
		}

		if (rangeTill == null || (validTill != null && (rangeTill.isAfter(validTill) || rangeTill.isEqual(validTill)))) {
			rightIntervalSideOk = true;
		}

		return leftIntervalSideOk && rightIntervalSideOk;
	}
}
