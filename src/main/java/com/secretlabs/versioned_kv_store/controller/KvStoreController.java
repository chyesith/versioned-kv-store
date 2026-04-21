package com.secretlabs.versioned_kv_store.controller;

import com.secretlabs.versioned_kv_store.dto.KvStoreRequest;
import com.secretlabs.versioned_kv_store.dto.KvStoreResponse;
import com.secretlabs.versioned_kv_store.service.KvStoreService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/kvstore/v1")
public class KvStoreController {

    private final KvStoreService kvStoreService;

    public KvStoreController(KvStoreService kvStoreService) {
        this.kvStoreService = kvStoreService;
    }


    @PostMapping
    public ResponseEntity<KvStoreResponse> upsert(
            @Valid @RequestBody KvStoreRequest request) {
        log.debug("POST /object key='{}'", request.key());
        KvStoreResponse response = kvStoreService.upsert(
                request.key(), request.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{key}")
    public ResponseEntity<KvStoreResponse> get(
            @PathVariable String key,
            @RequestParam(required = false) Long timestamp) {
        log.debug("GET /object/{} timestamp={}", key, timestamp);
        KvStoreResponse response = (timestamp != null)
                ? kvStoreService.getAtTimestamp(key, timestamp)
                : kvStoreService.getLatest(key);
        return ResponseEntity.ok(response);
    }

}
