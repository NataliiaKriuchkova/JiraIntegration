package com.integration.testframework.tests;

import com.integration.testframework.client.CodebeamerApiClient;
import com.integration.testframework.client.JiraApiClient;
import com.integration.testframework.client.config.EnvironmentConfig;
import com.integration.testframework.service.IntegrationSyncFacade;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.BeforeAll;

/**
 * Shared test fixture wiring: loads environment config once per test class and
 * exposes ready-to-use clients/facade to concrete test classes. Kept deliberately
 * free of JUnit lifecycle surprises (no shared mutable state across test methods).
 */
public abstract class BaseIntegrationTest {

    protected static EnvironmentConfig config;
    protected static JiraApiClient jiraClient;
    protected static CodebeamerApiClient codebeamerClient;
    protected static IntegrationSyncFacade syncFacade;

    @BeforeAll
    static void setUpClients() {
        config = ConfigFactory.create(EnvironmentConfig.class);
        jiraClient = new JiraApiClient(config);
        codebeamerClient = new CodebeamerApiClient(config);
        syncFacade = new IntegrationSyncFacade(jiraClient, codebeamerClient, config);
    }
}
