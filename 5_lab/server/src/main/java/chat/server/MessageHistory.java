package chat.server;

import chat.protocol.message.*;

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


    public synchronized void append(EventMessageMsg msg) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                historyFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            String name = msg.getFromName() != null ? msg.getFromName() : "";
            String text = msg.getText() != null ? msg.getText().replace("\t", " ") : "";
            writer.write(name + "\t" + text);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Could not write history: " + e.getMessage());
        }
    }


    public synchronized List<EventMessageMsg> getLast() {
        List<EventMessageMsg> result = new ArrayList<>();
        if (!Files.exists(historyFile)) return result;

        try {
            List<String> lines = Files.readAllLines(historyFile);
            int from = Math.max(0, lines.size() - maxSize);
            for (int i = from; i < lines.size(); i++) {
                int tab = lines.get(i).indexOf('\t');
                if (tab < 0) continue;
                result.add(new EventMessageMsg(
                        lines.get(i).substring(0, tab),
                        lines.get(i).substring(tab + 1)));
            }
        } catch (IOException e) {
            System.err.println("Could not read history: " + e.getMessage());
        }
        return result;
    }
}
