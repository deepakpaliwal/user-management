package com.uums.api.tenant;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceApiKeyRepository extends JpaRepository<ServiceApiKey, Long> {
    List<ServiceApiKey> findByServiceIdOrderByCreatedAtDesc(Long serviceId);
    Optional<ServiceApiKey> findByApiKeyAndActiveTrue(String apiKey);
}
