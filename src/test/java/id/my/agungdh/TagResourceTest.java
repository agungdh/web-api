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
class TagResourceTest extends BaseResourceTest {

    @Test
    void testGetAll() {
        given()
                .when().get("/tags")
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
                .when().get("/tags?page=0&size=5")
                .then()
                .statusCode(200)
                .body("page", is(0))
                .body("size", is(5));
    }

    @Test
    void testCreate() {
        String slug = "tag-" + UUID.randomUUID().toString().substring(0, 8);

        String uuid = given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Test Tag\",\"slug\":\"" + slug + "\"}")
                .when().post("/tags")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body("uuid", notNullValue())
                .body("name", is("Test Tag"))
                .body("slug", is(slug))
                .extract().path("uuid");

        given()
                .when().delete("/tags/" + uuid)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void testCreateAutoSlug() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String name = "My Awesome Tag " + suffix;
        String expectedSlug = "my-awesome-tag-" + suffix;

        String uuid = given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"" + name + "\"}")
                .when().post("/tags")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body("uuid", notNullValue())
                .body("name", is(name))
                .body("slug", is(expectedSlug))
                .extract().path("uuid");

        given()
                .when().delete("/tags/" + uuid)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void testCreateValidationError() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"\"}")
                .when().post("/tags")
                .then()
                .statusCode(400);
    }

    @Test
    void testGetByUuid() {
        String slug = "tag-" + UUID.randomUUID().toString().substring(0, 8);

        String uuid = given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Get Tag\",\"slug\":\"" + slug + "\"}")
                .when().post("/tags")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .extract().path("uuid");

        given()
                .when().get("/tags/" + uuid)
                .then()
                .statusCode(200)
                .body("uuid", is(uuid))
                .body("name", is("Get Tag"))
                .body("slug", is(slug));

        given()
                .when().delete("/tags/" + uuid)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void testGetByUuidNotFound() {
        given()
                .when().get("/tags/" + UUID.randomUUID())
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testUpdate() {
        String slug1 = "tag-" + UUID.randomUUID().toString().substring(0, 8);
        String slug2 = "tag-" + UUID.randomUUID().toString().substring(0, 8);

        String uuid = given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Before Update\",\"slug\":\"" + slug1 + "\"}")
                .when().post("/tags")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .extract().path("uuid");

        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"After Update\",\"slug\":\"" + slug2 + "\"}")
                .when().put("/tags/" + uuid)
                .then()
                .statusCode(200)
                .body("uuid", is(uuid))
                .body("name", is("After Update"))
                .body("slug", is(slug2));

        given()
                .when().delete("/tags/" + uuid)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void testUpdateAutoSlug() {
        String slug1 = "tag-" + UUID.randomUUID().toString().substring(0, 8);
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String name = "After Update Auto " + suffix;
        String expectedSlug = "after-update-auto-" + suffix;

        String uuid = given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Before Update\",\"slug\":\"" + slug1 + "\"}")
                .when().post("/tags")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .extract().path("uuid");

        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"" + name + "\"}")
                .when().put("/tags/" + uuid)
                .then()
                .statusCode(200)
                .body("uuid", is(uuid))
                .body("name", is(name))
                .body("slug", is(expectedSlug));

        given()
                .when().delete("/tags/" + uuid)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void testUpdateNotFound() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"No Exist\"}")
                .when().put("/tags/" + UUID.randomUUID())
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testDelete() {
        String slug = "tag-" + UUID.randomUUID().toString().substring(0, 8);

        String uuid = given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"To Delete\",\"slug\":\"" + slug + "\"}")
                .when().post("/tags")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .extract().path("uuid");

        given()
                .when().delete("/tags/" + uuid)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given()
                .when().get("/tags/" + uuid)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testDeleteNotFound() {
        given()
                .when().delete("/tags/" + UUID.randomUUID())
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}
