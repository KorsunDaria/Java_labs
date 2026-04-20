package org.example.factory;

import org.example.factory.details.Product;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class Storage<T extends Product> {
    public final int capacity;
    private  final Queue<T> items = new LinkedList<>();
    private int totalProduced = 0;

    public Storage(int capacity) {
        this.capacity = capacity;
    }
    public synchronized void  put(T item) throws InterruptedException {
        while (isFull()){
            wait();
        }
        items.add(item);
        totalProduced++;
        notifyAll();
    }

    public  synchronized T get() throws InterruptedException{
        while ((items.isEmpty())) {
            wait();
        }
        T item = items.poll();
        notifyAll();
        return item;
    }




    private boolean isFull() {return items.size()>= capacity;}

    public synchronized  int getTotalProduced(){return totalProduced;}
    public  synchronized  int getItemsNumber(){ return items.size();}
    public  int getCapacity(){ return capacity;}


}
