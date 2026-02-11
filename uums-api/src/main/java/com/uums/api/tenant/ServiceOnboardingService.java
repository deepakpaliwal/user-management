package com.uums.api.tenant;

import com.uums.api.auth.AuthException;
import com.uums.api.tenant.dto.ServiceApplicationResponse;
import com.uums.api.tenant.dto.ServiceOnboardRequest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceOnboardingService {

    private final ServiceApplicationRepository repository;
    private final SecureRandom secureRandom = new SecureRandom();

    public ServiceOnboardingService(ServiceApplicationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ServiceApplicationResponse onboard(ServiceOnboardRequest request) {
        if (repository.existsByServiceName(request.serviceName())) {
            throw new AuthException("Service name already onboarded");
        }

        ServiceApplication app = new ServiceApplication();
        app.setServiceName(request.serviceName());
        app.setOwnerEmail(request.ownerEmail());
        app.setPricingTier(request.pricingTier());
        app.setRequestLimitPerMinute(defaultLimit(request.pricingTier()));
        app.setApiKey(generateApiKey());

        return toResponse(repository.save(app));
    }

    @Transactional(readOnly = true)
    public List<ServiceApplicationResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
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
}
