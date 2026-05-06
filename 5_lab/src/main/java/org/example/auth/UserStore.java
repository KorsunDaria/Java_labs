package org.example.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Хранит зарегистрированных пользователей в JSON-файле.
 * Пароли хранятся как SHA-256 хеши — исходный текст нигде не сохраняется.
 *
 * <p>Потокобезопасен: все мутирующие операции синхронизированы.
 */
public class UserStore {

    private static final Logger log = Logger.getLogger(UserStore.class.getName());
    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /** nick → passwordHash */
    private final Map<String, String> users = new ConcurrentHashMap<>();
    private final File storageFile;

    /**
     * Создаёт хранилище. Если файл существует — загружает данные из него.
     *
     * @param path путь к JSON-файлу хранилища
     */
    public UserStore(String path) {
        this.storageFile = new File(path);
        load();
    }

    /**
     * Регистрирует нового пользователя.
     *
     * @param nick     желаемый никнейм
     * @param password пароль в открытом виде
     * @return {@code true} если регистрация прошла успешно,
     *         {@code false} если ник уже занят
     */
    public synchronized boolean register(String nick, String password) {
        if (users.containsKey(nick)) return false;
        users.put(nick, hash(password));
        save();
        return true;
    }

    /**
     * Проверяет учётные данные существующего пользователя.
     *
     * @param nick     никнейм
     * @param password пароль в открытом виде
     * @return {@code true} если пара ник/пароль верна
     */
    public boolean authenticate(String nick, String password) {
        String stored = users.get(nick);
        return stored != null && stored.equals(hash(password));
    }

    /** Проверяет, зарегистрирован ли пользователь с таким ником. */
    public boolean exists(String nick) {
        return users.containsKey(nick);
    }

    // --------------------------------------------------------------- I/O

    @SuppressWarnings("unchecked")
    private void load() {
        if (!storageFile.exists()) return;
        try {
            Map<String, String> loaded = mapper.readValue(storageFile, Map.class);
            users.putAll(loaded);
            log.info("Loaded " + users.size() + " users from " + storageFile);
        } catch (IOException e) {
            log.warning("Could not load user store: " + e.getMessage());
        }
    }

    private void save() {
        try {
            storageFile.getParentFile().mkdirs();
            mapper.writeValue(storageFile, users);
        } catch (IOException e) {
            log.warning("Could not save user store: " + e.getMessage());
        }
    }

    // --------------------------------------------------------------- hash

    /**
     * Вычисляет SHA-256 хеш строки.
     *
     * @param input исходная строка
     * @return hex-строка хеша
     */
    static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}