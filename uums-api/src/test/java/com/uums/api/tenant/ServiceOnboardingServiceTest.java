package com.uums.api.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.uums.api.tenant.dto.ServiceOnboardRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServiceOnboardingServiceTest {

    @Mock
    private ServiceApplicationRepository repository;

    @Test
    void onboardShouldCreateApiKeyWithExpectedPrefix() {
        ServiceOnboardingService service = new ServiceOnboardingService(repository);
        ServiceOnboardRequest request = new ServiceOnboardRequest("billing-service", "owner@uums.local", PricingTier.BASIC);

        when(repository.existsByServiceName("billing-service")).thenReturn(false);
        when(repository.save(any(ServiceApplication.class))).thenAnswer(invocation -> {
            ServiceApplication app = invocation.getArgument(0);
            return app;
        });

        var response = service.onboard(request);

        assertEquals("billing-service", response.serviceName());
        assertTrue(response.apiKey().startsWith("uums_"));
        assertEquals(300, response.requestLimitPerMinute());
    }

    @Test
    void listShouldMapAllServices() {
        ServiceOnboardingService service = new ServiceOnboardingService(repository);
        ServiceApplication app = new ServiceApplication();
        app.setServiceName("reporting-service");
        app.setOwnerEmail("ops@uums.local");
        app.setApiKey("uums_dummy");
        app.setPricingTier(PricingTier.PRO);
        app.setRequestLimitPerMinute(1000);
        app.setActive(true);

        when(repository.findAll()).thenReturn(List.of(app));

        var result = service.list();
        assertEquals(1, result.size());
        assertEquals("reporting-service", result.getFirst().serviceName());
    }
}
