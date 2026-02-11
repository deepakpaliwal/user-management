package com.uums.api.tenant;

import com.uums.api.tenant.dto.ServiceApplicationResponse;
import com.uums.api.tenant.dto.ServiceOnboardRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/services")
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
}
