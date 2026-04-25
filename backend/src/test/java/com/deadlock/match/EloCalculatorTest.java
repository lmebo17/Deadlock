package com.deadlock.match;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EloCalculatorTest {

    private final EloCalculator calculator = new EloCalculator();

    @Test
    void equalRatedPlayersWinGains16Points() {
        var result = calculator.calculate(1500, 1500, 1.0);
        assertThat(result.player1Change()).isEqualTo(16);
        assertThat(result.player2Change()).isEqualTo(-16);
    }

    @Test
    void equalRatedPlayersLossLoses16Points() {
        var result = calculator.calculate(1500, 1500, 0.0);
        assertThat(result.player1Change()).isEqualTo(-16);
        assertThat(result.player2Change()).isEqualTo(16);
    }

    @Test
    void drawBetweenEqualRatedPlayersIsZero() {
        var result = calculator.calculate(1500, 1500, 0.5);
        assertThat(result.player1Change()).isEqualTo(0);
        assertThat(result.player2Change()).isEqualTo(0);
    }

    @Test
    void higherRatedPlayerGainsLessOnWin() {
        // 1700 beats 1300 — only gains a few points
        var result = calculator.calculate(1700, 1300, 1.0);
        assertThat(result.player1Change()).isLessThan(8);
        assertThat(result.player2Change()).isEqualTo(-result.player1Change());
    }

    @Test
    void lowerRatedPlayerGainsLotsOnUpsetWin() {
        // 1300 beats 1700 — big gain
        var result = calculator.calculate(1300, 1700, 1.0);
        assertThat(result.player1Change()).isGreaterThan(24);
    }

    @Test
    void zeroSumGuarantee() {
        // ELO changes always cancel out
        var result1 = calculator.calculate(1234, 1567, 1.0);
        assertThat(result1.player1Change() + result1.player2Change()).isEqualTo(0);

        var result2 = calculator.calculate(1234, 1567, 0.5);
        assertThat(result2.player1Change() + result2.player2Change()).isEqualTo(0);
    }

    @Test
    void hugeRatingGapWinExpectedPlayerGainsAlmostNothing() {
        var result = calculator.calculate(2400, 1000, 1.0);
        assertThat(result.player1Change()).isBetween(0, 2);
    }
}
