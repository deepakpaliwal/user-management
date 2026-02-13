package com.uums.api.tenant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record TrafficIngestRequest(
        @NotEmpty List<@Valid TrafficIngestMessage> messages) {
}
