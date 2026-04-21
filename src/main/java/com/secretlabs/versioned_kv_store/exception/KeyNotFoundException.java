package com.secretlabs.versioned_kv_store.exception;

public class KeyNotFoundException extends RuntimeException {

    public KeyNotFoundException(String keyName) {
        super("key not found:" + keyName);
    }
}
