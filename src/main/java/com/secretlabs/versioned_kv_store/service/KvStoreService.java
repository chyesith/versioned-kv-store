package com.secretlabs.versioned_kv_store.service;

import com.secretlabs.versioned_kv_store.dto.KvStoreResponse;

import java.util.List;

public interface KvStoreService {

    KvStoreResponse upsert(String keyName, String value);

    KvStoreResponse getLatest(String keyName);

    KvStoreResponse getAtTimestamp(String keyName, Long timestamp);

}
