package com.subproblem.fitnesstrackingapp.service;

import com.subproblem.fitnesstrackingapp.dto.WeightEntryRequest;
import com.subproblem.fitnesstrackingapp.dto.WeightEntryResponse;
import com.subproblem.fitnesstrackingapp.dto.WeightJourneyResponse;
import com.subproblem.fitnesstrackingapp.entity.User;
import com.subproblem.fitnesstrackingapp.entity.WeightEntry;
import com.subproblem.fitnesstrackingapp.repository.UserRepository;
import com.subproblem.fitnesstrackingapp.repository.WeightEntryRepository;
import com.subproblem.fitnesstrackingapp.util.BmiCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class WeightTrackerService {

    private static final int JOURNEY_RECENT_ENTRIES = 10;

    private final WeightEntryRepository weightEntryRepository;
    private final UserRepository userRepository;

    public WeightEntryResponse addEntry(Authentication authentication, WeightEntryRequest request) {
        User user = (User) authentication.getPrincipal();
        User persisted = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!BmiCalculator.isValidWeight(request.weightKg())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid weight: must be between 20 and 300 kg");
        }

        LocalDate entryDate = request.date() != null ? request.date() : LocalDate.now();
        WeightEntry entry = WeightEntry.builder()
                .userId(persisted.getId())
                .date(entryDate)
                .weightKg(request.weightKg())
                .note(request.note())
                .build();
        weightEntryRepository.save(entry);
        return toResponse(entry, persisted.getHeightCm());
    }

    public List<WeightEntryResponse> getEntries(Authentication authentication, LocalDate from, LocalDate to) {
        String userId = getUserId(authentication);
        List<WeightEntry> entries = (from != null && to != null)
                ? weightEntryRepository.findByUserIdAndDateBetweenOrderByDateDesc(userId, from, to)
                : weightEntryRepository.findByUserIdOrderByDateDesc(userId);
        User user = userRepository.findById(userId).orElse(null);
        Double heightCm = user != null ? user.getHeightCm() : null;
        return entries.stream().map(e -> toResponse(e, heightCm)).collect(Collectors.toList());
    }

    public WeightJourneyResponse getJourney(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        User persisted = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<WeightEntry> all = weightEntryRepository.findByUserIdOrderByDateDesc(persisted.getId());
        List<WeightEntry> recent = all.stream().limit(JOURNEY_RECENT_ENTRIES).toList();
        Double heightCm = persisted.getHeightCm();

        Double currentWeightKg = all.isEmpty() ? null : all.get(0).getWeightKg();
        Double currentBmi = (currentWeightKg != null && heightCm != null) ? BmiCalculator.bmi(currentWeightKg, heightCm) : null;

        double targetBmi;
        double targetWeightKg;
        if (persisted.getTargetWeightKg() != null && BmiCalculator.isValidWeight(persisted.getTargetWeightKg())) {
            targetWeightKg = persisted.getTargetWeightKg();
            targetBmi = heightCm != null ? BmiCalculator.bmi(targetWeightKg, heightCm) : BmiCalculator.getHealthyTargetBmi();
        } else {
            targetBmi = BmiCalculator.getHealthyTargetBmi();
            targetWeightKg = heightCm != null ? BmiCalculator.targetWeightKg(targetBmi, heightCm) : 0.0;
        }

        List<WeightEntryResponse> recentResponses = recent.stream()
                .map(e -> toResponse(e, heightCm))
                .collect(Collectors.toList());

        return new WeightJourneyResponse(
                currentWeightKg,
                currentBmi,
                targetWeightKg,
                targetBmi,
                heightCm,
                recentResponses
        );
    }

    public void deleteEntry(Authentication authentication, String entryId) {
        String userId = getUserId(authentication);
        WeightEntry entry = weightEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Weight entry not found"));
        if (!entry.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your weight entry");
        }
        weightEntryRepository.delete(entry);
    }

    private String getUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .getId();
    }

    private WeightEntryResponse toResponse(WeightEntry entry, Double heightCm) {
        Double bmi = BmiCalculator.bmi(entry.getWeightKg(), heightCm);
        return new WeightEntryResponse(
                entry.getId(),
                entry.getDate(),
                entry.getWeightKg(),
                bmi,
                entry.getNote()
        );
    }
}
