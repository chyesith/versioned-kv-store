package com.secretlabs.versioned_kv_store.dto;

import jakarta.validation.constraints.NotBlank;

public record KvStoreRequest(
        @NotBlank(message = "key can not be blank")
        String key

        , @NotBlank(message = "Value can not be blank")
        String value
) {
}
