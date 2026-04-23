package factory.Tasks;

import factory.Storage;
import factory.details.Accessory;
import factory.details.Body;
import factory.details.Car;
import factory.details.Motor;
import utilities.IDGenerator;

public class MakeCar implements Task {
    private final Storage<Body> bodyStorage;
    private final Storage<Motor> motorStorage;
    private final Storage<Accessory> accessoryStorage;
    private final Storage<Car> carStorage;

    public MakeCar(Storage<Body> bodyStorage, Storage<Motor> motorStorage,
                   Storage<Accessory> accessoryStorage, Storage<Car> carStorage) {
        this.bodyStorage = bodyStorage;
        this.motorStorage = motorStorage;
        this.accessoryStorage = accessoryStorage;
        this.carStorage = carStorage;
    }

    public void execute() {
        try {
            Body body = bodyStorage.get();
            Motor motor = motorStorage.get();
            Accessory accessory = accessoryStorage.get();
            String id = IDGenerator.generateID(Car.class);
            Car car = new Car(id, body, motor, accessory);
            carStorage.put(car); // чтобы детали не потерялись
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); //убрать (вернуть детали)
        }
    }
}