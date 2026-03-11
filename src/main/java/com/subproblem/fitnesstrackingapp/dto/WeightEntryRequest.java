package com.subproblem.fitnesstrackingapp.dto;

import jakarta.annotation.Nullable;

import java.time.LocalDate;

public record WeightEntryRequest(
        Double weightKg,
        @Nullable LocalDate date,
        @Nullable String note
) {
}
