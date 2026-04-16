package org.example;

import org.example.Commands.Command;
import org.example.Commands.CommandName;
import org.example.Exception.CalculatorException;
import org.example.Exception.FactoryException;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CommandFactory {

    private final Map<String, Class<? extends Command>> commands = new HashMap<>();

    public CommandFactory() throws CalculatorException {
        try {
            InputStream is = getClass().getResourceAsStream("/commands.txt");
            if (is == null) throw new FactoryException("commands.txt not found in classpath!");

            Scanner scanner = new Scanner(is);
            while (scanner.hasNextLine()) {
                String jarPath = scanner.nextLine().trim();
                if (jarPath.isEmpty()) continue;

                File file = new File(jarPath);
                if (!file.exists()) {

                    continue;
                }



                URL url = file.toURI().toURL();
                try (URLClassLoader loader = new URLClassLoader(new URL[]{url}, this.getClass().getClassLoader())) {
                    JarFile jarFile = new JarFile(file);
                    Enumeration<JarEntry> entries = jarFile.entries();

                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();

                        if (!name.endsWith(".class")) continue;

                        String className = name.replace("/", ".").replace(".class", "");
                        try {
                            Class<?> clazz = loader.loadClass(className);

                            if (Command.class.isAssignableFrom(clazz)) {
                                if (clazz.isAnnotationPresent(CommandName.class)) {
                                    CommandName meta = clazz.getAnnotation(CommandName.class);
                                    String cmdName = meta.value().toUpperCase();
                                    commands.put(cmdName, clazz.asSubclass(Command.class));

                                }
                            }
                        } catch (Throwable e) {
                            System.err.println("Failed to load class: " + className + " -> " + e.getMessage());
                        }
                    }
                }
            }



        } catch (Exception e) {
            throw new FactoryException("Factory init error: " + e.getMessage());
        }
    }

    public Command create(String name) throws Exception {
        Class<? extends Command> clazz = commands.get(name.toUpperCase());
        if (clazz == null) {

            return null;
        }
        return clazz.getDeclaredConstructor().newInstance();
    }

    public CommandName getMeta(String name) {
        Class<? extends Command> clazz = commands.get(name.toUpperCase());
        if (clazz == null) {
            return null;
        }
        return clazz.getAnnotation(CommandName.class);
    }



    public Map<String, Class<? extends Command>> getRegisteredCommands() {
        return commands;
    }
}
