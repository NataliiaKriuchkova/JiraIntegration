package com.integration.testframework.tests;

import com.integration.testframework.model.codebeamer.CodebeamerRequirement;
import com.integration.testframework.model.jira.JiraIssue;
import com.integration.testframework.service.IntegrationSyncFacade;
import com.integration.testframework.transform.JiraToCodebeamerTransformer;
import com.integration.testframework.validation.XmlSchemaValidator;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end integration test validating that a Jira issue transition is
 * correctly propagated to Codebeamer by the integration middleware.
 * <p>
 * Covers: triggering the sync, Awaitility-based polling (no Thread.sleep),
 * JSON -&gt; XML field transformation correctness, and XSD schema compliance
 * of the resulting Codebeamer payload.
 */
@Epic("Jira-Codebeamer Integration")
@Feature("Synchronization")
class JiraCodebeamerSyncTest extends BaseIntegrationTest {

    private static final String XSD_PATH = "schema/codebeamer-item.xsd";

    @Test
    @Story("Status transition sync")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Jira issue transition to 'Done' syncs to a Resolved Codebeamer item")
    @Description("Transitions a Jira issue, polls Codebeamer until the correlated item "
            + "appears, then asserts field-level parity between source and destination.")
    void jiraDoneTransitionSyncsToResolvedCodebeamerItem() {
        String issueKey = "PROJ-101";
        String doneTransitionId = "31";

        CodebeamerRequirement syncedItem =
                syncFacade.triggerSyncAndAwaitCodebeamerItem(issueKey, doneTransitionId);

        assertEquals(issueKey, syncedItem.getExternalId(),
                "Synced item should be correlated back to the originating Jira issue");
        assertEquals("Resolved", syncedItem.getStatus(),
                "Jira 'Done' status should map to Codebeamer 'Resolved'");
    }

    @Test
    @Story("Field mapping correctness")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Jira issue fields are correctly transformed into the Codebeamer XML payload")
    void jiraIssueTransformsIntoValidCodebeamerXmlPayload() {
        JiraIssue sourceIssue = JiraIssue.builder()
                .id("10001")
                .key("PROJ-202")
                .fields(JiraIssue.Fields.builder()
                        .summary("Add OAuth2 support to login flow")
                        .description("Allow users to authenticate via OAuth2 providers.")
                        .status(JiraIssue.Status.builder().name("In Progress").build())
                        .priority(JiraIssue.Priority.builder().name("High").build())
                        .assignee(JiraIssue.User.builder()
                                .displayName("Nataliia Kriuchkova")
                                .emailAddress("natalyshimko@gmail.com")
                                .build())
                        .labels(List.of("auth", "backend"))
                        .build())
                .build();

        CodebeamerRequirement mapped = JiraToCodebeamerTransformer.toCodebeamerRequirement(sourceIssue);

        assertEquals("Add OAuth2 support to login flow", mapped.getName());
        assertEquals("In Progress", mapped.getStatus());
        assertEquals("High", mapped.getPriority());
        assertEquals("Nataliia Kriuchkova", mapped.getAssignedTo());
        assertEquals("PROJ-202", mapped.getExternalId());

        String xmlPayload = JiraToCodebeamerTransformer.toXml(mapped);
        List<String> violations = XmlSchemaValidator.validate(xmlPayload, XSD_PATH);

        assertTrue(violations.isEmpty(),
                "Generated XML payload must satisfy the Codebeamer item XSD: " + violations);
    }

    @Test
    @Story("Source/destination parity")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Synced Codebeamer item preserves source Jira field values")
    void syncedPairPreservesFieldValues() {
        String issueKey = "PROJ-303";

        IntegrationSyncFacade.SyncedPair pair = syncFacade.fetchSyncedPair(issueKey);

        assertEquals(pair.jiraIssue().getFields().getSummary(), pair.codebeamerItem().getName(),
                "Summary should carry over as the Codebeamer item name");
        assertEquals(issueKey, pair.codebeamerItem().getExternalId());
    }
}
