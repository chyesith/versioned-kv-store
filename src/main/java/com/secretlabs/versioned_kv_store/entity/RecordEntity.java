package com.secretlabs.versioned_kv_store.entity;

import com.secretlabs.versioned_kv_store.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "record" ,indexes = {
        @Index(name = "idx_record_key_name" ,columnList = "key_name", unique = true)
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "versions")
public class RecordEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE , generator = "record_seq")
    @SequenceGenerator(name = "record_seq" ,sequenceName = "record_id_seq" , allocationSize = 1)
    @Column(name = "record_id")
    private Long id;
    @Column(name = "key_name", nullable = false)
    private String keyName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "value", columnDefinition = "TEXT", nullable = false)
    private String recordValue;

    @Column(name = "current_version" , nullable = false)
    private Integer currentVersion;

    @OneToMany(mappedBy = "recordEntity" , fetch = FetchType.LAZY)
    @OrderBy("version ASC")
    private List<RecordVersion> versions = new ArrayList<>();

    public static RecordEntity createNew(String keyName) {
        RecordEntity recordEntity = new RecordEntity();
        recordEntity.keyName = keyName;
        recordEntity.currentVersion =1;
        return recordEntity;
    }

    public void updateValue(String newValue) {
        this.recordValue = newValue;
    }

    public int incrementVersion() {
        this.currentVersion ++;
        return this.currentVersion;
    }

}
