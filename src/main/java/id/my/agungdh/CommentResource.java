package id.my.agungdh;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

@Path("/posts/{postUuid}/comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommentResource {

    @Inject
    CommentMapper commentMapper;

    @GET
    public PagedResponse<CommentDTO> getAll(@PathParam("postUuid") UUID postUuid,
                                            @QueryParam("page") @DefaultValue("0") int page,
                                            @QueryParam("size") @DefaultValue("20") int size) {
        PanacheQuery<Comment> query = Comment.find("post.uuid", postUuid);
        var list = query.page(Page.of(page, size)).list();
        return new PagedResponse<>(commentMapper.toDTOs(list), query.count(), page, size);
    }

    @POST
    @Transactional
    public Response create(@PathParam("postUuid") UUID postUuid, @Valid CommentRequest request) {
        Post post = Post.find("uuid", postUuid).firstResult();
        if (post == null) {
            throw new NotFoundException("Post not found");
        }
        Comment entity = new Comment();
        entity.name = request.name();
        entity.email = request.email();
        entity.content = request.content();
        entity.post = post;
        entity.persist();
        return Response.created(URI.create("/comments/" + entity.uuid))
                .entity(commentMapper.toDTO(entity))
                .build();
    }

    @DELETE
    @Path("/{uuid}")
    @Transactional
    public Response delete(@PathParam("uuid") UUID uuid) {
        Comment entity = Comment.find("uuid", uuid).firstResult();
        if (entity == null) {
            throw new NotFoundException();
        }
        entity.softDelete(null);
        entity.persist();
        return Response.noContent().build();
    }
}
