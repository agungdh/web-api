package id.my.agungdh.mapper;

import id.my.agungdh.dto.TagDTO;
import id.my.agungdh.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagMapper {
    TagDTO toDTO(Tag entity);
    List<TagDTO> toDTOs(List<Tag> entities);
}
