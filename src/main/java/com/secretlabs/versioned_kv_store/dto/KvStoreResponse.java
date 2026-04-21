package com.secretlabs.versioned_kv_store.dto;

import com.secretlabs.versioned_kv_store.entity.RecordVersion;

public record KvStoreResponse  (
        String key,
        String value,
        Integer version,
        Long createdAt
)  {
    public static KvStoreResponse from(String keyName, RecordVersion recordVersion) {
        return new KvStoreResponse(
                keyName,
                recordVersion.getRecordValue(),
                recordVersion.getVersion(),
                recordVersion.getCreatedAt()
        );
    }
}
