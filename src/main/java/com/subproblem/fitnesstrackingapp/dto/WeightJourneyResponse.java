package com.subproblem.fitnesstrackingapp.dto;

import jakarta.annotation.Nullable;

import java.util.List;

/**
 * Summary of the user's weight-loss journey: current state, goal (target) weight and BMI,
 * and recent entries. Target values are either user-set or suggested from a healthy BMI (22).
 */
public record WeightJourneyResponse(
        @Nullable Double currentWeightKg,
        @Nullable Double currentBmi,
        Double targetWeightKg,
        Double targetBmi,
        @Nullable Double heightCm,
        List<WeightEntryResponse> recentEntries
) {
}
