package chat.loadtest;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class LoadTest {

    private TestServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = new TestServer();
        server.start();

        sleep(100);
    }

    @AfterEach
    void stopServer() throws IOException {
        server.stop();
    }


    @Test
    @DisplayName("Нагрузочный: 1000 сообщений от одного клиента")
    void throughput_singleClient() throws Exception {
        int messageCount = 1000;

        try (TestClient client = server.connectClient("user_load")) {
            long start = System.currentTimeMillis();

            for (int i = 0; i < messageCount; i++) {
                client.sendMessage("message #" + i);
            }

            long elapsed = System.currentTimeMillis() - start;
            double throughput = messageCount * 1000.0 / elapsed;

            System.out.printf("[throughput_singleClient] %d сообщений за %d мс = %.1f msg/sec%n",
                    messageCount, elapsed, throughput);

            assertTrue(throughput > 100,
                    "Пропускная способность слишком низкая: " + throughput + " msg/sec");
        }
    }


    @Test
    @DisplayName("Нагрузочный: 50 клиентов подключаются одновременно")
    void throughput_manyClientsConnect() throws Exception {
        int clientCount = 50;
        CountDownLatch ready  = new CountDownLatch(clientCount);
        CountDownLatch done   = new CountDownLatch(clientCount);
        AtomicInteger errors  = new AtomicInteger(0);
        List<TestClient> clients = new CopyOnWriteArrayList<>();

        long start = System.currentTimeMillis();

        for (int i = 0; i < clientCount; i++) {
            final String name = "client_" + i;
            Thread.ofVirtual().start(() -> {
                try {
                    TestClient c = server.connectClient(name);
                    clients.add(c);
                    ready.countDown();
                } catch (IOException e) {
                    errors.incrementAndGet();
                    ready.countDown();
                } finally {
                    done.countDown();
                }
            });
        }

        boolean allConnected = ready.await(15, TimeUnit.SECONDS);
        long elapsed = System.currentTimeMillis() - start;

        System.out.printf("[manyClientsConnect] %d клиентов за %d мс, ошибок: %d%n",
                clientCount, elapsed, errors.get());


        for (TestClient c : clients) c.close();

        assertTrue(allConnected, "Не все клиенты подключились за 15 сек");
        assertEquals(0, errors.get(), "Были ошибки подключения: " + errors.get());
    }


    @Test
    @DisplayName("Нагрузочный: 10 клиентов по 100 сообщений одновременно")
    void throughput_concurrentSenders() throws Exception {
        int clientCount  = 10;
        int msgsPerClient = 100;
        int totalExpected = clientCount * msgsPerClient;

        List<TestClient> clients = new ArrayList<>();
        for (int i = 0; i < clientCount; i++) {
            clients.add(server.connectClient("sender_" + i));
        }

        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done     = new CountDownLatch(clientCount);
        AtomicInteger errors    = new AtomicInteger(0);
        AtomicInteger sent      = new AtomicInteger(0);

        for (TestClient client : clients) {
            Thread.ofVirtual().start(() -> {
                try {
                    startGun.await();
                    for (int i = 0; i < msgsPerClient; i++) {
                        client.sendMessage("hi from " + client.getUsername());
                        sent.incrementAndGet();
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        long start = System.currentTimeMillis();
        startGun.countDown(); // все стартуют одновременно
        boolean finished = done.await(30, TimeUnit.SECONDS);
        long elapsed = System.currentTimeMillis() - start;

        double throughput = sent.get() * 1000.0 / elapsed;
        System.out.printf("[concurrentSenders] %d сообщений за %d мс = %.1f msg/sec, ошибок: %d%n",
                sent.get(), elapsed, throughput, errors.get());

        for (TestClient c : clients) c.close();

        assertTrue(finished, "Не все клиенты завершили отправку за 30 сек");
        assertEquals(0, errors.get(), "Были ошибки при отправке");
        assertEquals(totalExpected, sent.get(), "Отправлено не всё");
    }

    @Test
    @DisplayName("Стресс: 200 клиентов одновременно")
    void stress_maxClients() throws Exception {
        int clientCount = 200;
        AtomicInteger connected = new AtomicInteger(0);
        AtomicInteger errors    = new AtomicInteger(0);
        CountDownLatch done     = new CountDownLatch(clientCount);
        List<TestClient> clients = new CopyOnWriteArrayList<>();

        for (int i = 0; i < clientCount; i++) {
            final String name = "stress_" + i;
            Thread.ofVirtual().start(() -> {
                try {
                    TestClient c = server.connectClient(name);
                    clients.add(c);
                    connected.incrementAndGet();
                } catch (IOException e) {
                    errors.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        done.await(30, TimeUnit.SECONDS);

        System.out.printf("[stress_maxClients] подключено: %d / %d, ошибок: %d%n",
                connected.get(), clientCount, errors.get());

        for (TestClient c : clients) {
            try { c.close(); } catch (IOException ignored) {}
        }

        int maxErrors = clientCount / 20;
        assertTrue(errors.get() <= maxErrors,
                "Слишком много ошибок подключения: " + errors.get() + " из " + clientCount);
    }


    @Test
    @DisplayName("Стресс: быстрые подключения и отключения (flood)")
    void stress_connectDisconnectFlood() throws Exception {
        int rounds      = 50;
        int perRound    = 5;
        AtomicInteger errors = new AtomicInteger(0);
        CountDownLatch done  = new CountDownLatch(rounds * perRound);

        long start = System.currentTimeMillis();

        for (int r = 0; r < rounds; r++) {
            final int round = r;
            for (int c = 0; c < perRound; c++) {
                final String name = "flood_" + round + "_" + c;
                Thread.ofVirtual().start(() -> {
                    try (TestClient client = server.connectClient(name)) {
                        client.sendMessage("ping");
                    } catch (IOException e) {
                        errors.incrementAndGet();
                    } finally {
                        done.countDown();
                    }
                });
            }
            sleep(20);
        }

        boolean finished = done.await(30, TimeUnit.SECONDS);
        long elapsed = System.currentTimeMillis() - start;

        System.out.printf("[stress_flood] %d подключений за %d мс, ошибок: %d%n",
                rounds * perRound, elapsed, errors.get());

        assertTrue(finished, "Не все клиенты завершили работу за 30 сек");

        int maxErrors = (rounds * perRound) / 10; // допускаем 10% ошибок при флуде
        assertTrue(errors.get() <= maxErrors,
                "Слишком много ошибок при флуде: " + errors.get());
    }

    @Test
    @DisplayName("Стресс: сервер не теряет сообщения (10 клиентов × 200 сообщений)")
    void stress_noMessageLoss() throws Exception {
        int senderCount   = 10;
        int msgsPerSender = 200;
        int totalSent     = senderCount * msgsPerSender;


        TestClient receiver = server.connectClient("receiver");
        AtomicInteger received = new AtomicInteger(0);


        receiver.getSocket().setSoTimeout(5000);
        Thread readerThread = Thread.ofVirtual().start(() -> {
            try {
                while (true) {
                    chat.protocol.message.Message msg = receiver.readMessage();
                    if (msg instanceof chat.protocol.message.EventMessageMsg) {
                        received.incrementAndGet();
                    }
                }
            } catch (IOException ignored) {}
        });


        List<TestClient> senders = new ArrayList<>();
        for (int i = 0; i < senderCount; i++) {
            senders.add(server.connectClient("sender_loss_" + i));
        }

        CountDownLatch done = new CountDownLatch(senderCount);
        for (TestClient sender : senders) {
            Thread.ofVirtual().start(() -> {
                try {
                    for (int i = 0; i < msgsPerSender; i++) {
                        sender.sendMessage("msg_" + i);
                    }
                } catch (IOException e) {
                    System.err.println("Ошибка отправки: " + e.getMessage());
                } finally {
                    done.countDown();
                }
            });
        }

        done.await(30, TimeUnit.SECONDS);
        sleep(2000);

        for (TestClient s : senders) s.close();
        receiver.close();
        readerThread.join(3000);

        System.out.printf("[stress_noMessageLoss] отправлено: %d, получено: %d%n",
                totalSent, received.get());


        int minExpected = (int) (totalSent * 0.99);
        assertTrue(received.get() >= minExpected,
                "Слишком много потерь: получено " + received.get() + " из " + totalSent);
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
