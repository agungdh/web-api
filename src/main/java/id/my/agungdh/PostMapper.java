package id.my.agungdh;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "cdi", uses = {CategoryMapper.class, TagMapper.class})
public interface PostMapper {
    PostDTO toDTO(Post entity);
    List<PostDTO> toDTOs(List<Post> entities);
}
