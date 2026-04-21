package com.secretlabs.versioned_kv_store.service;

import com.secretlabs.versioned_kv_store.dto.KvStoreResponse;
import com.secretlabs.versioned_kv_store.entity.RecordEntity;
import com.secretlabs.versioned_kv_store.entity.RecordVersion;
import com.secretlabs.versioned_kv_store.exception.KeyNotFoundException;
import com.secretlabs.versioned_kv_store.exception.NoVersionAtTimestampException;
import com.secretlabs.versioned_kv_store.repository.RecordRepository;
import com.secretlabs.versioned_kv_store.repository.RecordVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class KvStoreServiceImpl implements KvStoreService {

    private final RecordRepository recordRepository;
    private final RecordVersionRepository recordVersionRepository;


    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public KvStoreResponse upsert(String keyName, String value) {
        log.info("Upserting key='{}'", keyName);
        RecordEntity recordEntity = recordRepository
                .findByKeyNameForUpdate(keyName)
                .orElseGet(() -> {
                    RecordEntity newRecord = RecordEntity.createNew(keyName , value);
                    return recordRepository.save(newRecord);
                });

        int version;
        boolean isFirstVersion = recordVersionRepository
                .findLatestRecordById(recordEntity.getId())
                .isEmpty();

        if (isFirstVersion) {
            version = 1;
        } else {
            version = recordEntity.incrementVersion();
            recordRepository.save(recordEntity);
        }

        RecordVersion recordVersion = RecordVersion.of(recordEntity, version, value);
        RecordVersion saved = recordVersionRepository.save(recordVersion);

        log.info("Saved key='{}' version={}", keyName, version);
        return KvStoreResponse.from(keyName, saved);
    }

    @Override
    @Transactional(readOnly = true)
    public KvStoreResponse getLatest(String keyName) {
        RecordEntity recordEntity = recordRepository
                .findByKeyName(keyName)
                .orElseThrow(() -> new KeyNotFoundException(keyName));

        RecordVersion recordVersion = recordVersionRepository
                .findLatestRecordById(recordEntity.getId())
                .orElseThrow(() -> new KeyNotFoundException(keyName));

        return KvStoreResponse.from(keyName, recordVersion);
    }

    @Override
    @Transactional(readOnly = true)
    public KvStoreResponse getAtTimestamp(String keyName, Long timestamp) {
        RecordEntity recordEntity = recordRepository
                .findByKeyName(keyName)
                .orElseThrow(() -> new KeyNotFoundException(keyName));

        RecordVersion recordVersion = recordVersionRepository
                .findByRecordIdAtTimestamp(recordEntity.getId(), timestamp)
                .orElseThrow(() ->
                        new NoVersionAtTimestampException(keyName, timestamp));

        return KvStoreResponse.from(keyName, recordVersion);
    }

}
