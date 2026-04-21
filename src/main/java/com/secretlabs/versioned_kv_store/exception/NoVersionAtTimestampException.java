package com.secretlabs.versioned_kv_store.exception;

public class NoVersionAtTimestampException extends RuntimeException{
    public NoVersionAtTimestampException(String keyName , Long timestamp) {
        super("No version of key '" + keyName + "' existed at timestamp " + timestamp);
    }
}
