package com.secretlabs.versioned_kv_store.repository;

import com.secretlabs.versioned_kv_store.entity.RecordEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface RecordRepository extends JpaRepository<RecordEntity , Long> {
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT kr FROM RecordEntity kr WHERE kr.keyName = :keyName")
        Optional<RecordEntity> findByKeyNameForUpdate(@Param("keyName")  String keyName);
        Optional<RecordEntity> findByKeyName(String keyName);

}
