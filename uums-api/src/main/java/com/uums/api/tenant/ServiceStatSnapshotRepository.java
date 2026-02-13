package com.uums.api.tenant;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceStatSnapshotRepository extends JpaRepository<ServiceStatSnapshot, Long> {
    Optional<ServiceStatSnapshot> findByServiceIdAndStatCode(Long serviceId, String statCode);
    List<ServiceStatSnapshot> findByServiceIdOrderByUpdatedAtDesc(Long serviceId);
}
