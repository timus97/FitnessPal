package com.subproblem.fitnesstrackingapp.util;

import jakarta.annotation.Nullable;

public final class BmiCalculator {

    private static final double HEALTHY_TARGET_BMI = 22.0;
    private static final double MIN_WEIGHT_KG = 20.0;
    private static final double MAX_WEIGHT_KG = 300.0;
    private static final double MIN_HEIGHT_CM = 100.0;
    private static final double MAX_HEIGHT_CM = 250.0;

    private BmiCalculator() {
    }

    /**
     * Computes BMI from weight (kg) and height (cm). Returns null if height is invalid.
     */
    @Nullable
    public static Double bmi(Double weightKg, Double heightCm) {
        if (heightCm == null || heightCm <= 0) return null;
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    /**
     * Target weight (kg) to achieve the given target BMI at the given height.
     */
    public static double targetWeightKg(double targetBmi, double heightCm) {
        double heightM = heightCm / 100.0;
        return targetBmi * (heightM * heightM);
    }

    public static double getHealthyTargetBmi() {
        return HEALTHY_TARGET_BMI;
    }

    public static boolean isValidWeight(Double weightKg) {
        return weightKg != null && weightKg >= MIN_WEIGHT_KG && weightKg <= MAX_WEIGHT_KG;
    }

    public static boolean isValidHeight(Double heightCm) {
        return heightCm != null && heightCm >= MIN_HEIGHT_CM && heightCm <= MAX_HEIGHT_CM;
    }
}
