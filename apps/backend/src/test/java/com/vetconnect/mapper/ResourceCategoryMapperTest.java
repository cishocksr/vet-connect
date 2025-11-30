package com.vetconnect.mapper;

import com.vetconnect.dto.resource.ResourceCategoryDTO;
import com.vetconnect.model.ResourceCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResourceCategoryMapperTest {

    private ResourceCategoryMapper mapper;
    private ResourceCategory testCategory;

    @BeforeEach
    void setUp() {
        mapper = new ResourceCategoryMapper();

        testCategory = new ResourceCategory();
        testCategory.setId(1);
        testCategory.setName("Healthcare");
        testCategory.setDescription("Healthcare and medical resources for veterans");
        testCategory.setIconName("health");
    }

    @Test
    void toDTO_ShouldMapAllFields() {
        ResourceCategoryDTO dto = mapper.toDTO(testCategory);

        assertNotNull(dto);
        assertEquals(testCategory.getId(), dto.getId());
        assertEquals(testCategory.getName(), dto.getName());
        assertEquals(testCategory.getDescription(), dto.getDescription());
        assertEquals(testCategory.getIconName(), dto.getIconName());
    }

    @Test
    void toDTO_WithNullCategory_ShouldReturnNull() {
        ResourceCategoryDTO dto = mapper.toDTO(null);
        assertNull(dto);
    }

    @Test
    void toDTOWithCount_ShouldIncludeResourceCount() {
        Long resourceCount = 15L;

        ResourceCategoryDTO dto = mapper.toDTOWithCount(testCategory, resourceCount);

        assertNotNull(dto);
        assertEquals(testCategory.getId(), dto.getId());
        assertEquals(resourceCount, dto.getResourceCount());
    }

    @Test
    void toDTOWithCount_WithNullCategory_ShouldReturnNull() {
        ResourceCategoryDTO dto = mapper.toDTOWithCount(null, 10L);
        assertNull(dto);
    }

    @Test
    void toDTOList_ShouldMapAllCategories() {
        ResourceCategory category2 = new ResourceCategory();
        category2.setId(2);
        category2.setName("Housing");
        category2.setDescription("Housing assistance");

        List<ResourceCategory> categories = Arrays.asList(testCategory, category2);
        List<ResourceCategoryDTO> dtos = mapper.toDTOList(categories);

        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        assertEquals(testCategory.getId(), dtos.get(0).getId());
        assertEquals(category2.getId(), dtos.get(1).getId());
    }

    @Test
    void toDTOList_WithNullList_ShouldReturnNull() {
        List<ResourceCategoryDTO> dtos = mapper.toDTOList(null);
        assertNull(dtos);
    }

    @Test
    void toDTOList_WithEmptyList_ShouldReturnEmptyList() {
        List<ResourceCategoryDTO> dtos = mapper.toDTOList(Arrays.asList());
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }

    @Test
    void toEntity_ShouldMapDTOToEntity() {
        ResourceCategoryDTO dto = ResourceCategoryDTO.builder()
                .name("Education")
                .description("Educational resources")
                .iconName("book")
                .build();

        ResourceCategory entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals(dto.getName(), entity.getName());
        assertEquals(dto.getDescription(), entity.getDescription());
        assertEquals(dto.getIconName(), entity.getIconName());
    }

    @Test
    void toEntity_WithNullDTO_ShouldReturnNull() {
        ResourceCategory entity = mapper.toEntity(null);
        assertNull(entity);
    }
}