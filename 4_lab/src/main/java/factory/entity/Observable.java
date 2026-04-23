package factory.entity;

public interface Observable {
    void register(Observer o);
    void notifyObservers();
}
