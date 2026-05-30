package id.my.agungdh.mapper;

import id.my.agungdh.dto.CommentDTO;
import id.my.agungdh.entity.Comment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface CommentMapper {
    CommentDTO toDTO(Comment entity);
    List<CommentDTO> toDTOs(List<Comment> entities);
}
