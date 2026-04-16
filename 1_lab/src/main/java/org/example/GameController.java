package org.example;

import java.util.*;

/**
 * Manages the game flow, user interactions, and time limits.
 */
public class GameController {
    private Scanner scanner = new Scanner(System.in);
    private GameLogger logger = new GameLogger("game.log");

    /**
     * Starts the game setup and initiates the main game loop.
     */
    public void start() {
        int initSuccess=0;
        System.out.println("=== Game Settings ===");
        while (initSuccess==0) {
            try {
                System.out.print("Enter code length (4-6): ");
                int len = Integer.parseInt(scanner.nextLine());

                if (len>6 || len<4){
                    System.out.println("Input error. Invalid length.");
                    continue;
                }

                System.out.print("Enter max attempts: ");
                int attempts = Integer.parseInt(scanner.nextLine());

                System.out.print("Enter time limit per move (sec): ");
                int timeLimit = Integer.parseInt(scanner.nextLine());

                String secret = generateSecret(len);
                GameModel game = new GameModel(secret, attempts);

                logger.log("Game started. Secret: " + secret + " | Attempts: " + attempts);
                logger.log("Time Limit: " + timeLimit);
                runGame(game, len, timeLimit);
                initSuccess=1;

            } catch (Exception e) {
                System.out.println("Input error. Please enter numeric values only.");
            }
        }

    }

    /**
     * Executes the main game loop until win or loss.
     * @param game The game model instance.
     * @param len Required guess length.
     * @param limit Time limit for each move in seconds.
     */
    private void runGame(GameModel game, int len, int limit) {
        int step = 0;
        while (game.canContinue()) {
            step++;
            System.out.printf("\nAttempt #%d (Remaining: %d)\n", step, game.getAttemptsLeft());
            System.out.print("Enter " + len + " digits (" + limit + " sec limit): ");

            String input = readWithTimeout(limit);

            if (input == null) {
                System.out.println("\n[!] TIME IS UP! This move is skipped.");
                game.check("");
                logger.log("Move " + step + ": Timeout reached");
                continue;
            }

            if (input.length() != len) {
                System.out.println("Error: Input must be " + len + " digits.");
                step--;
                continue;
            }

            GameResult result = game.check(input);
            logger.logMove(step, input, result);
            System.out.println("Result: Bulls=" + result.getBulls() + ", Cows=" + result.getCows());

            if (result.isWin(len)) {
                System.out.println("CONGRATULATIONS! YOU WON!");
                logger.log("Victory at step: " + step);
                return;
            }
        }
        System.out.println("GAME OVER. The secret code was: " + game.getSecretCode());
        logger.log("Game finished. Result: Defeat.");
    }

    /**
     * Reads user input from console with a time limit.
     * @param seconds Maximum time to wait for input.
     * @return The input string or null if timeout occurs.
     */
    private String readWithTimeout(int seconds) {
        long endTime = System.currentTimeMillis() + (seconds * 1000L);
        try {
            while (System.currentTimeMillis() < endTime) {
                if (System.in.available() > 0) {
                    return scanner.nextLine();
                }
                Thread.sleep(100);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * Generates a secret code with unique random digits.
     * @param len Length of the secret code.
     * @return A string of unique digits.
     */
    private String generateSecret(int len) {
        Random random = new Random();
        String result = "";

        for (int i = 0; i < len; i++) {
            int digit = random.nextInt(10);
            String digitStr = String.valueOf(digit);

            if (!result.contains(digitStr)) {
                result = result + digitStr;
            } else {
                i--; // Repeat iteration if digit is not unique
            }
        }
        return result;
    }
}