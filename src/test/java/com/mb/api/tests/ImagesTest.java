package com.mb.api.tests;

import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static com.mb.api.tests.utils.ValidationPatterns.IMAGE_URL;
import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ImagesTest extends BaseTest{
    private List<Map<String,Object>> images;
    private static final String ENDPOINT = "/images/search";
    private static final int DEFAULT_LIMIT = 5;
    private static final int MULTI_LIMIT = 3;
    private static final String MIME_TYPES = "jpg,png";
    private static final String ID = "id";
    private static final String URL = "url";
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final Map<String, Class<?>> IMAGE_FIELDS = Map.of(
            ID, String.class,
            URL, String.class,
            WIDTH, Number.class,
            HEIGHT, Number.class
    );

    private void assertImageSchema(Map<String, Object> image, int index) {
        IMAGE_FIELDS.forEach((field, type) -> {
            assertThat("Image at index " + index + " missing " + field, image, hasKey(field));
            assertThat("Invalid " + field + " type at index " + index, image.get(field), instanceOf(type));
        });
    }

    private void assertImageUrlValid(Map<String, Object> image) {
        String url = (String) image.get("url");
        assertThat("Invalid image URL: " + url, url, matchesPattern(IMAGE_URL));
    }
    private void assertUniqueField(List<Map<String, Object>> items, String fieldName) {
        List<String> values = items.stream()
                .map(i -> (String) i.get(fieldName))
                .toList();
        List<String> duplicates = findDuplicates(values);
        assertThat("Duplicate " + fieldName + " found: " + duplicates, duplicates.isEmpty(), is(true));
    }
    private void logExtraFields(Map<String, Object> image, int index) {
        if (image.containsKey("breeds") || image.containsKey("categories")) {
            Reporter.log("Image at index " + index + " has extra fields: " + image.keySet(), true);
        }
    }
    @BeforeClass
    public void setUp() {
        Map<String, Object> defaultParams = Map.of("limit", DEFAULT_LIMIT);
        images = getListFromEndpoint(ENDPOINT, defaultParams);
    }
    @Test
    public void imagesAreReturned() {
        assertThat("No images returned", images.size(), greaterThan(0));
        Reporter.log("Number of images returned: " + images.size(), true);
    }
    @Test
    public void imageSchemaMatchesJsonSchema() {
        Map<String, Object> params = Map.of("limit", 1);

        getResponseFromEndpoint(ENDPOINT, params)
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("schemas/ImageSchema.json"));
    }
    @Test
    public void schemaIsValid() {
        for (int i = 0; i < images.size(); i++) {
            assertImageSchema(images.get(i), i);
        }
    }
    @Test
    public void urlsAreValid() {

        images.forEach(this::assertImageUrlValid);
    }
    @Test
    public void idsAreUnique() {
        assertUniqueField(images, "id");
    }
    @Test
    public void atLeastLimitImagesReturned() {
        assertThat("Fewer images than requested", images.size(), greaterThanOrEqualTo(DEFAULT_LIMIT));
    }
    @Test
    public void logExtraFields() {
        for (int i = 0; i < images.size(); i++) {
            logExtraFields(images.get(i), i);
        }
    }
    @Test
    public void imagesWithMultipleQueryParamsAreValid() {
        Map<String, Object> params = Map.of(
                "limit", MULTI_LIMIT,
                "mime_types", MIME_TYPES
        );

        List<Map<String, Object>> result = getListFromEndpoint("/images/search", params);

        assertThat("Images not returned", result.size(), greaterThan(0));
        result.forEach(img -> {
            String url = (String) img.get("url");
            assertThat("Invalid URL: " + url, url, matchesPattern(IMAGE_URL));
        });
    }
}
