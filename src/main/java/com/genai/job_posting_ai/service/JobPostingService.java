package com.genai.job_posting_ai.service;

import com.genai.job_posting_ai.entity.JobPosting;
import com.genai.job_posting_ai.repository.JobPostingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final RestTemplate restTemplate;

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    // Save Job Posting
    public JobPosting saveJob(JobPosting jobPosting) {
        return jobPostingRepository.save(jobPosting);
    }

    // Get all jobs
    public List<JobPosting> getAllJobs() {
        return jobPostingRepository.findAll();
    }

    public String generateJobDescription(JobPosting jobPosting) {

        String prompt = "Create a professional and detailed job description with the following sections: 'Job Summary', 'Key Responsibilities', and 'Minimum Qualifications'.\n" +
                "The output should be formatted using clear section headings.\n\n" +
                "Details:\n" +
                "Job Title: " + jobPosting.getJobTitle() + "\n" +
                "Company: " + jobPosting.getCompanyName() + "\n" +
                "Location: " + jobPosting.getLocation() + "\n" +
                "Experience: " + jobPosting.getExperienceRange() + "\n" +
                "Must Have Skills: " + jobPosting.getMustHaveSkills() + "\n" +
                "Good to Have Skills: " + jobPosting.getGoodToHaveSkills() + "\n\n" +
                "Instructions:\n" +
                "1. **Job Summary**: Write a brief paragraph that provides a high-level overview of the role, the company, and the impact the candidate will have.\n" +
                "2. **Key Responsibilities**: Use bullet points to list the main duties and expectations of the role. Use strong action verbs.\n" +
                "3. **Minimum Qualifications**: Use bullet points to list the required skills, experience, and educational background. Incorporate both 'Must Have' and 'Good to Have' skills into this section, clearly distinguishing between them or presenting them logically.\n" +
                "4. Ensure the entire response is well-structured and easy to read.";

        try {
            // Build request body
            Map<String, Object> requestBody = new HashMap<>();

            Map<String, String> part = new HashMap<>();
            part.put("text", prompt);

            Map<String, Object> contentItem = new HashMap<>();
            contentItem.put("parts", List.of(part));

            requestBody.put("contents", List.of(contentItem));

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Construct the correct API URL with the model and API key
            String fullGeminiApiUrl = geminiApiUrl + "?key=" + geminiApiKey;

            // Make API call
            ResponseEntity<String> response = restTemplate.exchange(fullGeminiApiUrl, HttpMethod.POST, request, String.class);

            // Parse response safely
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    JsonNode textNode = parts.get(0).path("text");
                    if (!textNode.isMissingNode()) {
                        String rawText = textNode.asText();
                        return cleanJobDescriptionFromSummary(rawText);
                    }
                }
            }

        } catch (RestClientException e) {
            System.err.println("Error calling Gemini API: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error parsing Gemini API response: " + e.getMessage());
        }

        return "AI could not generate description. Please edit manually.";
    }

    public String cleanJobDescriptionFromSummary(String rawText) {
        if (rawText == null || rawText.isEmpty()) {
            return "";
        }

        // 1. Find the position of "Job Summary" and ignore everything before it
        int summaryIndex = rawText.indexOf("Job Summary");
        if (summaryIndex >= 0) {
            rawText = rawText.substring(summaryIndex).trim();
        }

        // 2. Remove bold markdown **
        String cleaned = rawText.replaceAll("\\*\\*(.*?)\\*\\*", "$1");

        // 3. Convert * and - bullets to plain bullets (•)
        cleaned = cleaned.replaceAll("(?m)^\\*\\s*(.*?)$", "• $1")
                .replaceAll("(?m)^-\\s*(.*?)$", "• $1");

        // 4. Handle Must Have / Good to Have subsections starting with *
        cleaned = cleaned.replaceAll("(?m)^(\\*\\s*Must Have:)", "\n$1")
                .replaceAll("(?m)^(\\*\\s*Good to Have:)", "\n$1");

        // 5. Remove any remaining single * used as markdown
        cleaned = cleaned.replaceAll("\\*(.*?)\\*", "$1");

        // 6. Remove headings starting with #
        cleaned = cleaned.replaceAll("(?m)^#(#+)?\\s*(.*?)$", "$2");

        // 7. Replace multiple newlines with a single newline
        cleaned = cleaned.replaceAll("\\n{2,}", "\n");

        return cleaned.trim();
    }

}
