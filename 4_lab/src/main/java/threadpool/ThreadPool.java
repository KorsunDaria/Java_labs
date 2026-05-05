package threadpool;

import factory.Tasks.Task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ThreadPool {
    private final List<Task> taskQueue = new LinkedList<>();//?
    private final List<Thread> threads;//List


    class PooledThread implements Runnable { //Runnable

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                Task task;
                synchronized (ThreadPool.this.taskQueue) {
                    try {
                        while (ThreadPool.this.taskQueue.isEmpty()) {
                            ThreadPool.this.taskQueue.wait(20);
                        }
                        task = ThreadPool.this.taskQueue.removeFirst();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                if (task != null) {
                    task.execute();
                }
            }
        }

    }

    public ThreadPool(int threadCount, int listSize) {
        this.threads = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; ++i) {

            Thread thread = new Thread(new PooledThread());
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
    }

    public synchronized  int getTaskQueueSize() {
        return taskQueue.size();
    }
}