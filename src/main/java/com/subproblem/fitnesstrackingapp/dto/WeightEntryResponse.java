package com.subproblem.fitnesstrackingapp.dto;

import jakarta.annotation.Nullable;

import java.time.LocalDate;

public record WeightEntryResponse(
        String id,
        LocalDate date,
        Double weightKg,
        @Nullable Double bmi,
        @Nullable String note
) {
}
