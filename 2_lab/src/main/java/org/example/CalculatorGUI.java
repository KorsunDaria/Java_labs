package org.example;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.util.Collections;
import java.util.List;

public class CalculatorGUI extends JFrame {

    private final JTextArea stackArea = new JTextArea("STACK:\n\n");
    private final JTextPane historyArea = new JTextPane();
    private final JTextArea paramsArea = new JTextArea("PARAMS:\n\n");
    private final JTextField inputField = new JTextField();

    private Calculator calculator;
    private Context context;
    private CommandReader reader;

    public CalculatorGUI() {
        super("Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLayout(new BorderLayout());

        stackArea.setEditable(false);
        paramsArea.setEditable(false);

        historyArea.setEditable(false);
        historyArea.setBackground(Color.WHITE);
        appendToHistory("SYSTEM READY\n", Color.BLACK);

        JPanel center = new JPanel(new GridLayout(1, 3));
        center.add(new JScrollPane(stackArea));
        center.add(new JScrollPane(historyArea));
        center.add(new JScrollPane(paramsArea));
        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(inputField, BorderLayout.CENTER);

        JButton save = new JButton("SAVE");
        JButton load = new JButton("LOAD");

        JPanel btns = new JPanel();
        btns.add(save);
        btns.add(load);

        bottom.add(btns, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        inputField.addActionListener(e -> runSingle(inputField.getText()));
        save.addActionListener(e -> handleSave());
        load.addActionListener(e -> handleLoad());
    }

    public void setCalculator(Calculator calc, CommandFactory factory) {
        calculator = calc;
        context = calc.getContext();
        reader = new CommandReader(factory);
    }

    public void refresh(Context ctx, String cmd) {
        if (ctx == null) return;
        context = ctx;

        stackArea.setText("STACK:\n\n");
        for (int i = context.getStack().size() - 1; i >= 0; i--)
            stackArea.append(context.getStack().get(i) + "\n");

        paramsArea.setText("PARAMS:\n\n");
        context.getMap().forEach((k, v) ->
                paramsArea.append(k + " = " + v + "\n"));

        if (cmd != null)
            appendToHistory(cmd, Color.BLACK);
    }

    public void loadAndRunFile(File file) {
        try {
            execute(reader.read(file));
        } catch (Exception e) {
            appendToHistory("SYSTEM ERROR: " + e.getMessage(), Color.RED);
        }
    }

    public void appendToHistory(String msg, Color color, int size) {
        appendToHistory(msg, color);
    }


    private void runSingle(String line) {
        if (line.trim().isEmpty()) return;

        try {
            execute(Collections.singletonList(reader.Single(line)));
        } catch (Exception e) {
            appendToHistory("ERROR: " + e.getMessage(), Color.RED);
        }

        inputField.setText("");
    }

    private void execute(List<String[]> commands) throws Exception {
        List<ExecutionResult> results = calculator.run(commands);


        for (ExecutionResult r : results)
            appendToHistory(
                    r.hasError() ? "ERROR: " + r.getError() : r.getCommand(),
                    r.hasError() ? Color.RED : Color.BLACK
            );

        refresh(calculator.getContext(), null);
    }

    private void appendToHistory(String msg, Color color) {
        StyledDocument doc = historyArea.getStyledDocument();
        Style style = historyArea.addStyle("style", null);
        StyleConstants.setForeground(style, color);

        try {
            doc.insertString(doc.getLength(), msg + "\n", style);
            historyArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException ignored) {}
    }

    private void handleSave() {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(fc.getSelectedFile()))) {
            out.writeObject(context);
            appendToHistory("SYSTEM: Saved", Color.BLUE);
        } catch (Exception e) {
            appendToHistory("SAVE ERROR: " + e.getMessage(), Color.RED);
        }
    }

    private void handleLoad() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(fc.getSelectedFile()))) {
            context.updateFrom((Context) in.readObject());
            refresh(context, null);
            appendToHistory("SYSTEM: Loaded", Color.BLUE);
        } catch (Exception e) {
            appendToHistory("LOAD ERROR: " + e.getMessage(), Color.RED);
        }
    }
}
