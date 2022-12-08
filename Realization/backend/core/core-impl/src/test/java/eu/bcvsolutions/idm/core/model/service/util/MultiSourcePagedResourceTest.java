package eu.bcvsolutions.idm.core.model.service.util;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.adapter.AdaptableService;
import eu.bcvsolutions.idm.core.api.service.adapter.DtoAdapter;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;
import org.junit.Assert;
import org.junit.Test;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MultiSourcePagedResourceTest extends AbstractUnitTest {

    @Test
    public void testSingleResource() {

        final List<MockDto> inputData = Stream.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()).map(MockDto::new).collect(Collectors.toList());
        TestListResource resource = new TestListResource(inputData);

        MultiSourcePagedResource<MockDto, DataFilter,DataFilter, MockDto> multiresource = new MultiSourcePagedResource<>(List.of(resource), new ModelMapper());

        Pageable pageable = PageRequest.of(0, 2);
        final Page<MockDto> mockDtos = multiresource.find(null, pageable, null);
        Assert.assertNotNull(mockDtos);
        Assert.assertEquals(2, mockDtos.getNumberOfElements());
        Assert.assertEquals(5, mockDtos.getTotalElements());
        Assert.assertEquals(inputData.subList(0,2), mockDtos.getContent());
        //
        Pageable pageable2 = PageRequest.of(1, 2);
        final Page<MockDto> mockDtos2 = multiresource.find(null, pageable2, null);
        Assert.assertNotNull(mockDtos2);
        Assert.assertEquals(2, mockDtos2.getNumberOfElements());
        Assert.assertEquals(5, mockDtos2.getTotalElements());
        Assert.assertEquals(inputData.subList(2,4), mockDtos2.getContent());
        //
        Pageable pageable3 = PageRequest.of(0, 7);
        final Page<MockDto> mockDtos3 = multiresource.find(null, pageable3, null);
        Assert.assertNotNull(mockDtos3);
        Assert.assertEquals(5, mockDtos3.getNumberOfElements());
        Assert.assertEquals(5, mockDtos3.getTotalElements());
        Assert.assertEquals(inputData, mockDtos3.getContent());
    }

    @Test
    public void testMultipleResources() {
        final List<MockDto> inputData1 = Stream.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()).map(MockDto::new).collect(Collectors.toList());
        TestListResource resource1 = new TestListResource(inputData1);
        final List<MockDto> inputData2 = Stream.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()).map(MockDto::new).collect(Collectors.toList());
        TestListResource resource2 = new TestListResource(inputData2);
        final List<MockDto> inputData3 = Stream.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()).map(MockDto::new).collect(Collectors.toList());
        TestListResource resource3 = new TestListResource(inputData3);

        MultiSourcePagedResource<MockDto, DataFilter, DataFilter, MockDto> multiresource = new MultiSourcePagedResource<>(List.of(resource1, resource2, resource3), new ModelMapper());

        Pageable pageable = PageRequest.of(0, 2);
        final Page<MockDto> mockDtos = multiresource.find(null, pageable, null);
        Assert.assertNotNull(mockDtos);
        Assert.assertEquals(2, mockDtos.getNumberOfElements());
        Assert.assertEquals(12, mockDtos.getTotalElements());
        Assert.assertEquals(inputData1.subList(0,2), mockDtos.getContent());

        Pageable pageable2 = PageRequest.of(0, 7);
        final Page<MockDto> mockDtos2 = multiresource.find(null, pageable2, null);
        Assert.assertNotNull(mockDtos2);
        Assert.assertEquals(7, mockDtos2.getNumberOfElements());
        Assert.assertEquals(12, mockDtos2.getTotalElements());
        final List<MockDto> expectedResult1 = Stream.concat(inputData1.stream(), inputData2.subList(0, 2).stream()).collect(Collectors.toList());
        Assert.assertEquals(expectedResult1, mockDtos2.getContent());

        Pageable pageable3 = PageRequest.of(0, 11);
        final Page<MockDto> mockDtos3 = multiresource.find(null, pageable3, null);
        Assert.assertNotNull(mockDtos3);
        Assert.assertEquals(11, mockDtos3.getNumberOfElements());
        Assert.assertEquals(12, mockDtos3.getTotalElements());
        final List<MockDto> expectedResult2 = Stream.concat(Stream.concat(inputData1.stream(), inputData2.stream()), inputData3.subList(0, 2).stream()).collect(Collectors.toList());
        Assert.assertEquals(expectedResult2, mockDtos3.getContent());

        Pageable pageable4 = PageRequest.of(1, 4);
        final Page<MockDto> mockDtos4 = multiresource.find(null, pageable4, null);
        Assert.assertNotNull(mockDtos4);
        Assert.assertEquals(4, mockDtos4.getNumberOfElements());
        Assert.assertEquals(12, mockDtos4.getTotalElements());
        final List<MockDto> expectedResult3 = Stream.concat(Stream.of(inputData1.get(inputData1.size()-1)), inputData2.subList(0, 3).stream()).collect(Collectors.toList());
        Assert.assertEquals(expectedResult3, mockDtos4.getContent());

        Pageable pageable5 = PageRequest.of(2, 4);
        final Page<MockDto> mockDtos5 = multiresource.find(null, pageable5, null);
        Assert.assertNotNull(mockDtos5);
        Assert.assertEquals(4, mockDtos5.getNumberOfElements());
        Assert.assertEquals(12, mockDtos5.getTotalElements());
        final List<MockDto> expectedResult4 = Stream.concat(Stream.of(inputData2.get(inputData2.size()-1)), inputData3.stream()).collect(Collectors.toList());
        Assert.assertEquals(expectedResult4, mockDtos5.getContent());

        Pageable pageable6 = PageRequest.of(1, 11);
        final Page<MockDto> mockDtos6 = multiresource.find(null, pageable6, null);
        Assert.assertNotNull(mockDtos6);
        Assert.assertEquals(1, mockDtos6.getNumberOfElements());
        Assert.assertEquals(12, mockDtos6.getTotalElements());
        final List<MockDto> expectedResult5 = Stream.of(inputData3.get(inputData3.size()-1)).collect(Collectors.toList());
        Assert.assertEquals(expectedResult5, mockDtos6.getContent());
    }



    private class TestListResource implements AdaptableService<MockDto, DataFilter, MockDto> {

        private final List<MockDto> data;

        private TestListResource(List<MockDto> data) {
            if (data == null) {
                throw new IllegalArgumentException("no data provided");
            }
            this.data = data;
        }

        @Override
        public Class<MockDto> getDtoClass() {
            return MockDto.class;
        }

        @Override
        public Class<? extends BaseEntity> getEntityClass() {
            throw new UnsupportedOperationException("Not needed for tests");
        }

        @Override
        public Class<DataFilter> getFilterClass() {
            return DataFilter.class;
        }

        @Override
        public boolean supportsToDtoWithFilter() {
            throw new UnsupportedOperationException("Not needed for tests");
        }

        @Override
        public MockDto get(Serializable id, BasePermission... permission) {
            return data.stream().filter(mockDto -> mockDto.getId().equals(id)).findFirst().orElse(null);
        }

        @Override
        public MockDto get(Serializable id, DataFilter context, BasePermission... permission) {
            return data.stream().filter(mockDto -> mockDto.getId().equals(id)).findFirst().orElse(null);
        }

        @Override
        public Page<MockDto> find(Pageable pageable, BasePermission... permission) {
            return find(null, pageable, permission);
        }

        @Override
        public Page<MockDto> find(DataFilter filter, Pageable pageable, BasePermission... permission) {
            return new PageImpl<>(data.subList(
                    Math.toIntExact(Math.min(pageable.getOffset(), data.size())),
                    Math.min(
                            Math.toIntExact(pageable.getOffset() + pageable.getPageSize()),
                            data.size())),
                    pageable, data.size());
        }

        @Override
        public Page<UUID> findIds(Pageable pageable, BasePermission... permission) {
            return findIds(null, pageable, permission);
        }

        @Override
        public Page<UUID> findIds(DataFilter filter, Pageable pageable, BasePermission... permission) {
            return new PageImpl<>(find(pageable, permission).stream().map(AbstractDto::getId).collect(Collectors.toList()),pageable, data.size());
        }

        @Override
        public long count(DataFilter filter, BasePermission... permission) {
            return data.size();
        }

        @Override
        public boolean isNew(MockDto dto) {
            return dto.getId() == null;
        }

        @Override
        public Set<String> getPermissions(Serializable id) {
            throw new UnsupportedOperationException("Not needed for tests");
        }

        @Override
        public Set<String> getPermissions(MockDto dto) {
            throw new UnsupportedOperationException("Not needed for tests");
        }

        @Override
        public MockDto checkAccess(MockDto dto, BasePermission... permission) {
            throw new UnsupportedOperationException("Not needed for tests");
        }

        @Override
        public void export(UUID id, IdmExportImportDto batch) {
            throw new UnsupportedOperationException("Not needed for tests");
        }

        @Override
        public void siemLog(String action, String status, String targetName, String targetUuid, String subjectName, String subjectUuid, String transactionUuid, String reason) {
            throw new UnsupportedOperationException("Not needed for tests");
        }

        @Override
        public void siemLog(String action, String status, BaseDto targetDto, BaseDto subjectDto, String transactionUuid, String reason) {
            throw new UnsupportedOperationException("Not needed for tests");
        }

        @Override
        public <F2 extends BaseFilter> DtoAdapter<MockDto, MockDto> getAdapter(F2 originalFilter) {
            return input -> input;
        }

        @Override
        public boolean supports(Class<?> delimiter) {
            return false;
        }
    }

    private class MockDto extends AbstractDto {

        public MockDto(UUID id) {
            super(id);
        }

        @Override
        public String toString() {
            return "Mock[" + getId().toString().substring(0, getId().toString().indexOf("-")) + "]";
        }
    }


}