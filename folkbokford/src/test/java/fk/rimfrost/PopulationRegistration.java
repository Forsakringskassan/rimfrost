package fk.rimfrost;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class FullmaktsControllerTest
{
   @Test
   void test()
   {
      String actualResponse = given()
            .when().get("/population_registration/1234")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .asString();

      assertThat(actualResponse).isEqualToIgnoringWhitespace("""
            {"result":true}
            """);
   }

}
