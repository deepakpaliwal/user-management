package com.uums.api.tenant;

import com.uums.api.tenant.dto.ServiceApiKeyCreateRequest;
import com.uums.api.tenant.dto.ServiceApiKeyResponse;
import com.uums.api.tenant.dto.ServiceApplicationResponse;
import com.uums.api.tenant.dto.ServiceOnboardRequest;
import com.uums.api.tenant.dto.ServiceStatRequest;
import com.uums.api.tenant.dto.ServiceStatResponse;
import com.uums.api.tenant.dto.ServiceTrafficEventResponse;
import com.uums.api.tenant.dto.TrafficIngestRequest;
import com.uums.api.tenant.dto.TrafficIngestResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/services")
@PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'MANAGER')")
public class ServiceOnboardingController {

    private final ServiceOnboardingService serviceOnboardingService;

    public ServiceOnboardingController(ServiceOnboardingService serviceOnboardingService) {
        this.serviceOnboardingService = serviceOnboardingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceApplicationResponse onboard(@Valid @RequestBody ServiceOnboardRequest request) {
        return serviceOnboardingService.onboard(request);
    }

    @GetMapping
    public List<ServiceApplicationResponse> list() {
        return serviceOnboardingService.list();
    }

    @PostMapping("/{serviceId}/keys")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceApiKeyResponse createApiKey(
            @PathVariable Long serviceId,
            @Valid @RequestBody ServiceApiKeyCreateRequest request) {
        return serviceOnboardingService.createKey(serviceId, request);
    }

    @GetMapping("/{serviceId}/keys")
    public List<ServiceApiKeyResponse> listApiKeys(@PathVariable Long serviceId) {
        return serviceOnboardingService.listKeys(serviceId);
    }

    @PostMapping("/traffic/ingest")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TrafficIngestResponse ingestTraffic(@Valid @RequestBody TrafficIngestRequest request) {
        return serviceOnboardingService.ingestTraffic(request);
    }

    @GetMapping("/{serviceId}/traffic")
    public List<ServiceTrafficEventResponse> listTraffic(@PathVariable Long serviceId) {
        return serviceOnboardingService.listTraffic(serviceId);
    }

    @PostMapping("/{serviceId}/stats")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceStatResponse createServiceStat(
            @PathVariable Long serviceId,
            @Valid @RequestBody ServiceStatRequest request) {
        return serviceOnboardingService.createServiceStat(serviceId, request);
    }

    @PutMapping("/{serviceId}/stats")
    public ServiceStatResponse upsertServiceStat(
            @PathVariable Long serviceId,
            @Valid @RequestBody ServiceStatRequest request) {
        return serviceOnboardingService.upsertServiceStat(serviceId, request);
    }

    @GetMapping("/{serviceId}/stats")
    public List<ServiceStatResponse> listServiceStats(@PathVariable Long serviceId) {
        return serviceOnboardingService.listServiceStats(serviceId);
    }
}
