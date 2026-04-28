package threadpool;

import factory.Tasks.Task;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ThreadPool {
    private final List<Task> taskQueue = new LinkedList<>();//?
    private final ArrayList<Thread> threads;//List



    public ThreadPool(int threadCount, int listSize) {
        this.threads = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; ++i) {

            Thread thread = new Thread(new PooledThread(taskQueue));
            threads.add(thread);
        }
    }

    public void addTask(Task task) {
        synchronized (taskQueue) {
            taskQueue.add(task);
            taskQueue.notifyAll();
        }
    }

    public void start() {
        for (Thread thread : threads) {
            thread.start();
        }
    }

    public void stop() {
        for (Thread thread : threads) {
            thread.interrupt();
        }
        synchronized (taskQueue) {
            taskQueue.notifyAll();
        }
    }

    public synchronized  int getTaskQueueSize() {
        return taskQueue.size();
    }
}