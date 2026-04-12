package com.deadlock.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "problems")
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "input_format", nullable = false, columnDefinition = "TEXT")
    private String inputFormat;

    @Column(name = "output_format", nullable = false, columnDefinition = "TEXT")
    private String outputFormat;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String constraints;

    @Column(nullable = false)
    private int rating = 1200;

    @Column(name = "time_limit_ms", nullable = false)
    private int timeLimitMs = 2000;

    @Column(name = "memory_limit_mb", nullable = false)
    private int memoryLimitMb = 256;

    @Column(name = "test_case_count", nullable = false)
    private int testCaseCount = 0;

    @Column(name = "sample_count", nullable = false)
    private int sampleCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Problem() {}

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getInputFormat() { return inputFormat; }
    public void setInputFormat(String inputFormat) { this.inputFormat = inputFormat; }
    public String getOutputFormat() { return outputFormat; }
    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }
    public String getConstraints() { return constraints; }
    public void setConstraints(String constraints) { this.constraints = constraints; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public int getTimeLimitMs() { return timeLimitMs; }
    public void setTimeLimitMs(int timeLimitMs) { this.timeLimitMs = timeLimitMs; }
    public int getMemoryLimitMb() { return memoryLimitMb; }
    public void setMemoryLimitMb(int memoryLimitMb) { this.memoryLimitMb = memoryLimitMb; }
    public int getTestCaseCount() { return testCaseCount; }
    public void setTestCaseCount(int testCaseCount) { this.testCaseCount = testCaseCount; }
    public int getSampleCount() { return sampleCount; }
    public void setSampleCount(int sampleCount) { this.sampleCount = sampleCount; }
    public Instant getCreatedAt() { return createdAt; }

    public String getTierLabel() {
        if (rating <= 1000) return "Beginner";
        if (rating <= 1400) return "Easy";
        if (rating <= 1800) return "Medium";
        if (rating <= 2200) return "Hard";
        return "Expert";
    }
}
