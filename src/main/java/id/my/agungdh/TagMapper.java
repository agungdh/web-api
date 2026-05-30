package id.my.agungdh;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagMapper {
    TagDTO toDTO(Tag entity);
    List<TagDTO> toDTOs(List<Tag> entities);
}
