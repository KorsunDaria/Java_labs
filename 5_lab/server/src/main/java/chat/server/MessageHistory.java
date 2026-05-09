package chat.server;

import chat.protocol.Message;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;


public class MessageHistory {

    private final Path historyFile;
    private final int  maxSize;

    public MessageHistory(String filePath, int maxSize) {
        this.historyFile = Path.of(filePath);
        this.maxSize     = maxSize;
    }


    public synchronized void append(Message msg) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                historyFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            String name = msg.getName() != null ? msg.getName() : "";
            String text = msg.getText() != null ? msg.getText().replace("\t", " ") : "";
            writer.write(name + "\t" + text);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Could not write history: " + e.getMessage());
        }
    }


    public synchronized List<Message> getLast() {
        List<Message> result = new ArrayList<>();
        if (!Files.exists(historyFile)) return result;

        try {
            List<String> lines = Files.readAllLines(historyFile);
            int from = Math.max(0, lines.size() - maxSize);
            for (int i = from; i < lines.size(); i++) {
                String line = lines.get(i);
                int tab = line.indexOf('\t');
                if (tab < 0) continue;

                String name = line.substring(0, tab);
                String text = line.substring(tab + 1);

                Message msg = new Message(Message.EVENT_MESSAGE);
                msg.setName(name);
                msg.setText(text);
                result.add(msg);
            }
        } catch (IOException e) {
            System.err.println("Could not read history: " + e.getMessage());
        }
        return result;
    }
}
