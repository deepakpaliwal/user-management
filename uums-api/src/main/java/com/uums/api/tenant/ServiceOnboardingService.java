package com.uums.api.tenant;

import com.uums.api.auth.AuthException;
import com.uums.api.tenant.dto.ServiceApiKeyCreateRequest;
import com.uums.api.tenant.dto.ServiceApiKeyResponse;
import com.uums.api.tenant.dto.ServiceApplicationResponse;
import com.uums.api.tenant.dto.ServiceOnboardRequest;
import com.uums.api.tenant.dto.ServiceStatRequest;
import com.uums.api.tenant.dto.ServiceStatResponse;
import com.uums.api.tenant.dto.ServiceTrafficEventResponse;
import com.uums.api.tenant.dto.TrafficIngestMessage;
import com.uums.api.tenant.dto.TrafficIngestRequest;
import com.uums.api.tenant.dto.TrafficIngestResponse;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceOnboardingService {

    private final ServiceApplicationRepository serviceRepository;
    private final ServiceApiKeyRepository apiKeyRepository;
    private final ServiceTrafficEventRepository trafficEventRepository;
    private final ServiceStatSnapshotRepository statSnapshotRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public ServiceOnboardingService(
            ServiceApplicationRepository serviceRepository,
            ServiceApiKeyRepository apiKeyRepository,
            ServiceTrafficEventRepository trafficEventRepository,
            ServiceStatSnapshotRepository statSnapshotRepository) {
        this.serviceRepository = serviceRepository;
        this.apiKeyRepository = apiKeyRepository;
        this.trafficEventRepository = trafficEventRepository;
        this.statSnapshotRepository = statSnapshotRepository;
    }

    @Transactional
    public ServiceApplicationResponse onboard(ServiceOnboardRequest request) {
        if (serviceRepository.existsByServiceName(request.serviceName())) {
            throw new AuthException("Service name already onboarded");
        }

        ServiceApplication app = new ServiceApplication();
        app.setServiceName(request.serviceName());
        app.setOwnerEmail(request.ownerEmail());
        app.setPricingTier(request.pricingTier());
        app.setRequestLimitPerMinute(defaultLimit(request.pricingTier()));
        app.setApiKey(generateApiKey());
        ServiceApplication saved = serviceRepository.save(app);

        ServiceApiKey defaultKey = new ServiceApiKey();
        defaultKey.setService(saved);
        defaultKey.setKeyName("default");
        defaultKey.setApiKey(saved.getApiKey());
        apiKeyRepository.save(defaultKey);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ServiceApplicationResponse> list() {
        return serviceRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public ServiceApiKeyResponse createKey(Long serviceId, ServiceApiKeyCreateRequest request) {
        ServiceApplication service = getService(serviceId);
        ServiceApiKey key = new ServiceApiKey();
        key.setService(service);
        key.setKeyName(request.keyName());
        key.setApiKey(generateApiKey());
        return toKeyResponse(apiKeyRepository.save(key));
    }

    @Transactional(readOnly = true)
    public List<ServiceApiKeyResponse> listKeys(Long serviceId) {
        getService(serviceId);
        return apiKeyRepository.findByServiceIdOrderByCreatedAtDesc(serviceId).stream()
                .map(this::toKeyResponse)
                .toList();
    }

    @Transactional
    public TrafficIngestResponse ingestTraffic(TrafficIngestRequest request) {
        processTrafficMessagesAsync(request.messages());
        return new TrafficIngestResponse(request.messages().size(), "ACCEPTED");
    }

    @Async
    @Transactional
    public void processTrafficMessagesAsync(List<TrafficIngestMessage> messages) {
        for (TrafficIngestMessage message : messages) {
            ServiceApplication service = getService(message.serviceId());
            ServiceTrafficEvent event = new ServiceTrafficEvent();
            event.setService(service);
            event.setApiName(message.apiName());
            event.setInputPayload(message.inputData());
            event.setStatus(message.status());
            event.setEventTime(message.eventTime() == null ? LocalDateTime.now() : message.eventTime());
            trafficEventRepository.save(event);
        }
    }

    @Transactional(readOnly = true)
    public List<ServiceTrafficEventResponse> listTraffic(Long serviceId) {
        getService(serviceId);
        return trafficEventRepository.findTop200ByServiceIdOrderByEventTimeDesc(serviceId).stream()
                .map(event -> new ServiceTrafficEventResponse(
                        event.getId(),
                        event.getService().getId(),
                        event.getService().getServiceName(),
                        event.getApiName(),
                        event.getInputPayload(),
                        event.getStatus(),
                        event.getEventTime()))
                .toList();
    }

    @Transactional
    public ServiceStatResponse createServiceStat(Long serviceId, ServiceStatRequest request) {
        ServiceApplication service = getService(serviceId);
        statSnapshotRepository.findByServiceIdAndStatCode(serviceId, request.statCode())
                .ifPresent(existing -> {
                    throw new AuthException("Stat already exists for this service: " + request.statCode());
                });

        ServiceStatSnapshot stat = new ServiceStatSnapshot();
        stat.setService(service);
        stat.setStatCode(request.statCode());
        stat.setStatValue(request.statValue());
        stat.setUpdatedAt(LocalDateTime.now());
        return toStatResponse(statSnapshotRepository.save(stat));
    }

    @Transactional
    public ServiceStatResponse upsertServiceStat(Long serviceId, ServiceStatRequest request) {
        ServiceApplication service = getService(serviceId);
        ServiceStatSnapshot stat = statSnapshotRepository.findByServiceIdAndStatCode(serviceId, request.statCode())
                .orElseGet(() -> {
                    ServiceStatSnapshot created = new ServiceStatSnapshot();
                    created.setService(service);
                    created.setStatCode(request.statCode());
                    return created;
                });

        stat.setStatValue(request.statValue());
        stat.setUpdatedAt(LocalDateTime.now());
        return toStatResponse(statSnapshotRepository.save(stat));
    }

    @Transactional(readOnly = true)
    public List<ServiceStatResponse> listServiceStats(Long serviceId) {
        getService(serviceId);
        return statSnapshotRepository.findByServiceIdOrderByUpdatedAtDesc(serviceId).stream()
                .map(this::toStatResponse)
                .toList();
    }

    private ServiceApplication getService(Long serviceId) {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new AuthException("Service not found: " + serviceId));
    }

    private int defaultLimit(PricingTier tier) {
        return switch (tier) {
            case FREE -> 60;
            case BASIC -> 300;
            case PRO -> 1000;
            case ENTERPRISE -> 5000;
        };
    }

    String generateApiKey() {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        return "uums_" + HexFormat.of().formatHex(bytes);
    }

    private ServiceApplicationResponse toResponse(ServiceApplication app) {
        return new ServiceApplicationResponse(
                app.getId(),
                app.getServiceName(),
                app.getOwnerEmail(),
                app.getApiKey(),
                app.getPricingTier(),
                app.getRequestLimitPerMinute(),
                app.isActive(),
                app.getCreatedAt());
    }

    private ServiceApiKeyResponse toKeyResponse(ServiceApiKey key) {
        return new ServiceApiKeyResponse(
                key.getId(),
                key.getService().getId(),
                key.getKeyName(),
                key.getApiKey(),
                key.isActive(),
                key.getCreatedAt());
    }

    private ServiceStatResponse toStatResponse(ServiceStatSnapshot stat) {
        return new ServiceStatResponse(
                stat.getId(),
                stat.getService().getId(),
                stat.getService().getServiceName(),
                stat.getStatCode(),
                stat.getStatValue(),
                stat.getUpdatedAt());
    }
}
