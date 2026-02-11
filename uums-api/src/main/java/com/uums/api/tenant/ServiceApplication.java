package com.uums.api.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_application")
public class ServiceApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", nullable = false, unique = true, length = 160)
    private String serviceName;

    @Column(name = "owner_email", nullable = false, length = 255)
    private String ownerEmail;

    @Column(name = "api_key", nullable = false, unique = true, length = 128)
    private String apiKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_tier", nullable = false, length = 30)
    private PricingTier pricingTier;

    @Column(name = "request_limit_per_min", nullable = false)
    private int requestLimitPerMinute;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public PricingTier getPricingTier() { return pricingTier; }
    public void setPricingTier(PricingTier pricingTier) { this.pricingTier = pricingTier; }
    public int getRequestLimitPerMinute() { return requestLimitPerMinute; }
    public void setRequestLimitPerMinute(int requestLimitPerMinute) { this.requestLimitPerMinute = requestLimitPerMinute; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
