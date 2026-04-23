package threadpool;

import factory.Tasks.Task;
import java.util.concurrent.BlockingQueue;

public class PooledThread extends Thread { //Runnable
    private final BlockingQueue<Task> tasks;

    public PooledThread(BlockingQueue<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                Task task = tasks.take();
                task.execute();
            } catch (InterruptedException e) {
                this.interrupt(); //log
                break;
            }
        }
    }

    public void stopThread() { this.interrupt(); }
}