import threadpool.ThreadPool;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class ThreadPoolTest {

    @Test
    void testThreadPoolExecutesTasks() throws InterruptedException {
        ThreadPool pool = new ThreadPool(2, 5);
        pool.start();
        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 10; i++) {
            pool.addTask(counter::incrementAndGet);
        }
        Thread.sleep(500);
        assertEquals(10, counter.get());
        pool.stop();
    }

    @Test
    void testThreadPoolStops() throws InterruptedException {
        ThreadPool pool = new ThreadPool(2, 5);
        pool.start();
        AtomicBoolean taskExecuted = new AtomicBoolean(false);
        pool.addTask(() -> {
            try {
                Thread.sleep(1000);
                taskExecuted.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        Thread.sleep(50);
        pool.stop();
        Thread.sleep(200);
        assertFalse(taskExecuted.get());
    }

    @Test
    void testThreadPoolStopsWithWaitingTasks() throws InterruptedException {
        ThreadPool pool = new ThreadPool(2, 5);
        pool.start();
        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 5; i++) {
            pool.addTask(() -> {
                try {
                    Thread.sleep(100);
                    counter.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        Thread.sleep(150);
        pool.stop();
        assertEquals(2, counter.get());
        assertEquals(1, pool.getTaskQueueSize());
    }
}

