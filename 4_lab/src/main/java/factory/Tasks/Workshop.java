package factory.Tasks;

import factory.Storage;
import factory.details.Accessory;
import factory.details.Body;
import factory.details.Car;
import factory.details.Motor;
import threadpool.ThreadPool;

import java.io.Serializable;

public class Workshop implements Serializable {
    private final ThreadPool threadPool;
    private final Storage<Body> bodyStorage;
    private final Storage<Motor> motorStorage;
    private final Storage<Accessory> accessoryStorage;
    private final Storage<Car> carStorage;
    private final int workersNum;

    public Workshop(int workersNum, Storage<Body> bodyStorage, Storage<Motor> motorStorage,
                    Storage<Accessory> accessoryStorage, Storage<Car> carStorage) {
        threadPool = new ThreadPool(workersNum, 1000);
        this.bodyStorage = bodyStorage;
        this.motorStorage = motorStorage;
        this.accessoryStorage = accessoryStorage;
        this.carStorage = carStorage;
        this.workersNum = workersNum;
    }

    public void assembleCar() {
        threadPool.addTask(new MakeCar(bodyStorage, motorStorage, accessoryStorage, carStorage));
    }

    public int getPendingOrders() {
        return threadPool.getTaskQueueSize();
    }

    public void start() { threadPool.start(); }

    public void stop() { threadPool.stop(); }

    public int getWorkersNum() { return this.workersNum; }
}