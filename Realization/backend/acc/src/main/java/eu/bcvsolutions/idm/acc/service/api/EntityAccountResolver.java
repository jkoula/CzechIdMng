package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface EntityAccountResolver {

    List<Pair<AccAccountDto, AbstractDto>> resolveEntityAccount(UUID accountId, UUID systemId);

}
