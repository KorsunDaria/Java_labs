package org.example.threadpool;

import java.util.concurrent.BlockingQueue;

public class PooledThread extends Thread {
    private final BlockingQueue<Task> tasks;
    public  PooledThread(BlockingQueue<Task> tasks){
        this.tasks = tasks;
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                Task task = tasks.take();
                task.execute();
            } catch (InterruptedException e) {
                this.interrupt();;
                break;
            }
        }
    }

    public  void stopThread() {this.interrupt();}
}
