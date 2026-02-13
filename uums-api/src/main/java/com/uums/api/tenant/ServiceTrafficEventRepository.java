package com.uums.api.tenant;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceTrafficEventRepository extends JpaRepository<ServiceTrafficEvent, Long> {
    List<ServiceTrafficEvent> findTop200ByServiceIdOrderByEventTimeDesc(Long serviceId);
}
