package com.integration.testframework.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Immutable representation of a Jira issue payload (the "source" side of the
 * Jira -&gt; Codebeamer synchronization flow).
 * <p>
 * Mapped directly from Jira's REST API JSON shape:
 * {@code { "id": "...", "key": "...", "fields": { ... } } }.
 */
@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraIssue {

    @JsonProperty("id")
    String id;

    @JsonProperty("key")
    String key;

    @JsonProperty("fields")
    Fields fields;

    @Value
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fields {
        @JsonProperty("summary")
        String summary;

        @JsonProperty("description")
        String description;

        @JsonProperty("status")
        Status status;

        @JsonProperty("priority")
        Priority priority;

        @JsonProperty("assignee")
        User assignee;

        @JsonProperty("labels")
        List<String> labels;
    }

    @Value
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        @JsonProperty("name")
        String name;
    }

    @Value
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Priority {
        @JsonProperty("name")
        String name;
    }

    @Value
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        @JsonProperty("displayName")
        String displayName;

        @JsonProperty("emailAddress")
        String emailAddress;
    }
}
