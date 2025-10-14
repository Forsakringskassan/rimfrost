package fk.rimfrost;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class PopulationRegistrationTest
{
   @Test
   void testPopulationRegistrationTrue()
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

    @Test
    void testPopulationRegistrationFalse()
    {
        String actualResponse = given()
                .when().get("/population_registration/19999")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        assertThat(actualResponse).isEqualToIgnoringWhitespace("""
            {"result":false}
            """);
    }

}
