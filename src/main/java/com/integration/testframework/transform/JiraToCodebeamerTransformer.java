package com.integration.testframework.transform;

import com.integration.testframework.model.codebeamer.CodebeamerRequirement;
import com.integration.testframework.model.jira.JiraIssue;
import com.integration.testframework.util.MapperFactory;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Optional;

/**
 * Pure data-transformation layer: maps a Jira issue (JSON model) into the
 * Codebeamer requirement shape (XML model), mirroring the field mapping an
 * integration middleware (OpsHub/MuleSoft) would apply.
 * <p>
 * This class has no I/O side effects, which makes it independently unit-testable
 * without any live Jira/Codebeamer environment.
 */
public final class JiraToCodebeamerTransformer {

    private JiraToCodebeamerTransformer() {
    }

    /**
     * Maps a {@link JiraIssue} to its corresponding {@link CodebeamerRequirement},
     * applying the same field-level business rules the real middleware would use
     * (status normalization, assignee flattening, correlation-id stamping).
     */
    public static CodebeamerRequirement toCodebeamerRequirement(JiraIssue jiraIssue) {
        JiraIssue.Fields fields = jiraIssue.getFields();

        String assignee = Optional.ofNullable(fields.getAssignee())
                .map(JiraIssue.User::getDisplayName)
                .orElse("Unassigned");

        String normalizedStatus = normalizeStatus(
                Optional.ofNullable(fields.getStatus())
                        .map(JiraIssue.Status::getName)
                        .orElse("Unknown"));

        return CodebeamerRequirement.builder()
                .name(fields.getSummary())
                .description(fields.getDescription())
                .status(normalizedStatus)
                .priority(Optional.ofNullable(fields.getPriority())
                        .map(JiraIssue.Priority::getName)
                        .orElse("Medium"))
                .assignedTo(assignee)
                .externalId(jiraIssue.getKey())
                .labels(Optional.ofNullable(fields.getLabels()).orElse(List.of()))
                .build();
    }

    /**
     * Maps Jira's workflow status names onto Codebeamer's status vocabulary.
     * In a real middleware this mapping is typically configuration-driven;
     * here it's hardcoded for test-data determinism.
     */
    private static String normalizeStatus(String jiraStatus) {
        return switch (jiraStatus.toLowerCase()) {
            case "to do", "open" -> "New";
            case "in progress" -> "In Progress";
            case "done", "closed" -> "Resolved";
            default -> "Unknown";
        };
    }

    /**
     * Serializes a {@link CodebeamerRequirement} to the XML payload Codebeamer's
     * REST API expects on create/update calls.
     */
    public static String toXml(CodebeamerRequirement requirement) {
        try {
            return MapperFactory.xml().writeValueAsString(requirement);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize CodebeamerRequirement to XML", e);
        }
    }

    /**
     * Convenience one-shot: Jira issue -> Codebeamer XML payload.
     */
    public static String jiraIssueToXmlPayload(JiraIssue jiraIssue) {
        return toXml(toCodebeamerRequirement(jiraIssue));
    }
}
