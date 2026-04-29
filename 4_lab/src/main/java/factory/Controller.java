package factory;

import factory.Tasks.Workshop;
import factory.entity.Observer;

import java.io.Serializable;

public class Controller implements Observer, Serializable {
    private final Workshop workshop;
    private final ObservableStorage carStorage;
    private final int capacity;

    public Controller(Workshop workshop, ObservableStorage carStorage) {
        this.workshop = workshop;
        this.carStorage = carStorage;
        this.capacity = carStorage.getCapacity();
    }

    private int calculateOrder(int carsLeft, int pendingOrders) {
        int totalInProcess = carsLeft + pendingOrders;
        double fillPercent = (double) totalInProcess / capacity * 100;
        return (int) Math.max(1, Math.min(workshop.getWorkersNum(), (100 - fillPercent) / 10));
    }

    private void onCarDemand() {
        int carsLeft = carStorage.getItemsNum();
        int pendingOrders = workshop.getPendingOrders();
        int toProduce = calculateOrder(carsLeft, pendingOrders);
        for (int i = 0; i < toProduce; i++) {
            workshop.assembleCar();
        }
    }

    @Override
    public void update(Event e) {
        if (e == Event.CAR_DEMAND) {
            onCarDemand();
        }
    }
}