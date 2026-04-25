package com.deadlock.match;

import com.deadlock.dto.MatchResponse;
import com.deadlock.exception.ResourceNotFoundException;
import com.deadlock.model.Match;
import com.deadlock.model.MatchStatus;
import com.deadlock.model.User;
import com.deadlock.repository.MatchRepository;
import com.deadlock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final EloCalculator eloCalculator;
    private final MatchEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public MatchResponse getMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match", matchId.toString()));
        return MatchResponse.from(match);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MatchResponse> getActiveMatch(Long userId) {
        return matchRepository.findActiveByUserId(userId).map(MatchResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchResponse> getMatchHistory(String username) {
        return matchRepository.findByUsernameOrderByStartedAtDesc(username).stream()
                .map(MatchResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public boolean tryResolveWinner(Long matchId, Long winnerUserId) {
        int updated = matchRepository.claimWinner(matchId, winnerUserId, Instant.now());
        if (updated == 0) {
            return false;
        }

        // Reload after atomic update, then apply ELO
        Match match = matchRepository.findById(matchId).orElseThrow();
        applyElo(match, winnerUserId);
        publishMatchEnd(match);
        return true;
    }

    @Override
    @Transactional
    public void finishMatch(Long matchId, Long winnerUserId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match", matchId.toString()));

        if (match.getStatus() == MatchStatus.FINISHED || match.getStatus() == MatchStatus.CANCELLED) {
            log.debug("Match {} already finished, skipping", matchId);
            return;
        }

        if (winnerUserId != null) {
            User winner = userRepository.findById(winnerUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", winnerUserId.toString()));
            match.setWinner(winner);
            match.setStatus(MatchStatus.CANCELLED); // forfeit
        } else {
            match.setStatus(MatchStatus.FINISHED); // draw
        }
        match.setEndedAt(Instant.now());
        matchRepository.save(match);

        applyElo(match, winnerUserId);
        publishMatchEnd(match);
    }

    private void applyElo(Match match, Long winnerUserId) {
        Long p1Id = match.getPlayer1().getId();
        double p1Score;
        if (winnerUserId == null) {
            p1Score = 0.5;
        } else {
            p1Score = winnerUserId.equals(p1Id) ? 1.0 : 0.0;
        }

        var result = eloCalculator.calculate(
                match.getPlayer1EloBefore(),
                match.getPlayer2EloBefore(),
                p1Score
        );

        match.setPlayer1EloChange(result.player1Change());
        match.setPlayer2EloChange(result.player2Change());

        // Refresh users from DB to update their current ELO atomically
        User p1 = userRepository.findById(p1Id).orElseThrow();
        User p2 = userRepository.findById(match.getPlayer2().getId()).orElseThrow();
        p1.setEloRating(p1.getEloRating() + result.player1Change());
        p2.setEloRating(p2.getEloRating() + result.player2Change());
        userRepository.save(p1);
        userRepository.save(p2);

        matchRepository.save(match);
        log.info("Match {} ELO applied: p1 {} ({}), p2 {} ({})",
                match.getId(), p1.getEloRating(), result.player1Change(),
                p2.getEloRating(), result.player2Change());
    }

    private void publishMatchEnd(Match match) {
        User p1 = userRepository.findById(match.getPlayer1().getId()).orElseThrow();
        User p2 = userRepository.findById(match.getPlayer2().getId()).orElseThrow();

        eventPublisher.publishToUser(p1.getId(),
                MatchEventPayloads.matchEnd(match, p1.getId(), p1.getEloRating()));
        eventPublisher.publishToUser(p2.getId(),
                MatchEventPayloads.matchEnd(match, p2.getId(), p2.getEloRating()));
        eventPublisher.publishToMatch(match.getId(),
                MatchEventPayloads.matchEnd(match, p1.getId(), p1.getEloRating()));
    }
}
