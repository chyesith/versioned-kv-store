package com.secretlabs.versioned_kv_store.audit;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

import java.time.Instant;

@MappedSuperclass
public abstract class Auditable {

    @Column(name = "created_at" , nullable = false , updatable = false)
    private Long createdAt;

    @PrePersist
    protected void onPersist() {
        this.createdAt = Instant.now().getEpochSecond();
    }

    public Long getCreatedAt() {
        return createdAt;
    }
}
