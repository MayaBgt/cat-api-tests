package com.mb.api.tests;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.BeforeClass;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BaseTest {

    protected RequestSpecification requestSpecification;
    protected ResponseSpecification responseSpecification;
    protected static final List<String> BREEDS_REQUIRED_KEYS = List.of(
            "id", "name", "weight", "temperament", "origin",
            "country_codes", "country_code", "description",
            "life_span", "adaptability", "affection_level",
            "child_friendly", "dog_friendly", "energy_level",
            "grooming", "health_issues", "intelligence",
            "shedding_level", "social_needs", "stranger_friendly",
            "vocalisation", "experimental", "hairless",
            "natural", "rare", "hypoallergenic"
    );
    protected static final List<String> BREEDS_OPTIONAL_KEYS = List.of(
            "cfa_url", "vetstreet_url", "vcahospitals_url", "lap",
            "alt_names", "rex", "suppressed_tail", "short_legs",
            "wikipedia_url", "reference_image_id"
    );
    protected static final List<String> KNOWN_BREEDS = List.of("Abyssinian", "Bengal", "Siberian");

    @BeforeClass
    public void beforeClass() {

        RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();
        requestSpecBuilder.
                setBaseUri("https://api.thecatapi.com/v1").
                log(LogDetail.ALL);
        requestSpecification = requestSpecBuilder.build();

        responseSpecification = RestAssured.expect().
                statusCode(200).
                contentType(ContentType.JSON);
    }
    protected <T> List<T> findDuplicates(List<T> list) {
        return list.stream()
                .filter(e -> Collections.frequency(list, e) > 1)
                .distinct()
                .toList();
    }
}
