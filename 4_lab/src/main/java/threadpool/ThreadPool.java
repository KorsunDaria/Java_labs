package threadpool;

import factory.Tasks.Task;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ThreadPool {
    private final BlockingQueue<Task> taskQueue;//?
    private final ArrayList<Thread> threads;//List

    private void createPooledThreads(int threadCount) {
        for (int i = 0; i < threadCount; ++i) {
            Thread thread = new Thread(new PooledThread(taskQueue));
            threads.add(thread);
        }
    }

    public ThreadPool(int threadCount, int taskQueueSize) {
        taskQueue = new ArrayBlockingQueue<>(taskQueueSize);
        threads = new ArrayList<>(threadCount);
        createPooledThreads(threadCount);
    }

    public void addTask(Task task) {
        try {
            taskQueue.put(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
    }

    public synchronized  int getTaskQueueSize() {
        return taskQueue.size();
    }
}