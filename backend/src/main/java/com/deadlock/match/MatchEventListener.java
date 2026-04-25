package com.deadlock.match;

import com.deadlock.event.SubmissionJudgedEvent;
import com.deadlock.model.Match;
import com.deadlock.model.MatchStatus;
import com.deadlock.model.Submission;
import com.deadlock.repository.MatchRepository;
import com.deadlock.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchEventListener {

    private final SubmissionRepository submissionRepository;
    private final MatchRepository matchRepository;
    private final MatchService matchService;
    private final MatchEventPublisher eventPublisher;

    /**
     * Whenever a submission is judged, check if it belongs to an active match.
     * If verdict is ACCEPTED, attempt to resolve the match winner atomically.
     * Either way, notify the opponent that the submission count incremented.
     */
    @EventListener
    @Async("judgeExecutor")
    @Transactional
    public void onSubmissionJudged(SubmissionJudgedEvent event) {
        Submission submission = submissionRepository.findById(event.getSubmissionId()).orElse(null);
        if (submission == null || submission.getMatchId() == null) return;

        Long matchId = submission.getMatchId();
        Match match = matchRepository.findById(matchId).orElse(null);
        if (match == null || match.getStatus() != MatchStatus.IN_PROGRESS) return;

        Long submitterId = submission.getUser().getId();

        // Notify opponent of the submission (count, not verdict)
        Long opponentId = match.opponentOf(submitterId).getId();
        int countForSubmitter = (int) submissionRepository
                .findByUserIdAndProblemIdOrderBySubmittedAtDesc(submitterId, submission.getProblem().getId())
                .stream()
                .filter(s -> matchId.equals(s.getMatchId()))
                .count();
        eventPublisher.publishToUser(opponentId,
                MatchEventPayloads.opponentSubmitted(matchId, countForSubmitter));

        // If accepted, try to claim winner
        if ("ACCEPTED".equals(event.getVerdict())) {
            boolean won = matchService.tryResolveWinner(matchId, submitterId);
            log.info("Submission {} ACCEPTED in match {} — claimed winner: {}",
                    submission.getId(), matchId, won);
        }
    }
}
