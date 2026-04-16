package org.example.util;

import org.example.model.GameModel;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class StatsManager {
    private static final String STATS_FILE = "stats.txt";

    public record PlayerRecord(String name, int[] levelWins) {

        public String toLine() {
            String wins = Arrays.stream(levelWins)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining(";"));
            return name + ";" + wins;
        }

        public static PlayerRecord fromLine(String line) {
            String[] parts = line.split(";");
            String name = parts[0];
            int[] wins = new int[parts.length - 1];
            for (int i = 0; i < wins.length; i++) {
                wins[i] = Integer.parseInt(parts[i + 1]);
            }
            return new PlayerRecord(name, wins);
        }

        public int getTotalWins() {
            return Arrays.stream(levelWins).sum();
        }
    }

    public static void updateStats(GameModel model, int completedLevelId) {
        List<PlayerRecord> records = loadAll();
        String currentName = model.getPlayerName();
        int totalLevels = model.getTotalLevels();

        Optional<PlayerRecord> existing = records.stream()
                .filter(r -> r.name().equalsIgnoreCase(currentName))
                .findFirst();

        int[] wins = new int[totalLevels];

        if (existing.isPresent()) {
            PlayerRecord old = existing.get();
            System.arraycopy(old.levelWins(), 0, wins, 0, Math.min(old.levelWins().length, totalLevels));
            records.remove(old);
        }

        if (completedLevelId > 0 && completedLevelId <= totalLevels) {
            wins[completedLevelId - 1]++;
        }

        records.add(new PlayerRecord(currentName, wins));
        saveAll(records);
    }

    public static List<PlayerRecord> getTopPlayers() {
        return loadAll().stream()
                .sorted(Comparator.comparingInt(PlayerRecord::getTotalWins).reversed())
                .collect(Collectors.toList());
    }

    private static List<PlayerRecord> loadAll() {
        try {
            if (!Files.exists(Paths.get(STATS_FILE))) return new ArrayList<>();
            return Files.lines(Paths.get(STATS_FILE))
                    .map(PlayerRecord::fromLine)
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private static void saveAll(List<PlayerRecord> records) {
        try (PrintWriter out = new PrintWriter(new FileWriter(STATS_FILE))) {
            records.forEach(r -> out.println(r.toLine()));
        } catch (IOException ignored) {}
    }

    public static void loadProgressForPlayer(GameModel model) {
        List<PlayerRecord> records = loadAll();
        String name = model.getPlayerName();
        int totalLevels = model.getTotalLevels();

        records.stream()
                .filter(r -> r.name().equalsIgnoreCase(name))
                .findFirst()
                .ifPresent(r -> {

                    model.unlockLevel(1);
                    int[] wins = r.levelWins();
                    for (int i = 0; i < wins.length; i++) {

                        if (wins[i] > 0 && (i + 2) <= totalLevels) {
                            model.unlockLevel(i + 2);
                        }
                    }
                });
    }
}