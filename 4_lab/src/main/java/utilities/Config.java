package utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.Properties;

public class Config {
    private final Properties properties;
    private static final Logger log = LoggerFactory.getLogger(Config.class);

    public Config(String path) {
        properties = new Properties();
        if (path != null) {
            try (FileInputStream fis = new FileInputStream(path)) {
                properties.load(fis);
                validateKeys();
                return;
            } catch (IOException e) {
                log.warn("Failed to load config from: {}", path);
            }
        }

        try (InputStream dis = getClass().getResourceAsStream("/default_config.txt")) {
            if (dis == null) {
                throw new RuntimeException("Default config not found");
            }
            properties.load(dis);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load any config");
        }
        validateKeys();
    }

    private void validateKeys() {
        String[] requiredKeys = {
          "StorageBodySize",
                "StorageMotorSize",
                "StorageAccessorySize",
                "StorageCarSize",
                "BodySuppliers",
                "MotorSuppliers",
                "AccessorySuppliers",
                "Dealers",
                "Workers",
                "BodySupplierDelay",
                "MotorSupplierDelay",
                "AccessorySupplierDelay",
                "DealerDelay",
                "LogSale"
        };

        for (String key : requiredKeys) {
            if (!properties.containsKey(key)) {
                throw new RuntimeException("Missing required config key: " + key);
            }
        }
    }

    public int getInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }
}
