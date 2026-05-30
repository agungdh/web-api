package id.my.agungdh;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface CommentMapper {
    CommentDTO toDTO(Comment entity);
    List<CommentDTO> toDTOs(List<Comment> entities);
}
