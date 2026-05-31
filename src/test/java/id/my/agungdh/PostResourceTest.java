package id.my.agungdh;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class PostResourceTest extends BaseResourceTest {

    private String createCategory() {
        return given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Cat for Post\",\"slug\":\"cat-post-" + UUID.randomUUID().toString().substring(0, 8) + "\"}")
                .when().post("/categories")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .extract().path("uuid");
    }

    private String createTag() {
        return given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Tag for Post\",\"slug\":\"tag-post-" + UUID.randomUUID().toString().substring(0, 8) + "\"}")
                .when().post("/tags")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .extract().path("uuid");
    }

    @Test
    void testGetAll() {
        given()
                .when().get("/posts")
                .then()
                .statusCode(200)
                .body("data", notNullValue())
                .body("total", notNullValue())
                .body("page", notNullValue())
                .body("size", notNullValue());
    }

    @Test
    void testGetAllPagination() {
        given()
                .when().get("/posts?page=0&size=5")
                .then()
                .statusCode(200)
                .body("page", is(0))
                .body("size", is(5));
    }

    @Test
    void testGetAllFilterByCategorySlug() {
        String categoryUuid = createCategory();
        String catSlug = given().when().get("/categories/" + categoryUuid)
                .then().extract().path("slug");

        given()
                .when().get("/posts?categorySlug=" + catSlug)
                .then()
                .statusCode(200)
                .body("data", notNullValue());

        given().when().delete("/categories/" + categoryUuid);
    }

    @Test
    void testGetAllFilterByTagSlug() {
        String tagUuid = createTag();
        String tagSlug = given().when().get("/tags/" + tagUuid)
                .then().extract().path("slug");

        given()
                .when().get("/posts?tagSlug=" + tagSlug)
                .then()
                .statusCode(200)
                .body("data", notNullValue());

        given().when().delete("/tags/" + tagUuid);
    }

    @Test
    void testGetAllFilterByCategoryAndTagSlug() {
        String categoryUuid = createCategory();
        String catSlug = given().when().get("/categories/" + categoryUuid)
                .then().extract().path("slug");

        String tagUuid = createTag();
        String tagSlug = given().when().get("/tags/" + tagUuid)
                .then().extract().path("slug");

        given()
                .when().get("/posts?categorySlug=" + catSlug + "&tagSlug=" + tagSlug)
                .then()
                .statusCode(200)
                .body("data", notNullValue());

        given().when().delete("/categories/" + categoryUuid);
        given().when().delete("/tags/" + tagUuid);
    }

    @Test
    void testCreate() {
        String categoryUuid = createCategory();
        String slug = "post-" + UUID.randomUUID().toString().substring(0, 8);

        String uuid = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Test Post\",\"slug\":\"" + slug + "\",\"content\":\"Post content\",\"categoryUuid\":\"" + categoryUuid + "\"}")
                .when().post("/posts")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body("uuid", notNullValue())
                .body("title", is("Test Post"))
                .body("slug", is(slug))
                .body("content", is("Post content"))
                .extract().path("uuid");

        given().when().delete("/posts/" + uuid);
        given().when().delete("/categories/" + categoryUuid);
    }

    @Test
    void testCreateAutoSlug() {
        String categoryUuid = createCategory();
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String title = "My Awesome Post " + suffix;
        String expectedSlug = "my-awesome-post-" + suffix;

        String uuid = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"" + title + "\",\"content\":\"Content\",\"categoryUuid\":\"" + categoryUuid + "\"}")
                .when().post("/posts")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body("uuid", notNullValue())
                .body("title", is(title))
                .body("slug", is(expectedSlug))
                .extract().path("uuid");

        given().when().delete("/posts/" + uuid);
        given().when().delete("/categories/" + categoryUuid);
    }

    @Test
    void testCreateWithTags() {
        String categoryUuid = createCategory();
        String tagUuid = createTag();
        String slug = "post-" + UUID.randomUUID().toString().substring(0, 8);

        String uuid = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Post With Tags\",\"slug\":\"" + slug + "\",\"content\":\"Content\",\"categoryUuid\":\"" + categoryUuid + "\",\"tagUuids\":[\"" + tagUuid + "\"]}")
                .when().post("/posts")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body("uuid", notNullValue())
                .body("title", is("Post With Tags"))
                .body("tags.size()", is(1))
                .extract().path("uuid");

        given().when().delete("/posts/" + uuid);
        given().when().delete("/tags/" + tagUuid);
        given().when().delete("/categories/" + categoryUuid);
    }

    @Test
    void testCreateValidationError() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"\"}")
                .when().post("/posts")
                .then()
                .statusCode(400);
    }

    @Test
    void testCreateCategoryNotFound() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Test\",\"categoryUuid\":\"" + UUID.randomUUID() + "\"}")
                .when().post("/posts")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testCreateTagNotFound() {
        String categoryUuid = createCategory();

        given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Test\",\"categoryUuid\":\"" + categoryUuid + "\",\"tagUuids\":[\"" + UUID.randomUUID() + "\"]}")
                .when().post("/posts")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given().when().delete("/categories/" + categoryUuid);
    }

    @Test
    void testGetByUuid() {
        String categoryUuid = createCategory();
        String slug = "post-" + UUID.randomUUID().toString().substring(0, 8);

        String uuid = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Get Post\",\"slug\":\"" + slug + "\",\"content\":\"Content\",\"categoryUuid\":\"" + categoryUuid + "\"}")
                .when().post("/posts")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .extract().path("uuid");

        given()
                .when().get("/posts/" + uuid)
                .then()
                .statusCode(200)
                .body("uuid", is(uuid))
                .body("title", is("Get Post"))
                .body("slug", is(slug));

        given().when().delete("/posts/" + uuid);
        given().when().delete("/categories/" + categoryUuid);
    }

    @Test
    void testGetByUuidNotFound() {
        given()
                .when().get("/posts/" + UUID.randomUUID())
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testUpdate() {
        String categoryUuid = createCategory();
        String slug1 = "post-" + UUID.randomUUID().toString().substring(0, 8);
        String slug2 = "post-" + UUID.randomUUID().toString().substring(0, 8);

        String uuid = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Before Update\",\"slug\":\"" + slug1 + "\",\"content\":\"Old\",\"categoryUuid\":\"" + categoryUuid + "\"}")
                .when().post("/posts")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .extract().path("uuid");

        given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"After Update\",\"slug\":\"" + slug2 + "\",\"content\":\"New\",\"categoryUuid\":\"" + categoryUuid + "\"}")
                .when().put("/posts/" + uuid)
                .then()
                .statusCode(200)
                .body("uuid", is(uuid))
                .body("title", is("After Update"))
                .body("slug", is(slug2))
                .body("content", is("New"));

        given().when().delete("/posts/" + uuid);
        given().when().delete("/categories/" + categoryUuid);
    }

    @Test
    void testUpdateAutoSlug() {
        String categoryUuid = createCategory();
        String slug1 = "post-" + UUID.randomUUID().toString().substring(0, 8);
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String title = "After Update Auto " + suffix;
        String expectedSlug = "after-update-auto-" + suffix;

        String uuid = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Before Update\",\"slug\":\"" + slug1 + "\",\"content\":\"Old\",\"categoryUuid\":\"" + categoryUuid + "\"}")
                .when().post("/posts")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .extract().path("uuid");

        given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"" + title + "\",\"content\":\"New\",\"categoryUuid\":\"" + categoryUuid + "\"}")
                .when().put("/posts/" + uuid)
                .then()
                .statusCode(200)
                .body("uuid", is(uuid))
                .body("title", is(title))
                .body("slug", is(expectedSlug));

        given().when().delete("/posts/" + uuid);
        given().when().delete("/categories/" + categoryUuid);
    }

    @Test
    void testUpdateNotFound() {
        String categoryUuid = createCategory();

        given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"No Exist\",\"categoryUuid\":\"" + categoryUuid + "\"}")
                .when().put("/posts/" + UUID.randomUUID())
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given().when().delete("/categories/" + categoryUuid);
    }

    @Test
    void testDelete() {
        String categoryUuid = createCategory();
        String slug = "post-" + UUID.randomUUID().toString().substring(0, 8);

        String uuid = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"To Delete\",\"slug\":\"" + slug + "\",\"content\":\"Content\",\"categoryUuid\":\"" + categoryUuid + "\"}")
                .when().post("/posts")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .extract().path("uuid");

        given()
                .when().delete("/posts/" + uuid)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given()
                .when().get("/posts/" + uuid)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given().when().delete("/categories/" + categoryUuid);
    }

    @Test
    void testDeleteNotFound() {
        given()
                .when().delete("/posts/" + UUID.randomUUID())
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}
