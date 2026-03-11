package com.subproblem.fitnesstrackingapp.repository;

import com.subproblem.fitnesstrackingapp.entity.WeightEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface WeightEntryRepository extends MongoRepository<WeightEntry, String> {

    List<WeightEntry> findByUserIdOrderByDateDesc(String userId);

    List<WeightEntry> findByUserIdAndDateBetweenOrderByDateDesc(String userId, LocalDate from, LocalDate to);
}
