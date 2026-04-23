import factory.Factory;
import factory.FactoryStat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utilities.Config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FactoryTest {
    @TempDir
    Path tempDir;

    private Factory factory;

    @BeforeEach
    void setUp() throws IOException {
        File configFile = tempDir.resolve("test_config.txt").toFile();
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("""
                    StorageBodySize=10
                    StorageMotorSize=10
                    StorageAccessorySize=10
                    StorageCarSize=10
                    BodySuppliers=1
                    MotorSuppliers=1
                    AccessorySuppliers=5
                    Workers=5
                    Dealers=2
                    BodySupplierDelay=50
                    MotorSupplierDelay=50
                    AccessorySupplierDelay=50
                    DealerDelay=100
                    LogSale=true
                    """);
        }
        Config config = new Config(configFile.getAbsolutePath());
        factory = new Factory(config);
    }

    @Test
    void testFactoryStartsAndProduces() throws InterruptedException {
        factory.start();
        Thread.sleep(2000);
        FactoryStat stat = factory.getFactoryStat();
        assertTrue(stat.bodiesProduced() > 0, "Bodies should be produced");
        assertTrue(stat.motorsProduced() > 0, "Motors should be produced");
        assertTrue(stat.accessoriesProduced() > 0, "Accessories should be produced");
        factory.stop();
    }
}