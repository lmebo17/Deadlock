package com.deadlock.match;

import org.springframework.stereotype.Component;

@Component
public class EloCalculator {

    private static final int K_FACTOR = 32;

    public record Result(int player1Change, int player2Change) {}

    /**
     * @param p1Score 1.0 = win, 0.0 = loss, 0.5 = draw
     */
    public Result calculate(int p1Elo, int p2Elo, double p1Score) {
        double expected1 = 1.0 / (1.0 + Math.pow(10, (p2Elo - p1Elo) / 400.0));
        double p2Score = 1.0 - p1Score;
        double expected2 = 1.0 - expected1;

        int p1Change = (int) Math.round(K_FACTOR * (p1Score - expected1));
        int p2Change = (int) Math.round(K_FACTOR * (p2Score - expected2));

        // Enforce zero-sum (rounding errors can cause off-by-one)
        if (p1Change + p2Change != 0) {
            p2Change = -p1Change;
        }
        return new Result(p1Change, p2Change);
    }
}
