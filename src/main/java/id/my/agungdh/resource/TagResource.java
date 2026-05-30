package id.my.agungdh.resource;

import id.my.agungdh.dto.PagedResponse;
import id.my.agungdh.dto.TagDTO;
import id.my.agungdh.dto.TagRequest;
import id.my.agungdh.entity.Tag;
import id.my.agungdh.mapper.TagMapper;
import id.my.agungdh.util.SlugUtils;
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
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

@Path("/tags")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TagResource {

    @Inject
    TagMapper tagMapper;

    @GET
    public PagedResponse<TagDTO> getAll(@QueryParam("page") @DefaultValue("0") int page,
                                        @QueryParam("size") @DefaultValue("20") int size) {
        PanacheQuery<Tag> query = Tag.findAll();
        var list = query.page(Page.of(page, size)).list();
        return new PagedResponse<>(tagMapper.toDTOs(list), query.count(), page, size);
    }

    @GET
    @Path("/{uuid}")
    public TagDTO getByUuid(@PathParam("uuid") UUID uuid) {
        Tag entity = Tag.find("uuid", uuid).firstResult();
        if (entity == null) {
            throw new NotFoundException();
        }
        return tagMapper.toDTO(entity);
    }

    @POST
    @Transactional
    public Response create(@Valid TagRequest request) {
        Tag entity = new Tag();
        entity.name = request.name();
        entity.slug = resolveSlug(request.slug(), request.name());
        entity.persist();
        return Response.created(URI.create("/tags/" + entity.uuid))
                .entity(tagMapper.toDTO(entity))
                .build();
    }

    @PUT
    @Path("/{uuid}")
    @Transactional
    public TagDTO update(@PathParam("uuid") UUID uuid, @Valid TagRequest request) {
        Tag entity = Tag.find("uuid", uuid).firstResult();
        if (entity == null) {
            throw new NotFoundException();
        }
        entity.name = request.name();
        entity.slug = resolveSlug(request.slug(), request.name());
        entity.persist();
        return tagMapper.toDTO(entity);
    }

    @DELETE
    @Path("/{uuid}")
    @Transactional
    public Response delete(@PathParam("uuid") UUID uuid) {
        Tag entity = Tag.find("uuid", uuid).firstResult();
        if (entity == null) {
            throw new NotFoundException();
        }
        entity.softDelete(null);
        entity.persist();
        return Response.noContent().build();
    }

    private String resolveSlug(String slug, String name) {
        if (slug != null && !slug.isBlank()) {
            return slug;
        }
        return SlugUtils.toSlug(name);
    }
}
