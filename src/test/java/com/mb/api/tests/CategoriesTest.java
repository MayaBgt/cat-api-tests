package com.mb.api.tests;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CategoriesTest extends BaseTest{

    private List<Map<String,Object>> categories;

    @BeforeClass
    public void setUp() {
        categories = given()
                .spec(requestSpecification)
        .when()
                .get("/categories")
        .then()
                .spec(responseSpecification)
                .extract()
                .jsonPath()
                .getList("$");
    }

    @Test
    public void categoriesAreReturned() {
        assertThat("No categories returned", categories.size(), greaterThan(0));
    }

    @Test
    public void schemaIsValid() {

        for (int i=0; i<categories.size();i++) {
            Map<String, Object> category = categories.get(i);

            assertThat("Category at index " + i + " is missing id", category, hasKey("id"));
            assertThat("Category at index " + i + " is missing name", category, hasKey("name"));

            assertThat("ID type mismatch at index " + i, category.get("id"), instanceOf(Number.class));
            assertThat("Name type mismatch at index " + i, category.get("name"), instanceOf(String.class));
        }
    }

    @Test
    public void idsAreUnique() {
        List<Integer> ids = categories.stream()
                .map(c -> ((Number) c.get("id")).intValue())
                .toList();

        List<Integer> duplicates = findDuplicates(ids);
        assertThat("Duplicate IDs found: " + duplicates, duplicates.isEmpty(), is(true));
    }

    @Test
    public void namesAreUnique() {
        List<String> names = categories.stream()
                .map(c -> (String) c.get("name"))
                .toList();

        List<String> duplicates = findDuplicates(names);
        assertThat("Duplicate names found: " + duplicates, duplicates.isEmpty(), is(true));
    }

    @Test
    public void namesFollowPattern() {
        for (Map<String, Object> category : categories) {
            String name = (String) category.get("name");
            assertThat("Invalid name format: " + name, name, matchesPattern("^[a-z]+$"));
        }
    }
}
