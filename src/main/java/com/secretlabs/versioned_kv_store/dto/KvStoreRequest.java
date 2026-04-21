package com.secretlabs.versioned_kv_store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KvStoreRequest(
        @NotBlank(message = "key can not be blank")
        @Size(max = 512, message = "key must not exceed 512 characters")
        String key

        , @NotBlank(message = "Value can not be blank")
        @Size(max = 65536, message = "Value must not exceed 64KB")
        String value
) {
}
