package com.uums.api.tenant;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceApplicationRepository extends JpaRepository<ServiceApplication, Long> {
    boolean existsByServiceName(String serviceName);
    Optional<ServiceApplication> findByApiKey(String apiKey);
}
