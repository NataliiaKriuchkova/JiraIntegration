package com.integration.testframework.client;

import com.integration.testframework.client.config.EnvironmentConfig;
import com.integration.testframework.model.codebeamer.CodebeamerRequirement;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;

/**
 * Stateless HTTP client for the Codebeamer tracker-item REST API. Codebeamer
 * natively speaks XML for tracker items, so this client sends/receives XML
 * bodies while JUnit/Allure continue to see strongly-typed Java models.
 */
public class CodebeamerApiClient {

    private final RequestSpecification requestSpec;

    public CodebeamerApiClient(EnvironmentConfig config) {
        this.requestSpec = new RequestSpecBuilder()
                .setBaseUri(config.codebeamerBaseUrl())
                .setContentType(ContentType.XML)
                .setAccept(ContentType.XML)
                .addHeader("Authorization", "Bearer " + config.codebeamerApiToken())
                .addFilter(new AllureRestAssured())
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    /**
     * Fetches a tracker item by numeric id, deserializing the XML response
     * directly into a {@link CodebeamerRequirement}.
     */
    public CodebeamerRequirement getItem(long itemId) {
        return given()
                .spec(requestSpec)
                .pathParam("itemId", itemId)
                .when()
                .get("/rest/item/{itemId}")
                .then()
                .statusCode(200)
                .extract()
                .as(CodebeamerRequirement.class);
    }

    /**
     * Queries tracker items by external correlation id (the field used to link
     * a Codebeamer item back to the Jira issue that produced it).
     */
    public List<CodebeamerRequirement> findByExternalId(String externalId) {
        CodebeamerRequirement[] results = given()
                .spec(requestSpec)
                .queryParam("externalId", externalId)
                .when()
                .get("/rest/item/search")
                .then()
                .statusCode(200)
                .extract()
                .as(CodebeamerRequirement[].class);

        return Arrays.asList(results);
    }

    /**
     * Creates a tracker item from a raw XML body (typically produced by
     * {@code JiraToCodebeamerTransformer}) and returns the raw response.
     */
    public Response createItem(String xmlBody) {
        return given()
                .spec(requestSpec)
                .body(xmlBody)
                .when()
                .post("/rest/item")
                .then()
                .extract()
                .response();
    }
}
