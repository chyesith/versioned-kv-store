package com.secretlabs.versioned_kv_store.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("Audit class")
class AuditableTest {
    static class TestAuditable extends Auditable {}

    @Test
    @DisplayName("onPersist() should set createdAt to current time")
    void shouldSetCreatedAtOnPersist() {
        TestAuditable entity = new TestAuditable();
        entity.onPersist();
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getCreatedAt()).isGreaterThan(0L);
    }

    @Test
    @DisplayName("getCreatedAt should return the value")
    void shouldGetCreatedAt() {
        TestAuditable entity = new TestAuditable();
        entity.onPersist();
        assertThat(entity.getCreatedAt()).isEqualTo(entity.getCreatedAt());
    }
}
