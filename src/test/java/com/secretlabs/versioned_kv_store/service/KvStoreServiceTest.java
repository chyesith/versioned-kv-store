package com.secretlabs.versioned_kv_store.service;

import com.secretlabs.versioned_kv_store.dto.KvStoreResponse;
import com.secretlabs.versioned_kv_store.entity.RecordEntity;
import com.secretlabs.versioned_kv_store.entity.RecordVersion;
import com.secretlabs.versioned_kv_store.exception.KeyNotFoundException;
import com.secretlabs.versioned_kv_store.exception.NoVersionAtTimestampException;
import com.secretlabs.versioned_kv_store.repository.RecordRepository;
import com.secretlabs.versioned_kv_store.repository.RecordVersionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KvStoreServiceImpl unit tests")
public class KvStoreServiceTest {
    @Mock
    private RecordRepository recordRepository;

    @Mock
    private RecordVersionRepository recordVersionRepository;

    @InjectMocks
    private KvStoreServiceImpl kvStoreService;


    @Nested
    @DisplayName("upsert()")
    class Upsert {

        @Test
        @DisplayName("new key — creates at version 1")
        void newKeyCreatesAtVersionOne() {
            RecordEntity savedEntity = RecordEntity.createNew("mykey", "v1");
            RecordVersion savedVersion = RecordVersion.of(savedEntity, 1, "v1");

            when(recordRepository.findByKeyNameForUpdate("mykey"))
                    .thenReturn(Optional.empty());
            when(recordRepository.saveAndFlush(any(RecordEntity.class)))
                    .thenReturn(savedEntity);
            when(recordVersionRepository.findLatestRecordById(any()))
                    .thenReturn(Optional.empty());
            when(recordVersionRepository.save(any(RecordVersion.class)))
                    .thenReturn(savedVersion);

            KvStoreResponse response = kvStoreService.upsert("mykey", "v1");

            assertThat(response.version()).isEqualTo(1);
            assertThat(response.key()).isEqualTo("mykey");
            verify(recordRepository, times(1)).saveAndFlush(any(RecordEntity.class));
            verify(recordVersionRepository, times(1)).save(any(RecordVersion.class));
        }

        @Test
        @DisplayName("existing key — increments version")
        void existingKeyIncrementsVersion() {
            RecordEntity existingEntity = RecordEntity.createNew("mykey", "v1");
            RecordVersion existingVersion = RecordVersion.of(existingEntity, 1, "v1");
            RecordVersion newVersion = RecordVersion.of(existingEntity, 2, "v2");

            when(recordRepository.findByKeyNameForUpdate("mykey"))
                    .thenReturn(Optional.of(existingEntity));
            when(recordVersionRepository.findLatestRecordById(any()))
                    .thenReturn(Optional.of(existingVersion));
            when(recordRepository.save(any(RecordEntity.class)))
                    .thenReturn(existingEntity);
            when(recordVersionRepository.save(any(RecordVersion.class)))
                    .thenReturn(newVersion);

            KvStoreResponse response = kvStoreService.upsert("mykey", "v2");

            assertThat(response.version()).isEqualTo(2);
            verify(recordRepository, times(1)).save(existingEntity);
        }
    }


    @Nested
    @DisplayName("getLatest()")
    class GetLatest {

        @Test
        @DisplayName("should return latest version of existing key")
        void returnsLatestVersion() {
            RecordEntity entity = RecordEntity.createNew("mykey", "v2");
            RecordVersion version = RecordVersion.of(entity, 2, "v2");

            when(recordRepository.findByKeyName("mykey"))
                    .thenReturn(Optional.of(entity));
            when(recordVersionRepository.findLatestRecordById(any()))
                    .thenReturn(Optional.of(version));

            KvStoreResponse response = kvStoreService.getLatest("mykey");

            assertThat(response.key()).isEqualTo("mykey");
            assertThat(response.version()).isEqualTo(2);
            assertThat(response.value()).isEqualTo("v2");
        }

        @Test
        @DisplayName("should throw KeyNotFoundException for unknown key")
        void throwsForUnknownKey() {
            when(recordRepository.findByKeyName("ghost"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> kvStoreService.getLatest("ghost"))
                    .isInstanceOf(KeyNotFoundException.class)
                    .hasMessageContaining("ghost");
        }
    }


    @Nested
    @DisplayName("getAtTimestamp()")
    class GetAtTimestamp {

        @Test
        @DisplayName("should return version that existed at timestamp")
        void returnsVersionAtTimestamp() {
            RecordEntity entity = RecordEntity.createNew("mykey", "v1");
            RecordVersion version = RecordVersion.of(entity, 1, "v1");

            when(recordRepository.findByKeyName("mykey"))
                    .thenReturn(Optional.of(entity));
            when(recordVersionRepository.findByRecordIdAtTimestamp(any(), eq(1000L)))
                    .thenReturn(Optional.of(version));

            KvStoreResponse response = kvStoreService.getAtTimestamp("mykey", 1000L);

            assertThat(response.version()).isEqualTo(1);
            assertThat(response.value()).isEqualTo("v1");
        }

        @Test
        @DisplayName("should throw KeyNotFoundException when key does not exist")
        void throwsWhenKeyMissing() {
            when(recordRepository.findByKeyName("ghost"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> kvStoreService.getAtTimestamp("ghost", 1000L))
                    .isInstanceOf(KeyNotFoundException.class)
                    .hasMessageContaining("ghost");
        }

        @Test
        @DisplayName("should throw NoVersionAtTimestampException when no version at time")
        void throwsWhenNoVersionAtTimestamp() {
            RecordEntity entity = RecordEntity.createNew("mykey", "v1");

            when(recordRepository.findByKeyName("mykey"))
                    .thenReturn(Optional.of(entity));
            when(recordVersionRepository.findByRecordIdAtTimestamp(any(), eq(0L)))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> kvStoreService.getAtTimestamp("mykey", 0L))
                    .isInstanceOf(NoVersionAtTimestampException.class)
                    .hasMessageContaining("mykey")
                    .hasMessageContaining("0");
        }
    }

    @Nested
    @DisplayName("getAllLatest()")
    class GetAllLatest {

        @Test
        @DisplayName("should return list of KvStoreResponse for all latest versions")
        void returnsAllLatestVersions() {

            RecordEntity entity1 = RecordEntity.createNew("key1", "value1");
            RecordVersion version1 = RecordVersion.of(entity1, 1, "value1");

            RecordEntity entity2 = RecordEntity.createNew("key2", "value2");
            RecordVersion version2 = RecordVersion.of(entity2, 2, "value2");

            when(recordVersionRepository.findAllLatestVersions())
                    .thenReturn(List.of(version1, version2));

            List<KvStoreResponse> responses = kvStoreService.getAllLatest();

            assertThat(responses).hasSize(2);

            assertThat(responses.get(0).key()).isEqualTo("key1");
            assertThat(responses.get(0).value()).isEqualTo("value1");
            assertThat(responses.get(0).version()).isEqualTo(1);

            assertThat(responses.get(1).key()).isEqualTo("key2");
            assertThat(responses.get(1).value()).isEqualTo("value2");
            assertThat(responses.get(1).version()).isEqualTo(2);


            verify(recordVersionRepository, times(1)).findAllLatestVersions();
        }

        @Test
        @DisplayName("should return empty list when no records exist")
        void returnsEmptyListWhenNoRecords() {
            when(recordVersionRepository.findAllLatestVersions())
                    .thenReturn(List.of());

            List<KvStoreResponse> responses = kvStoreService.getAllLatest();

            assertThat(responses).isEmpty();
        }
    }
}
