package factory.Tasks;

import factory.Storage;
import factory.details.Accessory;
import factory.details.Body;
import factory.details.Car;
import factory.details.Motor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.IDGenerator;

import java.io.Serializable;

public class MakeCar implements Task, Serializable {
    private final Storage<Body> bodyStorage;
    private final Storage<Motor> motorStorage;
    private final Storage<Accessory> accessoryStorage;
    private final Storage<Car> carStorage;
    private static final Logger log = LoggerFactory.getLogger(MakeCar.class);

    public MakeCar(Storage<Body> bodyStorage, Storage<Motor> motorStorage,
                   Storage<Accessory> accessoryStorage, Storage<Car> carStorage) {
        this.bodyStorage = bodyStorage;
        this.motorStorage = motorStorage;
        this.accessoryStorage = accessoryStorage;
        this.carStorage = carStorage;
    }

    public void execute() {
        Body body = null;
        Motor motor = null;
        Accessory accessory = null;
        try {
            body = bodyStorage.get();
            motor = motorStorage.get();
            accessory = accessoryStorage.get();
            String id = IDGenerator.generateID(Car.class);
            Car car = new Car(id, body, motor, accessory);
            carStorage.put(car);

            body = null; motor = null; accessory = null;
        } catch (InterruptedException e) {
            log.warn("Car assembly interrupted. Returning parts to storage...");
            try {
                if (body != null) bodyStorage.put(body);
                if (motor != null) motorStorage.put(motor);
                if (accessory != null) accessoryStorage.put(accessory);
            } catch (InterruptedException ex) {
                log.error("Failed to return parts: double interrupt!");
                Thread.currentThread().interrupt();
            }
            Thread.currentThread().interrupt();
        }
    }

}