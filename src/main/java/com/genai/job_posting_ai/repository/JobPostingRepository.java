package com.genai.job_posting_ai.repository;

import com.genai.job_posting_ai.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

}
