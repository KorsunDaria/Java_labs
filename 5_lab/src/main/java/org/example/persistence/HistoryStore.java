package org.example.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.protocol.Message;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class HistoryStore {

    private static final Logger log = Logger.getLogger(HistoryStore.class.getName());
    private static final int    MAX_STORED = 500; // максимум записей на диске
    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final File file;


    public HistoryStore(String path) {
        this.file = new File(path);
    }

    public synchronized void append(Message msg) {
        List<MessageRecord> history = readAll();
        history.add(MessageRecord.from(msg));
        if (history.size() > MAX_STORED) {
            history = history.subList(history.size() - MAX_STORED, history.size());
        }
        write(history);
    }


    public List<Message> load() {
        List<Message> result = new ArrayList<>();
        for (MessageRecord r : readAll()) {
            result.add(r.toMessage());
        }
        return result;
    }


    private List<MessageRecord> readAll() {
        if (!file.exists()) return new ArrayList<>();
        try {
            MessageRecord[] arr = mapper.readValue(file, MessageRecord[].class);
            return new ArrayList<>(List.of(arr));
        } catch (IOException e) {
            log.warning("Could not read history: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void write(List<MessageRecord> records) {
        try {
            file.getParentFile().mkdirs();
            mapper.writeValue(file, records);
        } catch (IOException e) {
            log.warning("Could not write history: " + e.getMessage());
        }
    }

    // ------------------------------------------- DTO для сериализации


    public static class MessageRecord {
        public String type;
        public String sender;
        public String body;
        public long   seqNum;

        public MessageRecord() {}

        static MessageRecord from(Message m) {
            MessageRecord r = new MessageRecord();
            r.type    = m.getType().name();
            r.sender  = m.getSender();
            r.body    = m.getBody();
            r.seqNum  = m.getSeqNum();
            return r;
        }

        Message toMessage() {
            return new Message(
                    Message.Type.valueOf(type),
                    sender, body, null, seqNum
            );
        }
    }
}