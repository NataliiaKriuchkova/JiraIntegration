package com.integration.testframework.client;

import com.integration.testframework.client.config.EnvironmentConfig;
import com.integration.testframework.model.jira.JiraIssue;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * Stateless HTTP client for the Jira REST API.
 * <p>
 * Each method builds the request from a shared, immutable {@link RequestSpecification}
 * and returns either the raw {@link Response} (for status/header assertions) or a
 * deserialized model (for business-level assertions). No mutable instance state is
 * held between calls, so a single client instance is safe to reuse across tests.
 */
public class JiraApiClient {

    private final RequestSpecification requestSpec;

    public JiraApiClient(EnvironmentConfig config) {
        this.requestSpec = new RequestSpecBuilder()
                .setBaseUri(config.jiraBaseUrl())
                .setContentType(ContentType.JSON)
                .addHeader("Authorization", "Bearer " + config.jiraApiToken())
                .addFilter(new AllureRestAssured())
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    /**
     * Fetches a single Jira issue by its key (e.g. "PROJ-123") and deserializes it.
     */
    public JiraIssue getIssue(String issueKey) {
        return given()
                .spec(requestSpec)
                .pathParam("issueKey", issueKey)
                .when()
                .get("/rest/api/2/issue/{issueKey}")
                .then()
                .statusCode(200)
                .extract()
                .as(JiraIssue.class);
    }

    /**
     * Creates a new Jira issue from the given payload and returns the raw response
     * so callers can assert on status code/headers before extracting the body.
     */
    public Response createIssue(Object issuePayload) {
        return given()
                .spec(requestSpec)
                .body(issuePayload)
                .when()
                .post("/rest/api/2/issue")
                .then()
                .extract()
                .response();
    }

    /**
     * Transitions an issue's status (e.g. "In Progress" -> "Done"), used to trigger
     * a downstream sync event in the middleware under test.
     */
    public Response transitionIssue(String issueKey, String transitionId) {
        String body = """
                { "transition": { "id": "%s" } }
                """.formatted(transitionId);

        return given()
                .spec(requestSpec)
                .pathParam("issueKey", issueKey)
                .body(body)
                .when()
                .post("/rest/api/2/issue/{issueKey}/transitions")
                .then()
                .extract()
                .response();
    }
}
