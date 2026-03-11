package com.subproblem.fitnesstrackingapp.dto;

import jakarta.annotation.Nullable;

/**
 * Optional fields for updating user profile (height and weight goal).
 * Only non-null fields are applied.
 */
public record ProfileUpdateRequest(
        @Nullable Double heightCm,
        @Nullable Double targetWeightKg
) {
}
