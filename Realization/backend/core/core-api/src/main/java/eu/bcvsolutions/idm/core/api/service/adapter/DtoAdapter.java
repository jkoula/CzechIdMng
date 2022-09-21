package eu.bcvsolutions.idm.core.api.service.adapter;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;

import java.util.stream.Stream;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface DtoAdapter<D extends BaseDto, R> {
    Stream<R> transform(Stream<D> input);
}
