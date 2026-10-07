package com.integration.testframework.client.config;

import org.aeonbits.owner.Config;

/**
 * Externalized environment configuration, loaded from
 * {@code src/test/resources/config/config.properties} (and overridable via
 * -D system properties or environment variables, per Owner's default resolution order).
 */
@Config.Sources({
        "system:properties",
        "system:env",
        "classpath:config/config.properties"
})
public interface EnvironmentConfig extends Config {

    @Key("jira.base.url")
    @DefaultValue("https://jira.example.com")
    String jiraBaseUrl();

    @Key("jira.api.token")
    @DefaultValue("dummy-jira-token")
    String jiraApiToken();

    @Key("codebeamer.base.url")
    @DefaultValue("https://codebeamer.example.com")
    String codebeamerBaseUrl();

    @Key("codebeamer.api.token")
    @DefaultValue("dummy-codebeamer-token")
    String codebeamerApiToken();

    @Key("sync.poll.timeout.seconds")
    @DefaultValue("60")
    long syncPollTimeoutSeconds();

    @Key("sync.poll.interval.seconds")
    @DefaultValue("3")
    long syncPollIntervalSeconds();
}
