package com.secretlabs.versioned_kv_store.controller;

import com.secretlabs.versioned_kv_store.dto.KvStoreRequest;
import com.secretlabs.versioned_kv_store.dto.KvStoreResponse;
import com.secretlabs.versioned_kv_store.service.KvStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Key-Value Store", description = "Version-controlled key-value store API")
@Slf4j
@RestController
@RequestMapping("/kvstore/v1")
public class KvStoreController {

    private final KvStoreService kvStoreService;

    public KvStoreController(KvStoreService kvStoreService) {
        this.kvStoreService = kvStoreService;
    }


    @Operation(summary = "Create or update a key",
            description = "Creates key at version 1 if new, increments version if exists")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Successfully written"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<KvStoreResponse> upsert(
            @Valid @RequestBody KvStoreRequest request) {
        log.debug("POST /object key='{}'", request.key());
        KvStoreResponse response = kvStoreService.upsert(
                request.key(), request.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get value by key",
            description = "Returns latest version, or version at timestamp if provided")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Key found"),
            @ApiResponse(responseCode = "404", description = "Key not found")
    })
    @GetMapping("/{key}")
    public ResponseEntity<KvStoreResponse> get(
            @PathVariable String key,
            @Parameter(description = "UNIX timestamp (epoch seconds UTC)")
            @Positive(message = "Timestamp must be a positive number")
            @RequestParam(required = false) Long timestamp) {
        log.debug("GET /object/{} timestamp={}", key, timestamp);
        KvStoreResponse response = (timestamp != null)
                ? kvStoreService.getAtTimestamp(key, timestamp)
                : kvStoreService.getLatest(key);
        return ResponseEntity.ok(response);
    }

}
