package factory.entity;

import factory.Storage;
import factory.details.Product;
import utilities.IDGenerator;
import java.lang.reflect.Constructor;
import static java.lang.Thread.sleep;

public class Supplier<T extends Product> extends DelayedEntity implements Runnable {
    private final Storage<T> storage;
    private final Class<T> detailClass;
    private final Constructor<T> constructor;

    public Supplier(Class<T> detailClass, Storage<T> storage, int delay) {
        super(delay);
        this.detailClass = detailClass;
        this.storage = storage;
        try {
            this.constructor = detailClass.getDeclaredConstructor(String.class);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public T createDetail() {
        T detail;
        try {
            detail = constructor.newInstance(IDGenerator.generateID(detailClass));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return detail;
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            try {
                T detail = createDetail();
                storage.put(detail);
                sleep(getDelay());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
