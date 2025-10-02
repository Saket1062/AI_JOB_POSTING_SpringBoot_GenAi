package com.genai.job_posting_ai.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobTitle;
    private String companyName;
    private String location;

    private String experienceRange;   // e.g. "2-4 years"
    private int openPositions;

    private String salaryRange;       // e.g. "8-12 LPA"

    @Column(length = 2000)
    private String mustHaveSkills;

    @Column(length = 2000)
    private String goodToHaveSkills;

    @Column(length = 5000)
    private String jobDescription;    // AI-generated + edited by HR
}
