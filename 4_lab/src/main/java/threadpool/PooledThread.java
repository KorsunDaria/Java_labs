package threadpool;

import factory.Tasks.Task;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class PooledThread implements Runnable { //Runnable
    private final List<Task> tasks;

    public PooledThread(List<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Task task = null;
            synchronized (tasks) {
                try {
                    while (tasks.isEmpty()) {
                        tasks.wait();
                    }
                    task = ((LinkedList<Task>) tasks).removeFirst();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); //log
                    break;
                }
            }
            if (task != null) {
                task.execute();
            }
        }
    }

}