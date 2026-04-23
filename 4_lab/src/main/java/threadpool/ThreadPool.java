package threadpool;

import factory.Tasks.Task;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ThreadPool {
    private final BlockingQueue<Task> taskQueue;//?
    private final ArrayList<PooledThread> threads;//List

    private void createPooledThreads(int threadCount) {
        for (int i = 0; i < threadCount; ++i) {
            PooledThread thread = new PooledThread(taskQueue);
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
        for (PooledThread thread : threads) {
            thread.start();
        }
    }

    public void stop() {
        for (PooledThread thread : threads) {
            thread.stopThread();
        }
    }

    public synchronized  int getTaskQueueSize() {
        return taskQueue.size();
    }
}