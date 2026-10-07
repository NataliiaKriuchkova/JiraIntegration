package com.integration.testframework.service;

import com.integration.testframework.client.CodebeamerApiClient;
import com.integration.testframework.client.JiraApiClient;
import com.integration.testframework.client.config.EnvironmentConfig;
import com.integration.testframework.model.codebeamer.CodebeamerRequirement;
import com.integration.testframework.model.jira.JiraIssue;
import io.qameta.allure.Step;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Business-level orchestration layer for the Jira -&gt; Codebeamer synchronization
 * workflow. Tests should depend on this facade rather than calling
 * {@link JiraApiClient}/{@link CodebeamerApiClient} directly, keeping polling logic
 * and multi-step workflows out of the test methods themselves.
 */
public class IntegrationSyncFacade {

    private final JiraApiClient jiraClient;
    private final CodebeamerApiClient codebeamerClient;
    private final EnvironmentConfig config;

    public IntegrationSyncFacade(JiraApiClient jiraClient,
                                  CodebeamerApiClient codebeamerClient,
                                  EnvironmentConfig config) {
        this.jiraClient = jiraClient;
        this.codebeamerClient = codebeamerClient;
        this.config = config;
    }

    /**
     * Transitions a Jira issue to trigger the middleware's outbound sync, then
     * polls Codebeamer until the corresponding item materializes (or the
     * configured timeout elapses). No {@code Thread.sleep} is used: Awaitility
     * re-evaluates the condition on its own schedule and fails fast with a
     * descriptive timeout exception otherwise.
     *
     * @return the synchronized Codebeamer requirement once it appears.
     */
    @Step("Trigger Jira transition and await Codebeamer sync for issue {issueKey}")
    public CodebeamerRequirement triggerSyncAndAwaitCodebeamerItem(String issueKey, String transitionId) {
        jiraClient.transitionIssue(issueKey, transitionId);
        return awaitSyncedItem(issueKey);
    }

    /**
     * Polls Codebeamer for an item correlated to {@code jiraIssueKey} until exactly
     * one match is found, using the externalId correlation field set during transformation.
     */
    @Step("Await Codebeamer item synced from Jira issue {jiraIssueKey}")
    public CodebeamerRequirement awaitSyncedItem(String jiraIssueKey) {
        try {
            return Awaitility.await("codebeamer-sync:" + jiraIssueKey)
                    .atMost(Duration.ofSeconds(config.syncPollTimeoutSeconds()))
                    .pollInterval(Duration.ofSeconds(config.syncPollIntervalSeconds()))
                    .ignoreExceptions()
                    .until(() -> findSingleMatch(jiraIssueKey), Optional::isPresent)
                    .orElseThrow();
        } catch (ConditionTimeoutException e) {
            throw new AssertionError(
                    "Codebeamer item for Jira issue '%s' did not appear within %ds"
                            .formatted(jiraIssueKey, config.syncPollTimeoutSeconds()), e);
        }
    }

    /**
     * Fetches the live Jira issue and the already-synced Codebeamer item together,
     * for tests that need to diff source vs. destination field-by-field.
     */
    public SyncedPair fetchSyncedPair(String jiraIssueKey) {
        JiraIssue jiraIssue = jiraClient.getIssue(jiraIssueKey);
        CodebeamerRequirement codebeamerItem = awaitSyncedItem(jiraIssueKey);
        return new SyncedPair(jiraIssue, codebeamerItem);
    }

    private Optional<CodebeamerRequirement> findSingleMatch(String jiraIssueKey) {
        List<CodebeamerRequirement> matches = codebeamerClient.findByExternalId(jiraIssueKey);
        return matches.size() == 1 ? Optional.of(matches.get(0)) : Optional.empty();
    }

    /** Pairs a Jira source record with its synchronized Codebeamer counterpart. */
    public record SyncedPair(JiraIssue jiraIssue, CodebeamerRequirement codebeamerItem) {
    }
}
