package com.subproblem.fitnesstrackingapp.controller;

import com.subproblem.fitnesstrackingapp.dto.WeightEntryRequest;
import com.subproblem.fitnesstrackingapp.dto.WeightEntryResponse;
import com.subproblem.fitnesstrackingapp.dto.WeightJourneyResponse;
import com.subproblem.fitnesstrackingapp.service.WeightTrackerService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/secured/user")
public class WeightController {

    private final WeightTrackerService weightTrackerService;
    private final UserService userService;

    @PostMapping("/weight")
    public ResponseEntity<WeightEntryResponse> addWeightEntry(
            @RequestBody WeightEntryRequest request,
            Authentication authentication) {
        WeightEntryResponse response = weightTrackerService.addEntry(authentication, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/weight")
    public List<WeightEntryResponse> getWeightEntries(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication) {
        return weightTrackerService.getEntries(authentication, from, to);
    }

    @GetMapping("/weight/journey")
    public WeightJourneyResponse getWeightJourney(Authentication authentication) {
        return weightTrackerService.getJourney(authentication);
    }

    @DeleteMapping("/weight/{id}")
    public ResponseEntity<Void> deleteWeightEntry(@PathVariable String id, Authentication authentication) {
        weightTrackerService.deleteEntry(authentication, id);
        return ResponseEntity.noContent().build();
    }
}
