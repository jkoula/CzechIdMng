package eu.bcvsolutions.idm.core.model.service.util;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.adapter.AdaptableService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 * This class joins multiple services into a single one and offers basic paging functionality across all of these resources.
 * There are some caveats to this solution, mainly performance and sorting.
 *
 * Performance
 *
 * Performance of searching across multiple services scales linearly with the number of services. When searching, each service
 * is queried either for data, or count. This means, then even if we are fetching only first few elements from the first service,
 * we still need to count the number of all the elements across all other services, otherwise we would not be able to create
 * {@link Pageable} instance, which requires total number of elements.
 *
 * Sorting
 *
 * {@link Pageable} allows client to specify {@link Sort} in which data should be returned. Specified {@link Sort} obejct is still forwarded
 * to underlying services. This means, that each sub-result will be sorted (if the particular {@link AdaptableService} supports it), but the whole
 * result will not be sorted.
 *
 * Example:
 *
 * Input data for each service:
 * service1: [A1, A2, A3, A4, A5]
 * service2: [B1, B2, B3, B4]
 * service3: [C1, C2]
 *
 * Search1:
 *  - Pageable[page:0, size 4]
 *  - Result: [A1, A2, A3, A4]
 *
 *  Search2:
 *  - Pageable[page:1, size 4]
 *  - Result: [A5, B1, B2, B3]
 *
 *  Search3:
 *  - Pageable[page:2, size 4]
 *  - Result: [B4, C1, C2]
 *
 * Adapting results using {@link eu.bcvsolutions.idm.core.api.service.adapter.DtoAdapter}
 *
 * Since this service combines multiple data sources, which means multiple different {@link eu.bcvsolutions.idm.core.api.dto.BaseDto}, we need a way
 * to convert them to the same data type. This is done using {@link eu.bcvsolutions.idm.core.api.service.adapter.DtoAdapter} object, which is
 * obtained by calling {@link AdaptableService#getAdapter(F2)}.
 *
 * @param <D> {@link BaseDto} class, which internal resources use
 * @param <F> {@link BaseFilter} class, which internal resources use
 * @param <F2> {@link BaseFilter} class, which this resource uses as an input filter. This filter is then translated to F filter using {@link ModelMapper}
 * @param <R> result type
 * @since 12.2.3
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public class MultiSourcePagedResource<D extends BaseDto, F extends BaseFilter, F2 extends BaseFilter, R> {

    private final Collection<AdaptableService<D, F, R>> sources;
    private final ModelMapper modelMapper;


    public MultiSourcePagedResource(Collection<AdaptableService<D, F, R>> sources, ModelMapper modelMapper) {
        this.sources = sources;
        this.modelMapper = modelMapper;
    }

    public Page<R> find(F2 filter, Pageable pageable, BasePermission[] permission) {
        return doPaging(
                (service, concreteFilter, currentPageable) -> service.find(concreteFilter, currentPageable, permission),
                (service, concreteFilter, currentPage) -> service.getAdapter(concreteFilter).transform(currentPage.stream()).collect(Collectors.toList()),
                filter, pageable, permission);
    }

    public Page<UUID> findIds(F2 filter, Pageable pageable, BasePermission[] permission) {
        return doPaging(
                (service, concreteFilter, currentPageable) -> service.findIds(concreteFilter, currentPageable, permission),
                (service, concreteFilter, currentPage) -> currentPage.getContent(),
                filter, pageable, permission);
    }

    public long count(F2 filter, BasePermission[] permission) {
        return doPaging(
                (service, concreteFilter, currentPageable) -> new PageImpl<>(Collections.emptyList(), currentPageable, service.count(concreteFilter)),
                (service, concreteFilter, currentPage) -> currentPage.getContent(),
                filter, Pageable.unpaged(), permission).getTotalElements();
    }

    private <O, Q> Page<Q> doPaging(
             TriFunction<AdaptableService<D, F, R>, F, Pageable, Page<O>> resultProvider,
            TriFunction<AdaptableService<D, F, R>,F , Page<O>, Collection<Q>> resultMapper, BaseFilter filter, Pageable pageable, BasePermission[] permission) {
        List<Q> result = new ArrayList<>();
        long total = 0L;
        for (AdaptableService<D, F, R> service : sources) {
            final int missingCount = Math.max(pageable.getPageSize() - result.size(), 0);
            final F concreteFilter = modelMapper.map(filter == null ? new EmptyFilter() : filter, service.getFilterClass());
            //
            if (missingCount != 0) {
                // Still need some records fetched
                final long offset = Math.max(0, (long) pageable.getPageNumber() * pageable.getPageSize() - total);
                final Pageable currentPageable = OffsetPageable.of(offset, 0, missingCount, pageable.getSort());
                //
                final Page<O> currentPage = resultProvider.apply(service, concreteFilter, currentPageable);
                result.addAll(resultMapper.apply(service, concreteFilter, currentPage));
                total += currentPage.getTotalElements();
            } else {
                // just count how many records there are
                final long count = service.count(concreteFilter, permission);
                total+= count;
            }
        }
        return new PageImpl<>(result, pageable, total);
    }

    @FunctionalInterface
    private interface TriFunction<T, U, V, R> {
        R apply(T var1, U var2, V var3);
    }

    public static class Builder<D extends BaseDto, F extends BaseFilter, F2 extends BaseFilter, R> {

        private ModelMapper modelMapper;
        private final List<AdaptableService<D, F, R>> adaptableServices = new ArrayList<>();

        public MultiSourcePagedResource<D, F, F2, R> build() {
            if (modelMapper == null) {
                throw new IllegalArgumentException("No model mapper specified. This needs it to translate filters.");
            }
            return new MultiSourcePagedResource<>(adaptableServices, modelMapper);
        }

        public void setModelMapper(ModelMapper modelMapper) {
            this.modelMapper = modelMapper;
        }

        public void addAdaptableServices(List<AdaptableService<D, F, R>> adaptableServices) {
            this.adaptableServices.addAll(adaptableServices);
        }
    }

    private static class OffsetPageable implements Pageable {

        private final long offset;
        private final int page;
        private final int size;

        private final Sort sort;

        private OffsetPageable(long offset, int page, int size, Sort sort) {
            this.offset = offset;
            this.page = page;
            this.size = size;
            this.sort = sort;
        }

        public static OffsetPageable of(long offset, int page, int size){
            return new OffsetPageable(offset, page, size, Sort.unsorted());
        }

        public static OffsetPageable of(long offset, int page, int size, Sort sort){
            return new OffsetPageable(offset, page, size, sort);
        }

        @Override
        public int getPageNumber() {
            return page;
        }

        @Override
        public int getPageSize() {
            return size;
        }

        @Override
        public long getOffset() {
            return offset + (long) page * size;
        }

        @Override
        public Sort getSort() {
            return sort;
        }

        @Override
        public Pageable next() {
            return OffsetPageable.of(getOffset(), getPageNumber() +1, getPageSize(), getSort());
        }

        @Override
        public Pageable previousOrFirst() {
            return OffsetPageable.of(getOffset(), getPageNumber() == 0 ? 0 : getPageNumber() - 1, getPageSize(), getSort());
        }

        @Override
        public Pageable first() {
            return OffsetPageable.of(getOffset(), 0, getPageSize());
        }

        @Override
        public boolean hasPrevious() {
            return getPageNumber() > 0;
        }

        @Override
        public String toString() {
            return "OffsetPageable[off:"+offset+",page:"+page+",size:"+size+"]";
        }
    }

}
