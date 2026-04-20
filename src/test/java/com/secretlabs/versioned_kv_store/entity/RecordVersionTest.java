package com.secretlabs.versioned_kv_store.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("RecordVersion entity")
public class RecordVersionTest {
    @Nested
    @DisplayName("recordVersionFactory()")
    class recordVersion {
        @Test
        @DisplayName("should add record, version, and recordValue correctly")
        void shouldFactoryCreateVersionCorrectly() {
            RecordEntity entity = RecordEntity.createNew("key");
            RecordVersion version = RecordVersion.of(entity, 1, "value");

            assertThat(version.getRecordEntity()).isEqualTo(entity);
            assertThat(version.getVersion()).isEqualTo(1);
            assertThat(version.getRecordValue()).isEqualTo("value");
        }
    }
}
