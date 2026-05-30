package id.my.agungdh.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@SQLRestriction("deleted_at IS NULL")
public class BaseEntity extends PanacheEntity {

    @Column(unique = true, nullable = false, updatable = false)
    public UUID uuid;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "created_by")
    public Long createdBy;

    @Column(name = "updated_at")
    public Instant updatedAt;

    @Column(name = "updated_by")
    public Long updatedBy;

    @Column(name = "deleted_at")
    public Instant deletedAt;

    @Column(name = "deleted_by")
    public Long deletedBy;

    @PrePersist
    void onCreate() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
        this.createdAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void softDelete(Long deletedBy) {
        this.deletedAt = Instant.now();
        this.deletedBy = deletedBy;
    }
}
