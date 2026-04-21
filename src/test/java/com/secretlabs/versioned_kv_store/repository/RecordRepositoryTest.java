package com.secretlabs.versioned_kv_store.repository;

import com.secretlabs.versioned_kv_store.entity.RecordEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.testcontainers.junit.jupiter.Testcontainers;



import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("RecordRepository Data Access")
public class RecordRepositoryTest {


    @Autowired
    private RecordRepository recordRepository;

    @Nested
    @DisplayName("findByKeyName()")
    class FindByKeyName {

        @Test
        @DisplayName("When give existing  key for searched, then return the entity")
        void shouldFindRecordByKeyName() {
            RecordEntity recordEntity = RecordEntity.createNew("test-key");
            recordEntity.setRecordValue("{\"status\": \"active\"}");
            recordRepository.save(recordEntity);

            Optional<RecordEntity> found = recordRepository.findByKeyName("test-key");

            assertThat(found).isPresent();
            assertThat(found.get().getKeyName()).isEqualTo("test-key");
        }

        @Test
        @DisplayName("When search with  non-existing key then return empty")
        void shouldReturnEmptyWhenKeyMissing() {
            Optional<RecordEntity> found = recordRepository.findByKeyName("missing-key");
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByKeyNameForUpdate()")
    class LockOperations {

        @Test
        @DisplayName("When  searched with key for update, then apply pessimistic lock")
        void shouldFindForUpdate() {
            RecordEntity recordEntity = RecordEntity.createNew("locked-key");
            recordEntity.setRecordValue("{}");
            recordRepository.save(recordEntity);

            Optional<RecordEntity> locked = recordRepository.findByKeyNameForUpdate("locked-key");

            assertThat(locked).isPresent();
            assertThat(locked.get().getKeyName()).isEqualTo("locked-key");
        }
    }
}
