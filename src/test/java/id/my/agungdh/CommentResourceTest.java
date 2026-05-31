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
class CommentResourceTest extends BaseResourceTest {

    private String createCategory() {
        return given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Cat Comment\",\"slug\":\"cat-comment-" + UUID.randomUUID().toString().substring(0, 8) + "\"}")
                .when().post("/categories")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .extract().path("uuid");
    }

    private String createPost(String categoryUuid) {
        return given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Post for Comment\",\"slug\":\"post-comment-" + UUID.randomUUID().toString().substring(0, 8) + "\",\"content\":\"Content\",\"categoryUuid\":\"" + categoryUuid + "\"}")
                .when().post("/posts")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .extract().path("uuid");
    }

    @Test
    void testGetAll() {
        String categoryUuid = createCategory();
        String postUuid = createPost(categoryUuid);

        given()
                .when().get("/posts/" + postUuid + "/comments")
                .then()
                .statusCode(200)
                .body("data", notNullValue())
                .body("total", notNullValue())
                .body("page", notNullValue())
                .body("size", notNullValue());

        given().when().delete("/posts/" + postUuid);
        given().when().delete("/categories/" + categoryUuid);
    }

    @Test
    void testGetAllPagination() {
        String categoryUuid = createCategory();
        String postUuid = createPost(categoryUuid);

        given()
                .when().get("/posts/" + postUuid + "/comments?page=0&size=5")
                .then()
                .statusCode(200)
                .body("page", is(0))
                .body("size", is(5));

        given().when().delete("/posts/" + postUuid);
        given().when().delete("/categories/" + categoryUuid);
    }

    @Test
    void testCreate() {
        String categoryUuid = createCategory();
        String postUuid = createPost(categoryUuid);

        String uuid = given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Commenter\",\"email\":\"commenter@test.com\",\"content\":\"Nice post!\"}")
                .when().post("/posts/" + postUuid + "/comments")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body("uuid", notNullValue())
                .body("name", is("Commenter"))
                .body("email", is("commenter@test.com"))
                .body("content", is("Nice post!"))
                .extract().path("uuid");

        given()
                .when().delete("/posts/" + postUuid + "/comments/" + uuid)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given().when().delete("/posts/" + postUuid);
        given().when().delete("/categories/" + categoryUuid);
    }

    @Test
    void testCreateWithoutEmail() {
        String categoryUuid = createCategory();
        String postUuid = createPost(categoryUuid);

        String uuid = given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Anonymous\",\"content\":\"Comment without email\"}")
                .when().post("/posts/" + postUuid + "/comments")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body("uuid", notNullValue())
                .body("name", is("Anonymous"))
                .body("content", is("Comment without email"))
                .extract().path("uuid");

        given()
                .when().delete("/posts/" + postUuid + "/comments/" + uuid)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given().when().delete("/posts/" + postUuid);
        given().when().delete("/categories/" + categoryUuid);
    }

    @Test
    void testCreateValidationError() {
        String categoryUuid = createCategory();
        String postUuid = createPost(categoryUuid);

        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"\",\"content\":\"\"}")
                .when().post("/posts/" + postUuid + "/comments")
                .then()
                .statusCode(400);

        given().when().delete("/posts/" + postUuid);
        given().when().delete("/categories/" + categoryUuid);
    }

    @Test
    void testCreatePostNotFound() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Commenter\",\"content\":\"Test\"}")
                .when().post("/posts/" + UUID.randomUUID() + "/comments")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testDelete() {
        String categoryUuid = createCategory();
        String postUuid = createPost(categoryUuid);

        String uuid = given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"To Delete\",\"content\":\"Will be deleted\"}")
                .when().post("/posts/" + postUuid + "/comments")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .extract().path("uuid");

        given()
                .when().delete("/posts/" + postUuid + "/comments/" + uuid)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given()
                .when().get("/posts/" + postUuid + "/comments")
                .then()
                .statusCode(200)
                .body("data.find { it.uuid == '" + uuid + "' }", is((Object) null));

        given().when().delete("/posts/" + postUuid);
        given().when().delete("/categories/" + categoryUuid);
    }

    @Test
    void testDeleteNotFound() {
        String categoryUuid = createCategory();
        String postUuid = createPost(categoryUuid);

        given()
                .when().delete("/posts/" + postUuid + "/comments/" + UUID.randomUUID())
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given().when().delete("/posts/" + postUuid);
        given().when().delete("/categories/" + categoryUuid);
    }
}
