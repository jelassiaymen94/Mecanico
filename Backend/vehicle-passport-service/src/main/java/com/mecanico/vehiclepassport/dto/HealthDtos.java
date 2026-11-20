package com.mecanico.vehiclepassport.dto;

import java.util.List;
import java.util.Map;

public final class HealthDtos {

    private HealthDtos() {
    }

    public record HealthFactor(
            String code,
            String label,
            String status,
            String detail,
            int penalty
    ) {
    }

    public record HealthScoreResponse(
            int score,
            String grade,
            String summary,
            Map<String, Integer> breakdown,
            List<HealthFactor> factors
    ) {
    }
}
