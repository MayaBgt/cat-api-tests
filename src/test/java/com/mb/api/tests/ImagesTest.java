package com.mb.api.tests;

import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ImagesTest extends BaseTest{
    private List<Map<String,Object>> images;

    @BeforeClass
    public void setUp() {
        images = given()
                .spec(requestSpecification)
                .queryParam("limit", 5)
        .when()
                .get("/images/search")
        .then()
                .spec(responseSpecification)
                .extract()
                .jsonPath()
                .getList("$");
    }

    @Test
    public void imagesAreReturned() {
        assertThat("No images returned", images.size(), greaterThan(0));
        Reporter.log("Number of images returned: " + images.size(), true);
    }

    @Test
    public void schemaIsValid() {
        for (int i = 0; i < images.size(); i++) {
            Map<String, Object> image = images.get(i);

            assertThat("Image at index " + i + " missing id", image, hasKey("id"));
            assertThat("Image at index " + i + " missing url", image, hasKey("url"));
            assertThat("Image at index " + i + " missing width", image, hasKey("width"));
            assertThat("Image at index " + i + " missing height", image, hasKey("height"));

            assertThat("Invalid id type at index " + i, image.get("id"), instanceOf(String.class));
            assertThat("Invalid url type at index " + i, image.get("url"), instanceOf(String.class));
            assertThat("Invalid width type at index " + i, image.get("width"), instanceOf(Number.class));
            assertThat("Invalid height type at index " + i, image.get("height"), instanceOf(Number.class));
        }
    }

    @Test
    public void urlsAreValid() {
        Pattern urlPattern = Pattern.compile("^https://cdn2\\.thecatapi\\.com/images/[\\w-]+\\.(jpg|png|gif)$");
        for (Map<String, Object> image : images) {
            String url = (String) image.get("url");
            assertThat("Invalid image URL format: " + url, url, matchesPattern(urlPattern));
        }
    }

    @Test
    public void idsAreUnique() {
        List<String> ids = images.stream()
                .map(i -> (String) i.get("id"))
                .toList();

        List<String> duplicates = findDuplicates(ids);
        assertThat("Duplicate image IDs found: " + duplicates, duplicates.isEmpty(), is(true));
    }

    @Test
    public void atLeastLimitImagesReturned() {
        assertThat("Fewer images than requested", images.size(), greaterThanOrEqualTo(5));
    }

    @Test
    public void logExtraFields() {
        for (int i = 0; i < images.size(); i++) {
            Map<String, Object> image = images.get(i);
            if (image.containsKey("breeds") || image.containsKey("categories")) {
                Reporter.log("Image at index " + i + " has extra fields: " + image.keySet(), true);
            }
        }
    }
}
