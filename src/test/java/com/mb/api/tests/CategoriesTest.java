package com.mb.api.tests;

import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static com.mb.api.tests.utils.ValidationPatterns.CATEGORY_NAME;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CategoriesTest extends BaseTest{

    private List<Map<String,Object>> categories;
    private static final String ENDPOINT = "/categories";
    private static final String ID = "id";
    private static final String NAME = "name";

    @BeforeClass
    public void setUp() {
        categories = getListFromEndpoint(ENDPOINT);
    }
    @Test
    public void categoriesAreReturned() {
        assertThat("No categories returned", categories.size(), greaterThan(0));
        Reporter.log("Number of categories returned: " + categories.size(), true);
    }
    @Test
    public void categoriesSchemaMatchesJsonSchema() {
        getResponseFromEndpoint(ENDPOINT)
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("schemas/CategoriesSchema.json"));
    }
    @Test
    public void requiredKeysArePresent() {
        for (int i = 0; i < categories.size(); i++) {
            Map<String, Object> cat = categories.get(i);
            assertThat("Category at index " + i + " missing key: " + ID, cat, hasKey(ID));
            assertThat("Category at index " + i + " missing key: " + NAME, cat, hasKey(NAME));
        }
    }
    @Test
    public void idsAreUnique() {
        List<Integer> ids = categories.stream()
                .map(c -> ((Number) c.get(ID)).intValue())
                .toList();

        List<Integer> duplicates = findDuplicates(ids);
        assertThat("Duplicate IDs found: " + duplicates, duplicates.isEmpty(), is(true));
    }

    @Test
    public void namesAreUnique() {
        List<String> names = categories.stream()
                .map(c -> (String) c.get(NAME))
                .toList();

        List<String> duplicates = findDuplicates(names);
        assertThat("Duplicate names found: " + duplicates, duplicates.isEmpty(), is(true));
    }
    @Test
    public void namesFollowPattern() {
        for (int i = 0; i < categories.size(); i++) {
            Map<String, Object> cat = categories.get(i);
            String name = (String) cat.get(NAME);
            assertThat("Invalid name format at index " + i + ": " + name, name, matchesPattern(CATEGORY_NAME));
        }
    }
    @Test
    public void logExtraFields() {
        for (int i = 0; i < categories.size(); i++) {
            Map<String, Object> cat = categories.get(i);
            if (cat.size() > 2) { // more than ID and NAME
                Reporter.log("Category at index " + i + " has extra fields: " + cat.keySet(), true);
            }
        }
    }
}
