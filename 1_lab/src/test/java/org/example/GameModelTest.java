package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for Bulls and Cows logic.
 */
public class GameModelTest {

    @Test
    @DisplayName("1. Perfect match: 4 bulls")
    void testFourBulls() {
        GameModel game = new GameModel("1234", 10);
        GameResult res = game.check("1234");
        assertEquals(4, res.getBulls());
        assertEquals(0, res.getCows());
    }

    @Test
    @DisplayName("2. No matches: 0 bulls, 0 cows")
    void testNoMatches() {
        GameModel game = new GameModel("1234", 10);
        GameResult res = game.check("5678");
        assertEquals(0, res.getBulls());
        assertEquals(0, res.getCows());
    }

    @Test
    @DisplayName("3. All cows: correct digits, wrong positions")
    void testAllCows() {
        GameModel game = new GameModel("1234", 10);
        GameResult res = game.check("4321");
        assertEquals(0, res.getBulls());
        assertEquals(4, res.getCows());
    }



    @DisplayName("0. Mixed results calculation")
    void testMixedCalculation(String secret, String guess, int bulls, int cows) {
        GameModel game = new GameModel(secret, 10);
        GameResult res = game.check(guess);
        assertEquals(bulls, res.getBulls());
        assertEquals(cows, res.getCows());
    }




    @Test
    @DisplayName("7. Verify secret code storage")
    void testSecretCodeStorage() {
        GameModel game = new GameModel("567", 10);
        assertEquals("567", game.getSecretCode());
    }

    @Test
    @DisplayName("8. Verify initial attempts setting")
    void testInitialAttempts() {
        GameModel game = new GameModel("1234", 7);
        assertEquals(7, game.getAttemptsLeft());
    }



    @Test
    @DisplayName("9. Timeout simulation (empty string)")
    void testTimeoutEmpty() {
        GameModel game = new GameModel("1234", 5);
        game.check("");
        assertEquals(4, game.getAttemptsLeft());
    }

    @Test
    @DisplayName("10. Game over when attempts reach zero")
    void testGameOver() {
        GameModel game = new GameModel("1234", 1);
        game.check("0000");
        assertFalse(game.canContinue());
    }

    @Test
    @DisplayName("11. Win condition boolean check")
    void testIsFullWin() {
        GameResult res = new GameResult(4, 0);
        assertTrue(res.isWin(4));
        assertFalse(res.isWin(5));
    }

    @Test
    @DisplayName("12. Check attempts decrease after valid wrong guess")
    void testValidWrongGuessAttempts() {
        GameModel game = new GameModel("1234", 10);
        game.check("5678");
        assertEquals(9, game.getAttemptsLeft());
    }
}