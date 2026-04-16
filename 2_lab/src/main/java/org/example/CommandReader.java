package org.example;

import org.example.Commands.Command;
import org.example.Commands.CommandName;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CommandReader {

    private final CommandFactory factory;

    public CommandReader(CommandFactory factory) {
        this.factory = factory;
    }

    public List<String[]> read(File file) throws FileNotFoundException {
        List<String[]> commands = new ArrayList<>();
        Scanner sc = new Scanner(file);

        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            commands.add(Single(line));
        }

        return commands;
    }
    public String[] Single(String line) {
        return line.trim().split("\\s+");
    }
}
