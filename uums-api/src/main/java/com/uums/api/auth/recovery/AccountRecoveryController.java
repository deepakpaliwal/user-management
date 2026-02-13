package com.uums.api.auth.recovery;

import com.uums.api.auth.dto.recovery.RecoveryChallengeRequest;
import com.uums.api.auth.dto.recovery.RecoveryChallengeResponse;
import com.uums.api.auth.dto.recovery.RecoveryResetRequest;
import com.uums.api.auth.dto.recovery.RecoverySetupRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/recovery")
public class AccountRecoveryController {

    private final AccountRecoveryService accountRecoveryService;

    public AccountRecoveryController(AccountRecoveryService accountRecoveryService) {
        this.accountRecoveryService = accountRecoveryService;
    }

    @PostMapping("/setup")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setup(@Valid @RequestBody RecoverySetupRequest request) {
        accountRecoveryService.setupSecurityQuestion(request);
    }

    @PostMapping("/challenge")
    public RecoveryChallengeResponse challenge(@Valid @RequestBody RecoveryChallengeRequest request) {
        return accountRecoveryService.initiateRecovery(request);
    }

    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset(@Valid @RequestBody RecoveryResetRequest request) {
        accountRecoveryService.resetPassword(request);
    }
}
