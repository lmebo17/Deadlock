package com.deadlock.service;

import com.deadlock.dto.SubmissionResponse;
import com.deadlock.model.Problem;
import com.deadlock.model.Submission;
import com.deadlock.model.User;
import com.deadlock.repository.ProblemRepository;
import com.deadlock.repository.SubmissionRepository;
import com.deadlock.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final JudgeService judgeService;

    public SubmissionServiceImpl(SubmissionRepository submissionRepository,
                                  ProblemRepository problemRepository,
                                  UserRepository userRepository,
                                  JudgeService judgeService) {
        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
        this.judgeService = judgeService;
    }

    @Override
    @Transactional
    public Long submit(Long userId, String problemSlug, String language, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Problem problem = problemRepository.findBySlug(problemSlug)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + problemSlug));

        String lang = language.toUpperCase();
        if (!lang.equals("JAVA") && !lang.equals("PYTHON") && !lang.equals("CPP")) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }

        Submission submission = new Submission();
        submission.setUser(user);
        submission.setProblem(problem);
        submission.setLanguage(lang);
        submission.setCode(code);
        submission.setStatus("PENDING");

        Submission saved = submissionRepository.save(submission);
        log.info("Submission {} created for problem {} by user {}", saved.getId(), problemSlug, userId);

        judgeService.judge(saved);

        return saved.getId();
    }

    @Override
    public SubmissionResponse getSubmission(Long id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
        return SubmissionResponse.from(submission);
    }
}
