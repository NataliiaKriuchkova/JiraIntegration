package com.integration.testframework.model.codebeamer;

/**
 * Represents the lifecycle state of a middleware synchronization job
 * (e.g. OpsHub) as reported by its status/monitoring endpoint.
 */
public enum SyncStatus {
    PENDING,
    IN_PROGRESS,
    SYNCED,
    FAILED,
    UNKNOWN
}
