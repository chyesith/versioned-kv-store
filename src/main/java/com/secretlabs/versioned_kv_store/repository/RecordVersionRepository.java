package com.secretlabs.versioned_kv_store.repository;

import com.secretlabs.versioned_kv_store.entity.RecordVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface RecordVersionRepository extends JpaRepository<RecordVersion , Long> {
    @Query("""
        SELECT v FROM RecordVersion v
        WHERE v.recordEntity.id = :recordId
        ORDER BY v.version DESC
        LIMIT 1
        """)
    Optional<RecordVersion>findLatestRecordById(@Param("recordId") Long recordId);

    @Query("""
        SELECT v FROM RecordVersion v
        WHERE v.recordEntity.id = :recordId
          AND v.createdAt <= :timestamp
        ORDER BY v.createdAt DESC
        LIMIT 1
        """)
    Optional<RecordVersion> findByRecordIdAtTimestamp(
            @Param("recordId") Long recordId,
            @Param("timestamp") Long timestamp
    );

    @Query("""
        SELECT v FROM RecordVersion v
        WHERE v.version = (
            SELECT MAX(v2.version)
            FROM RecordVersion v2
            WHERE v2.recordEntity.id = v.recordEntity.id
        )
        ORDER BY v.recordEntity.keyName ASC
        """)
    List<RecordVersion> findAllLatestVersions();
}
