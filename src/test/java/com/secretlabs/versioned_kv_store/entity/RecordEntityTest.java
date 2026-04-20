package com.secretlabs.versioned_kv_store.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("Record entity")
public class RecordEntityTest {

    @Nested
    @DisplayName("createNew()")
    class createNew {

        @Test
        @DisplayName("sets key and start version should be 1")
        void shouldSetFieldsCorrectly() {
            RecordEntity newRecord = RecordEntity.createNew("my-key");

            assertThat(newRecord.getKeyName()).isEqualTo("my-key");
            assertThat(newRecord.getCurrentVersion()).isEqualTo(1);
        }

    }

    @Nested
    @DisplayName("IncrementVersion()")
    class IncrementVersion {
        @Test
        @DisplayName("should return new version and update state")
        void shouldIncrementVersion() {
            RecordEntity newRecord = RecordEntity.createNew("my-key");

            int returned = newRecord.incrementVersion();

            assertThat(returned).isEqualTo(2);
            assertThat(newRecord.getCurrentVersion()).isEqualTo(2);
        }

        @Test
        @DisplayName("multiple increments are monotonically increasing")
        void multipleIncrements() {
            RecordEntity newRecord = RecordEntity.createNew("my-key");

            for (int i = 2; i <= 51; i++) {
                assertThat(newRecord.incrementVersion()).isEqualTo(i);
            }
            assertThat(newRecord.getCurrentVersion()).isEqualTo(51);
        }
    }

    @Nested
    @DisplayName("KeyValueVersion.of()")
    class Versions {
        @Test
        @DisplayName("should add record, version, and value correctly")
        void createsVersionCorrectly() {
            RecordEntity newRecord = RecordEntity.createNew("my-key");
            RecordVersion version = RecordVersion.of(newRecord, 1, "{\"data\":\"test\"}");

            assertThat(version.getRecordEntity()).isSameAs(newRecord);
            assertThat(version.getVersion()).isEqualTo(1);
            assertThat(version.getRecordValue()).isEqualTo("{\"data\":\"test\"}");
        }
    }
}
