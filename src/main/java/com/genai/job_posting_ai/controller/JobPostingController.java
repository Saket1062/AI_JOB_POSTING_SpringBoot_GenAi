package com.genai.job_posting_ai.controller;

import com.genai.job_posting_ai.entity.JobPosting;
import com.genai.job_posting_ai.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/jobs")
public class JobPostingController {

    private final JobPostingService jobPostingService;

    // Show job posting form
    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("jobPosting", new JobPosting());
        return "job-form.html";  // Thymeleaf template
    }

    // Save job after HR edits
    @PostMapping("/save")
    public String saveJob(@ModelAttribute JobPosting jobPosting) {
        jobPostingService.saveJob(jobPosting);
        return "redirect:/jobs/list";
    }

    // Generate AI job description (AJAX)
    @PostMapping(value = "/generate-description-ajax", produces = "application/json")
    @ResponseBody
    public Map<String, String> generateDescriptionAjax(@RequestBody JobPosting jobPosting) {
        String aiDescription = jobPostingService.generateJobDescription(jobPosting);
        return Collections.singletonMap("description", aiDescription);
    }

    // Show all jobs
    @GetMapping("/list")
    public String listJobs(Model model) {
        model.addAttribute("jobs", jobPostingService.getAllJobs());
        return "job-list";  // Thymeleaf template
    }
}
