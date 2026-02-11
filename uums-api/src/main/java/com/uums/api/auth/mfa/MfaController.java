package com.uums.api.auth.mfa;

import com.uums.api.auth.dto.AuthResponse;
import com.uums.api.auth.dto.mfa.MfaChallengeRequest;
import com.uums.api.auth.dto.mfa.MfaChallengeResponse;
import com.uums.api.auth.dto.mfa.MfaVerifyRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/mfa")
public class MfaController {

    private final MfaService mfaService;

    public MfaController(MfaService mfaService) {
        this.mfaService = mfaService;
    }

    @PostMapping("/challenge")
    public MfaChallengeResponse challenge(@Valid @RequestBody MfaChallengeRequest request) {
        return mfaService.initiateChallenge(request);
    }

    @PostMapping("/verify")
    public AuthResponse verify(@Valid @RequestBody MfaVerifyRequest request) {
        return mfaService.verifyChallenge(request);
    }
}
