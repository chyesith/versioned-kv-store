package com.secretlabs.versioned_kv_store.dto;

public record ErrorResponse(
        int status,
        String error,
        String message
) {
}
