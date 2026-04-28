package factory.entity;

import factory.Storage;
import factory.details.Car;
import static java.lang.Thread.sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dealer extends DelayedEntity implements Runnable {
    private final Storage<Car> carStorage;
    private final int number;
    private final boolean logSale;
    private static final Logger log = LoggerFactory.getLogger(Dealer.class);

    public Dealer(Storage<Car> carStorage, int delay, int number, boolean logSale) {
        super(delay);
        this.carStorage = carStorage;
        this.number = number + 1;
        this.logSale = logSale;
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            try {
                Car car = carStorage.get();
                if (logSale) {
                    log.info("Dealer {}: Car {} (Body {}, Motor {}, Accessory {})",
                            number, car.getId(), car.getBodyId(), car.getMotorId(), car.getAccessoryId());
                }
                sleep(getDelay());
            } catch (InterruptedException e) {
                log.info("Entity {} stopped via interrupt.", getClass().getSimpleName());
                Thread.currentThread().interrupt();
                //break;
            }
        }
    }
}
