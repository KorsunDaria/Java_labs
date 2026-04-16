package org.example;

/**
 * Handles the core game logic for calculating Bulls and Cows.
 */
public class GameModel {
    private String secretCode;
    private int attemptsLeft;

    /**
     * Initializes the game model with a secret code and attempt limit.
     * @param secretCode The code generated for the player to guess.
     * @param maxAttempts Maximum number of allowed attempts.
     */
    public GameModel(String secretCode, int maxAttempts) {
        this.secretCode = secretCode;
        this.attemptsLeft = maxAttempts;
    }

    /**
     * Checks the player's guess against the secret code.
     * @param guess The string provided by the player.
     * @return A GameResult object containing bulls and cows count.
     */
    public GameResult check(String guess) {
        int bulls = 0;
        int cows = 0;

        if (guess.isEmpty()) {
            attemptsLeft--;
            return new GameResult(0, 0);
        }

        for (int i = 0; i < guess.length(); i++) {
            char guessChar = guess.charAt(i);
            if (guessChar == secretCode.charAt(i)) {
                bulls++;
            } else if (secretCode.indexOf(guessChar)>=0){
                cows++;
            }
        }

        attemptsLeft--;
        return new GameResult(bulls, cows);
    }

    /**
     * Checks if the game can continue based on remaining attempts.
     * @return true if there are attempts left.
     */
    public boolean canContinue() {
        return attemptsLeft > 0;
    }

    /**
     * Returns the number of remaining attempts.
     * @return attempts count.
     */
    public int getAttemptsLeft() {
        return attemptsLeft;
    }

    /**
     * Returns the secret code generated for the game.
     * @return the secret code string.
     */
    public String getSecretCode() {
        return secretCode;
    }
}