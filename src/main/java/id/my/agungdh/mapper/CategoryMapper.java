package id.my.agungdh.mapper;

import id.my.agungdh.dto.CategoryDTO;
import id.my.agungdh.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {
    CategoryDTO toDTO(Category entity);
    List<CategoryDTO> toDTOs(List<Category> entities);
}
