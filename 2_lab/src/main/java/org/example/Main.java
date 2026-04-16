package org.example;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {

    public static void main(String[] args) {
        try {
            CommandFactory factory = new CommandFactory();
            CommandReader reader = new CommandReader(factory);
            Calculator calculator = new Calculator(factory);

            CalculatorGUI gui = new CalculatorGUI();
            gui.setCalculator(calculator, factory);

            gui.setVisible(true);

            if (args.length > 0) {
                List<String[]> commands = reader.read(new File(args[0]));
                List<ExecutionResult> results = calculator.run(commands);
                for (ExecutionResult r : results) {
                    if (r.hasError()) {
                        gui.appendToHistory("ERROR: " + r.getError(), java.awt.Color.RED, 14);
                    }
                }
                gui.refresh(calculator.getContext(), null);
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

// gui не передавать
// getCommand должен созлавать объект


// вынести валидацию и проверку совпадения типов
// вынести чтение файла
// добавить количество аргументов в анатацию
// добавть разные execute под разные тиаы
//нагрузочные тесты