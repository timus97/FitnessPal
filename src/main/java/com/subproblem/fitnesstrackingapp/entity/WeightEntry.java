package com.subproblem.fitnesstrackingapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class WeightEntry {

    @Id
    private String id;
    @Indexed
    private String userId;
    private LocalDate date;
    private Double weightKg;
    private String note;
}
