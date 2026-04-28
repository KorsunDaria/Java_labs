package factory;

import factory.details.*;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

public class Storage<T extends Product> implements Serializable {
    private final int capacity;
    private final Queue<T> items = new LinkedList<>();
    private int totalProduced = 0;

    public Storage(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void put(T item) throws InterruptedException {
        while (isFull()) {
            wait();
        }
        items.add(item);
        totalProduced++;
        notifyAll();
    }

    public synchronized T get() throws InterruptedException {
        while (items.isEmpty()) {
            wait();
        }
        T item = items.poll();
        notifyAll();
        return item;
    }

    private synchronized boolean isFull() {
        return items.size() >= capacity;
    }

    public synchronized int getTotalProduced() {
        return totalProduced;
    }

    public synchronized int getItemsNum() {
        return items.size();
    }

    public synchronized int getCapacity() {
        return capacity;
    }
}