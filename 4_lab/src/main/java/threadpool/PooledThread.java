package threadpool;

import factory.Tasks.Task;
import java.util.concurrent.BlockingQueue;

public class PooledThread implements Runnable { //Runnable
    private final BlockingQueue<Task> tasks;

    public PooledThread(BlockingQueue<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Task task = tasks.take();
                task.execute();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); //log
                break;
            }
        }
    }

}