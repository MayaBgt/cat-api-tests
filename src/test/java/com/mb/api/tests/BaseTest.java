package com.mb.api.tests;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.LogConfig;
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

    @BeforeClass
    public void beforeClass() {

        String apiKey = System.getenv("CAT_API_KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("CAT_API_KEY environment variable not set");
        }

        requestSpecification = new RequestSpecBuilder()
                .setBaseUri("https://api.thecatapi.com/v1")
                .addHeader("x-api-key", apiKey)
                .setContentType(ContentType.JSON)
                .log(LogDetail.URI)
                .log(LogDetail.METHOD)
                .log(LogDetail.BODY)
                .setConfig(RestAssured.config()
                        .logConfig(LogConfig.logConfig()
                                .enableLoggingOfRequestAndResponseIfValidationFails()
                                .blacklistHeader("x-api-key")))
                .build();

        responseSpecification = RestAssured.expect()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }
    protected <T> List<T> findDuplicates(List<T> list) {
        return list.stream()
                .filter(e -> Collections.frequency(list, e) > 1)
                .distinct()
                .toList();
    }
}
