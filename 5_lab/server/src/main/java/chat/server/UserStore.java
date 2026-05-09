package chat.server;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;


public class UserStore {

    private final Path       usersFile;
    private final Properties users = new Properties();

    public UserStore(String filePath) {
        this.usersFile = Path.of(filePath);
        load();
    }


    public synchronized String authenticate(String username, String password) {
        if (username == null || username.isBlank()) return "Name could de not empty";
        if (password == null || password.isBlank()) return "Passport could be not empty ";

        String hash = sha256(password);
        String stored = users.getProperty(username);

        if (stored == null) {
            users.setProperty(username, hash);
            save();
            return null;
        }

        if (!stored.equals(hash)) {
            return "Wrong passport";
        }
        return null;
    }

    private void load() {
        if (!Files.exists(usersFile)) return;
        try (InputStream in = Files.newInputStream(usersFile)) {
            users.load(in);
        } catch (IOException e) {
            System.err.println("Problem with loading client: " + e.getMessage());
        }
    }

    private void save() {
        try (OutputStream out = Files.newOutputStream(usersFile,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            users.store(out, "Chat users");
        } catch (IOException e) {
            System.err.println("Problem with writing client: " + e.getMessage());
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 недоступен", e);
        }
    }
}
