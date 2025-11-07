package com.mb.api.tests;
import com.fasterxml.jackson.databind.JsonNode;
import com.mb.api.tests.utils.TestDataLoader;
import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static com.mb.api.tests.utils.ValidationPatterns.WEIGHT_RANGE;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BreedsTest extends BaseTest {

    private List<Map<String, Object>> breeds;

    private static List<String> BREEDS_REQUIRED_KEYS;
    private static List<String> BREEDS_OPTIONAL_KEYS;
    private static final List<String> KNOWN_BREEDS = List.of("Abyssinian", "Bengal", "Siberian");

    @BeforeClass
    public void setUp() throws IOException {

        JsonNode breedKeys = TestDataLoader.loadJson("breedKeys.json");

        BREEDS_REQUIRED_KEYS = new ArrayList<>();
        BREEDS_OPTIONAL_KEYS = new ArrayList<>();

        breedKeys.get("required").forEach(node -> BREEDS_REQUIRED_KEYS.add(node.asText()));
        breedKeys.get("optional").forEach(node -> BREEDS_OPTIONAL_KEYS.add(node.asText()));

        breeds = given()
                .spec(requestSpecification)
        .when()
                .get("/breeds")
        .then()
                .spec(responseSpecification)
                .extract()
                .jsonPath()
                .getList("$");
    }

    @Test
    public void breedsAreReturned() {
        assertThat("No breeds returned", breeds.size(), greaterThan(0));
    }

    @Test
    public void requiredKeysArePresent() {
        for (int i = 0; i < breeds.size(); i++) {
            Map<String, Object> breed = breeds.get(i);
            for (String key : BREEDS_REQUIRED_KEYS) {
                assertThat("Breed at index " + i + " is missing required key: " + key,
                        breed, hasKey(key));
            }
            assertThat("Breed at index " + i + " has null name", breed.get("name"), notNullValue());
        }
    }

    @Test
    public void optionalKeysAreLogged() {
        for (int i = 0; i < breeds.size(); i++) {
            Map<String, Object> breed = breeds.get(i);
            for (String key : BREEDS_OPTIONAL_KEYS) {
                if (!breed.containsKey(key)) {
                    Reporter.log("Optional key '" + key + "' missing for breed at index " + i, true);
                }
            }
        }
    }

    @Test
    public void weightFormatIsValid() {

        IntStream.range(0, breeds.size()).forEach(i -> {
            Map<String, Object> breed = breeds.get(i);
            @SuppressWarnings("unchecked")
            Map<String, Object> weight = (Map<String, Object>) breed.get("weight");

            assertThat("Breed at index " + i + " missing weight", weight, notNullValue());
            assertThat("Breed at index " + i + " missing imperial weight", weight, hasKey("imperial"));
            assertThat("Breed at index " + i + " missing metric weight", weight, hasKey("metric"));

            String imperial = (String) weight.get("imperial");
            String metric = (String) weight.get("metric");

            assertThat("Invalid imperial format for breed " + i, imperial, matchesPattern(WEIGHT_RANGE));
            assertThat("Invalid metric format for breed " + i, metric, matchesPattern(WEIGHT_RANGE));
        });
    }

    @Test
    public void knownBreedsArePresent() {

        List<String> breedNames = breeds.stream()
                .map(b -> (String) b.get("name"))
                .toList();

        assertThat("Some known breeds are missing", breedNames, hasItems(KNOWN_BREEDS.toArray(new String[0])));
    }
}


