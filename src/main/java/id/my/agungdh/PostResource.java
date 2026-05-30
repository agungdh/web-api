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
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/posts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PostResource {

    @Inject
    PostMapper postMapper;

    @GET
    public PagedResponse<PostDTO> getAll(@QueryParam("page") @DefaultValue("0") int page,
                                         @QueryParam("size") @DefaultValue("20") int size,
                                         @QueryParam("categorySlug") String categorySlug,
                                         @QueryParam("tagSlug") String tagSlug) {
        Map<String, Object> params = new HashMap<>();
        String query;

        boolean hasCategory = categorySlug != null && !categorySlug.isBlank();
        boolean hasTag = tagSlug != null && !tagSlug.isBlank();

        if (hasCategory && hasTag) {
            query = "SELECT DISTINCT p FROM Post p JOIN p.category c JOIN p.tags t WHERE c.slug = :categorySlug AND t.slug = :tagSlug";
            params.put("categorySlug", categorySlug);
            params.put("tagSlug", tagSlug);
        } else if (hasCategory) {
            query = "SELECT p FROM Post p JOIN p.category c WHERE c.slug = :categorySlug";
            params.put("categorySlug", categorySlug);
        } else if (hasTag) {
            query = "SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE t.slug = :tagSlug";
            params.put("tagSlug", tagSlug);
        } else {
            query = "FROM Post";
        }

        PanacheQuery<Post> panacheQuery = Post.find(query, params);
        var list = panacheQuery.page(Page.of(page, size)).list();
        return new PagedResponse<>(postMapper.toDTOs(list), panacheQuery.count(), page, size);
    }

    @GET
    @Path("/{uuid}")
    public PostDTO getByUuid(@PathParam("uuid") UUID uuid) {
        Post entity = Post.find("uuid", uuid).firstResult();
        if (entity == null) {
            throw new NotFoundException();
        }
        return postMapper.toDTO(entity);
    }

    @POST
    @Transactional
    public Response create(@Valid PostRequest request) {
        Post entity = new Post();
        entity.title = request.title();
        entity.slug = resolveSlug(request.slug(), request.title());
        entity.content = request.content();
        entity.category = resolveCategory(request.categoryUuid());
        entity.tags = resolveTags(request.tagUuids());
        entity.persist();
        return Response.created(URI.create("/posts/" + entity.uuid))
                .entity(postMapper.toDTO(entity))
                .build();
    }

    @PUT
    @Path("/{uuid}")
    @Transactional
    public PostDTO update(@PathParam("uuid") UUID uuid, @Valid PostRequest request) {
        Post entity = Post.find("uuid", uuid).firstResult();
        if (entity == null) {
            throw new NotFoundException();
        }
        entity.title = request.title();
        entity.slug = resolveSlug(request.slug(), request.title());
        entity.content = request.content();
        entity.category = resolveCategory(request.categoryUuid());
        entity.tags = resolveTags(request.tagUuids());
        entity.persist();
        return postMapper.toDTO(entity);
    }

    @DELETE
    @Path("/{uuid}")
    @Transactional
    public Response delete(@PathParam("uuid") UUID uuid) {
        Post entity = Post.find("uuid", uuid).firstResult();
        if (entity == null) {
            throw new NotFoundException();
        }
        entity.softDelete(null);
        entity.persist();
        return Response.noContent().build();
    }

    private String resolveSlug(String slug, String title) {
        if (slug != null && !slug.isBlank()) {
            return slug;
        }
        return SlugUtils.toSlug(title);
    }

    private Category resolveCategory(UUID categoryUuid) {
        if (categoryUuid == null) {
            return null;
        }
        Category category = Category.find("uuid", categoryUuid).firstResult();
        if (category == null) {
            throw new NotFoundException("Category not found");
        }
        return category;
    }

    private java.util.Set<Tag> resolveTags(List<UUID> tagUuids) {
        if (tagUuids == null || tagUuids.isEmpty()) {
            return new java.util.HashSet<>();
        }
        var tags = new java.util.HashSet<Tag>(Tag.list("uuid in ?1", tagUuids));
        if (tags.size() != tagUuids.size()) {
            throw new NotFoundException("One or more tags not found");
        }
        return tags;
    }
}
