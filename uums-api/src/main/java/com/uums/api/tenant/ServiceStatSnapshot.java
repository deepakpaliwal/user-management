package com.uums.api.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_stat_snapshot")
public class ServiceStatSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceApplication service;

    @Column(name = "stat_code", nullable = false, length = 80)
    private String statCode;

    @Column(name = "stat_value", nullable = false)
    private Long statValue;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public ServiceApplication getService() { return service; }
    public void setService(ServiceApplication service) { this.service = service; }
    public String getStatCode() { return statCode; }
    public void setStatCode(String statCode) { this.statCode = statCode; }
    public Long getStatValue() { return statValue; }
    public void setStatValue(Long statValue) { this.statValue = statValue; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
