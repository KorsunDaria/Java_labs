package threadpool;

import factory.Tasks.Task;


import java.util.List;


public class PooledThread implements Runnable { //Runnable
    private final List<Task> tasks;

    public PooledThread(List<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Task task;
            synchronized (tasks) {
                try {
                    while (tasks.isEmpty()) {
                        tasks.wait();
                    }
                    task = tasks.removeFirst();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            if (task != null) {
                task.execute();
            }
        }
    }

}