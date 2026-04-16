package org.example;

/**
 * Data container for storing the result of a single guess check.
 */
public class GameResult {
    private final int bulls;
    private final int cows;

    /**
     * Constructor for GameResult.
     * @param bulls Count of correctly placed digits.
     * @param cows Count of digits present but misplaced.
     */
    public GameResult(int bulls, int cows) {
        this.bulls = bulls;
        this.cows = cows;
    }

    /** @return number of bulls. */
    public int getBulls() { return bulls; }

    /** @return number of cows. */
    public int getCows() { return cows; }

    /**
     * Checks if all digits match exactly.
     * @param length The expected code length.
     * @return true if bulls count equals length.
     */
    public boolean isWin(int length) {
        return bulls == length;
    }
}