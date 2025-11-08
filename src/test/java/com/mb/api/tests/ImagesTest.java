package com.mb.api.tests;

import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static com.mb.api.tests.utils.ValidationPatterns.IMAGE_URL;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ImagesTest extends BaseTest{
    private List<Map<String,Object>> images;
    private static final int DEFAULT_LIMIT = 5;
    private static final int MULTI_LIMIT = 3;
    private static final String MIME_TYPES = "jpg,png";

    private List<Map<String, Object>> getImages(Map<String, Object> queryParams) {
        return given()
                .spec(requestSpecification)
                .queryParams(queryParams) // supports multiple params
                .when()
                .get("/images/search")
                .then()
                .spec(responseSpecification)
                .extract()
                .jsonPath()
                .getList("$");
    }
    private void assertImageSchema(Map<String, Object> image, int index) {
        assertThat("Image at index " + index + " missing id", image, hasKey("id"));
        assertThat("Image at index " + index + " missing url", image, hasKey("url"));
        assertThat("Image at index " + index + " missing width", image, hasKey("width"));
        assertThat("Image at index " + index + " missing height", image, hasKey("height"));

        assertThat("Invalid id type at index " + index, image.get("id"), instanceOf(String.class));
        assertThat("Invalid url type at index " + index, image.get("url"), instanceOf(String.class));
        assertThat("Invalid width type at index " + index, image.get("width"), instanceOf(Number.class));
        assertThat("Invalid height type at index " + index, image.get("height"), instanceOf(Number.class));
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
        images = getImages(defaultParams);
    }
    @Test
    public void imagesAreReturned() {
        assertThat("No images returned", images.size(), greaterThan(0));
        Reporter.log("Number of images returned: " + images.size(), true);
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
    public void imagesWithMultipleQueryParams() {
        Map<String, Object> params = Map.of(
                "limit", MULTI_LIMIT,
                "mime_types", MIME_TYPES
        );

        List<Map<String, Object>> result = getImages(params);

        assertThat("Images not returned", result.size(), greaterThan(0));
        result.forEach(img -> {
            String url = (String) img.get("url");
            assertThat("Invalid URL: " + url, url, matchesPattern(IMAGE_URL));
        });
    }
}
