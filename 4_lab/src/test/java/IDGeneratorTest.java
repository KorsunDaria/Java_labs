import org.junit.jupiter.api.Test;
import utilities.IDGenerator;
import factory.details.*;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class IDGeneratorTest {
    @Test
    void testGenerateUniqueIds() {
        Set<String> ids = new HashSet<>();

        for (int i = 0; i < 100; ++i) {
            String bodyId = IDGenerator.generateID(Body.class);
            String motorId = IDGenerator.generateID(Motor.class);
            String accessoryId = IDGenerator.generateID(Accessory.class);
            String carId = IDGenerator.generateID(Car.class);

            assertTrue(bodyId.matches("BDY\\d+"));
            assertTrue(motorId.matches("MTR\\d+"));
            assertTrue(accessoryId.matches("ACS\\d+"));
            assertTrue(carId.matches("CAR\\d+"));

            assertTrue(ids.add(bodyId));
            assertTrue(ids.add(motorId));
            assertTrue(ids.add(accessoryId));
            assertTrue(ids.add(carId));
        }
    }
}