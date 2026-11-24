package com.recruitapp.apiservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "candidates")
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String city;
    private Integer experienceYears;
    private Integer noticePeriodDays;
    private Instant createdAt = Instant.now();

    public Candidate(String name, String email, String city, Integer experienceYears, Integer noticePeriodDays) {
        this.name = name;
        this.email = email;
        this.city = city;
        this.experienceYears = experienceYears;
        this.noticePeriodDays = noticePeriodDays;
    }

    public void updateProfile(String name, String email, String city,
                              Integer experienceYears, Integer noticePeriodDays) {
        this.name = name.trim().toLowerCase();
        this.email = email.trim().toLowerCase();
        this.city = city.trim().toLowerCase();
        this.experienceYears = experienceYears;
        this.noticePeriodDays = noticePeriodDays;
    }
}