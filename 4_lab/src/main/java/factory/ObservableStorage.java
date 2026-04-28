package factory;

import factory.details.Car;
import factory.entity.Observable;
import factory.entity.Observer;

import java.io.Serializable;
import java.util.ArrayList;

public class ObservableStorage extends Storage<Car> implements Observable, Serializable {
    private final ArrayList<Observer> observers; //list

    public ObservableStorage(int capacity) {
        super(capacity);
        observers = new ArrayList<>();
    }

    @Override
    public void register(Observer o) {
        observers.add(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update(Event.CAR_DEMAND);
        }
    }

    @Override
    public Car get() throws InterruptedException {
        Car car = super.get();
        notifyObservers(); // за блок синхронизации и после get
        return car;
    }
}