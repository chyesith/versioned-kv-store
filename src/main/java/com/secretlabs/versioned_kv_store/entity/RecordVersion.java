package com.secretlabs.versioned_kv_store.entity;

import com.secretlabs.versioned_kv_store.audit.Auditable;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "key_value_version",
        uniqueConstraints = {
        @UniqueConstraint(name = "uq_key_version", columnNames = {"record_id", "version"})
        },
        indexes = {@Index(name = "idx_kvv_key_created", columnList = "record_id, created_at DESC"),
                @Index(name = "idx_kvv_key_version", columnList = "record_id, version DESC")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class RecordVersion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "kv_seq")
    @SequenceGenerator(name = "kv_seq",
            sequenceName = "key_value_version_id_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "record_id" , nullable = false , updatable = false,
            foreignKey = @ForeignKey(name = "fk_kv_record"))
    private RecordEntity recordEntity;

    @Column(name = "version", updatable = false , nullable = false)
    private Integer version;

    @Column(name = "value" , updatable = false , nullable = false , columnDefinition = "TEXT")
    private String value;

    public static RecordVersion of(RecordEntity record , int version , String value ) {
        RecordVersion recordVersion = new RecordVersion();
        recordVersion.recordEntity = record;
        recordVersion.version = version;
        recordVersion.value = value;
        return recordVersion;
    }

}
