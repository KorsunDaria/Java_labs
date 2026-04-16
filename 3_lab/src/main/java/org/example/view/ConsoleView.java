package org.example.view;

import org.example.util.StatsManager;
import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConsoleView {

    private boolean running = true;

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Консоль управления Bikini Bottom ===");

        while (running) {
            System.out.println("\nДоступные команды:");
            System.out.println("1. Посмотреть файлы сохранений");
            System.out.println("2. Удалить файл сохранения");
            System.out.println("3. Посмотреть таблицу лидеров (статистику)"); // Новая команда
            System.out.println("4. Очистить всю статистику");
            System.out.println("5. Выход из консоли");
            System.out.print("Выберите действие: ");

            String input = scanner.nextLine();
            switch (input) {
                case "1" -> viewSaves();
                case "2" -> deleteSave(scanner);
                case "3" -> viewStats(); // Вызов новой функции
                case "4" -> clearStats();
                case "5" -> running = false;
                default -> System.out.println("Неизвестная команда.");
            }
        }
    }

    // Новая функция для просмотра статистики
    private void viewStats() {
        List<StatsManager.PlayerRecord> tops = StatsManager.getTopPlayers();

        if (tops.isEmpty()) {
            System.out.println("Статистика пуста. Игроков пока нет.");
            return;
        }

        System.out.println("\n--- ТАБЛИЦА ЛИДЕРОВ ---");
        System.out.printf("%-15s | %-12s | %s%n", "Игрок", "Всего побед", "Детали по уровням");
        System.out.println("------------------------------------------------------------");

        for (StatsManager.PlayerRecord r : tops) {
            // Динамически формируем строку с уровнями (L1: 5, L2: 3...)
            String levelsDetail = IntStream.range(0, r.levelWins().length)
                    .mapToObj(i -> "L" + (i + 1) + ": " + r.levelWins()[i])
                    .collect(Collectors.joining(", "));

            System.out.printf("%-15s | %-12d | %s%n",
                    r.name(),
                    r.getTotalWins(),
                    levelsDetail);
        }
    }

    private void viewSaves() {
        File savesDir = new File("saves");
        if (!savesDir.exists() || !savesDir.isDirectory()) {
            System.out.println("Папка saves/ не найдена. Сохранений пока нет.");
            return;
        }

        // Исправил расширение на .ser, так как в коде выше вы проверяли .ser
        File[] files = savesDir.listFiles((dir, name) -> name.endsWith(".ser"));
        if (files == null || files.length == 0) {
            System.out.println("Файлов сохранений не найдено.");
            return;
        }

        System.out.println("Найдены файлы:");
        for (File f : files) {
            System.out.println(" - " + f.getName());
        }
    }

    private void deleteSave(Scanner scanner) {
        System.out.print("Введите имя файла для удаления (с расширением): ");
        String name = scanner.nextLine();
        File file = new File("saves/" + name);

        if (file.exists() && file.delete()) {
            System.out.println("Файл " + name + " успешно удален.");
        } else {
            System.out.println("Файл не найден ");
        }
    }

    private void clearStats() {
        File file = new File("stats.txt");
        if (file.exists() && file.delete()) {
            System.out.println("Глобальная статистика успешно очищена.");
        } else {
            System.out.println("Файл stats.txt не найден.");
        }
    }
}