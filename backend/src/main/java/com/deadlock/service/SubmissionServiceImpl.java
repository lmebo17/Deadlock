package com.deadlock.service;

import com.deadlock.dto.SubmissionResponse;
import com.deadlock.exception.InvalidInputException;
import com.deadlock.exception.ResourceNotFoundException;
import com.deadlock.model.Language;
import com.deadlock.model.Problem;
import com.deadlock.model.Submission;
import com.deadlock.model.User;
import com.deadlock.repository.ProblemRepository;
import com.deadlock.repository.SubmissionRepository;
import com.deadlock.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

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
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
        Problem problem = problemRepository.findBySlug(problemSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Problem", problemSlug));

        Language lang;
        try {
            lang = Language.valueOf(language.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Unsupported language: " + language);
        }

        Submission submission = new Submission();
        submission.setUser(user);
        submission.setProblem(problem);
        submission.setLanguage(lang);
        submission.setCode(code);

        Submission saved = submissionRepository.save(submission);
        log.info("Submission {} created for problem {} by user {}", saved.getId(), problemSlug, userId);

        Long submissionId = saved.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                submissionRepository.findById(submissionId).ifPresent(judgeService::judge);
            }
        });

        return submissionId;
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmission(Long id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", id.toString()));
        return SubmissionResponse.from(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getUserSubmissionsForProblem(Long userId, String problemSlug) {
        Problem problem = problemRepository.findBySlug(problemSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Problem", problemSlug));
        return submissionRepository.findByUserIdAndProblemIdOrderBySubmittedAtDesc(userId, problem.getId())
                .stream()
                .map(SubmissionResponse::from)
                .toList();
    }
}
