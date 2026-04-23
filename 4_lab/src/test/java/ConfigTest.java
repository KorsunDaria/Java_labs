import utilities.Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {
    @TempDir
    Path tempDir;

    @Test
    void testLoadValidConfig() throws IOException {
        File configFile = tempDir.resolve("test_config.txt").toFile();
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("""
                StorageBodySize=100
                StorageMotorSize=100
                StorageAccessorySize=100
                StorageCarSize=100
                BodySuppliers=3
                MotorSuppliers=1
                AccessorySuppliers=5
                Dealers=20
                Workers=10
                BodySupplierDelay=2000
                MotorSupplierDelay=1500
                AccessorySupplierDelay=1000
                DealerDelay=3000
                LogSale=true
                """);
        }

        Config config = new Config(configFile.getAbsolutePath());
        assertEquals(100, config.getInt("StorageBodySize"));
        assertEquals(100, config.getInt("StorageMotorSize"));
        assertEquals(100, config.getInt("StorageAccessorySize"));
        assertEquals(100, config.getInt("StorageCarSize"));
        assertEquals(3, config.getInt("BodySuppliers"));
        assertEquals(1, config.getInt("MotorSuppliers"));
        assertEquals(5, config.getInt("AccessorySuppliers"));
        assertEquals(20, config.getInt("Dealers"));
        assertEquals(10, config.getInt("Workers"));
        assertEquals(2000, config.getInt("BodySupplierDelay"));
        assertEquals(1500, config.getInt("MotorSupplierDelay"));
        assertEquals(1000, config.getInt("AccessorySupplierDelay"));
        assertEquals(3000, config.getInt("DealerDelay"));
        assertTrue(config.getBoolean("LogSale"));
    }

    @Test
    void testMissingKeyThrowsException() throws IOException {
        File configFile = tempDir.resolve("invalid_config.txt").toFile();
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("""
                StorageBodySize=100
                StorageMotorSize=100
                StorageAccessorySize=100
                StorageCarSize=100
                BodySuppliers=3
                MotorSuppliers=1
                AccessorySuppliers=5
                Dealers=20
                Workers=10
                BodySupplierDelay=2000
                MotorSupplierDelay=1500
                AccessorySupplierDelay=1000
                DealerDelay=3000
                """);
        }

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            new Config(configFile.getAbsolutePath());
        });
        assertTrue(exception.getMessage().contains("Missing required config key: LogSale"));
    }

    @Test
    void testLoadDefaultConfig() {
        Config config = new Config("non_existent_file.txt");
        assertEquals(100, config.getInt("StorageBodySize"));
        assertEquals(100, config.getInt("StorageMotorSize"));
        assertEquals(100, config.getInt("StorageAccessorySize"));
        assertEquals(100, config.getInt("StorageCarSize"));
        assertEquals(5, config.getInt("BodySuppliers"));
        assertEquals(5, config.getInt("MotorSuppliers"));
        assertEquals(5, config.getInt("AccessorySuppliers"));
        assertEquals(10, config.getInt("Dealers"));
        assertEquals(15, config.getInt("Workers"));
        assertEquals(2500, config.getInt("BodySupplierDelay"));
        assertEquals(2500, config.getInt("MotorSupplierDelay"));
        assertEquals(2500, config.getInt("AccessorySupplierDelay"));
        assertEquals(2500, config.getInt("DealerDelay"));
        assertTrue(config.getBoolean("LogSale"));
    }
}