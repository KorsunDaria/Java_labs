package factory;

import factory.Tasks.MakeCar;
import factory.details.Accessory;
import factory.details.Body;
import factory.details.Motor;
import factory.Tasks.Workshop;
import factory.entity.Dealer;
import factory.entity.Supplier;
import gui.FactoryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Factory implements FactoryListener {
    private Config config;

    private Storage<Body> bodyStorage;
    private Storage<Motor> motorStorage;
    private Storage<Accessory> accessoryStorage;
    private ObservableStorage carStorage;
    private static final Logger log = LoggerFactory.getLogger(Factory.class);

    private List<Supplier<Body>> bodySuppliers;
    private List<Supplier<Motor>> motorSuppliers;
    private List<Supplier<Accessory>> accessorySuppliers;
    private List<Dealer> dealers;

    private Workshop workshop;

    private ArrayList<Thread> allThreads;

    private void initStorages() {
        bodyStorage = new Storage<>(config.getInt("StorageBodySize"));
        accessoryStorage = new Storage<>(config.getInt("StorageAccessorySize"));
        motorStorage = new Storage<>(config.getInt("StorageMotorSize"));
        carStorage = new ObservableStorage(config.getInt("StorageCarSize"));
    }

    private void initWorkshop() {
        workshop = new Workshop(config.getInt("Workers"), bodyStorage,
                motorStorage, accessoryStorage, carStorage);
    }

    private void initController() {
        Controller controller = new Controller(workshop, carStorage);
        carStorage.register(controller);
    }

    private void initSuppliers() {
        bodySuppliers = new ArrayList<>();
        int bodySuppliersNum = config.getInt("BodySuppliers");
        int bodyDelay = config.getInt("BodySupplierDelay");
        for (int i = 0; i < bodySuppliersNum; ++i) {
            bodySuppliers.add(new Supplier<>(Body.class, bodyStorage, bodyDelay));
        }
        motorSuppliers = new ArrayList<>();
        int motorSuppliersNum = config.getInt("MotorSuppliers");
        int motorDelay = config.getInt("MotorSupplierDelay");
        for (int i = 0; i < motorSuppliersNum; ++i) {
            motorSuppliers.add(new Supplier<>(Motor.class, motorStorage, motorDelay));
        }
        accessorySuppliers = new ArrayList<>();
        int accessorySuppliersNum = config.getInt("AccessorySuppliers");
        int accessoryDelay = config.getInt("AccessorySupplierDelay");
        for (int i = 0; i < accessorySuppliersNum; ++i) {
            accessorySuppliers.add(new Supplier<>(Accessory.class, accessoryStorage, accessoryDelay));
        }
    }

    private void initDealers() {
        int dealersNum = config.getInt("Dealers");
        int dealerDelay = config.getInt("DealerDelay");
        boolean logSale = config.getBoolean("LogSale");
        dealers = new ArrayList<>();
        for (int i = 0; i < dealersNum; ++i) {
            dealers.add(new Dealer(carStorage, dealerDelay, i, logSale));
        }
    }

    public Factory(Config config) {
        this.config = config;
        initStorages();
        loadState();
        initSuppliers();
        initDealers();
    }

    @Override
    public void start() {
        allThreads = new ArrayList<>();

        allThreads.addAll(bodySuppliers.stream().map(Thread.ofVirtual()::start).toList());
        allThreads.addAll(motorSuppliers.stream().map(Thread.ofVirtual()::start).toList());
        allThreads.addAll(accessorySuppliers.stream().map(Thread.ofVirtual()::start).toList());

        initWorkshop();
        workshop.start();
        initController();
        for (Dealer dealer : dealers) {
            Thread thread = new Thread(dealer);
            allThreads.add(thread);
            thread.start();
        }

        //carStorage.notifyObservers();

    }

    @Override
    public FactoryStat getFactoryStat() {
        return new FactoryStat(workshop.getPendingOrders(),
                bodyStorage.getItemsNum(), bodyStorage.getTotalProduced(),
                motorStorage.getItemsNum(), motorStorage.getTotalProduced(),
                accessoryStorage.getItemsNum(), accessoryStorage.getTotalProduced(),
                carStorage.getItemsNum(), carStorage.getTotalProduced());
    }

    @Override
    public void setDealerDelay(int delay) {
        for (Dealer dealer : dealers) {
            dealer.setDelay(delay);
        }
    }

    @Override
    public void setAccessorySupplierDelay(int delay) {
        for (Supplier<Accessory> accessorySupplier : accessorySuppliers) {
            accessorySupplier.setDelay(delay);
        }
    }

    @Override
    public void setBodySupplierDelay(int delay) {
        for (Supplier<Body> bodySupplier : bodySuppliers) {
            bodySupplier.setDelay(delay);
        }
    }

    @Override
    public void setMotorSupplierDelay(int delay) {
        for (Supplier<Motor> motorSupplier : motorSuppliers) {
            motorSupplier.setDelay(delay);
        }
    }

    @Override
    public void stop() {
        for (Thread thread : allThreads) {
            thread.interrupt();
        }
        workshop.stop();
    }

    public void saveState() {
        try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(new java.io.FileOutputStream("factory_state.ser"))) {
            out.writeObject(bodyStorage);
            out.writeObject(motorStorage);
            out.writeObject(accessoryStorage);
            out.writeObject(carStorage);
        } catch (java.io.IOException e) {
            log.error("Problem with saveState"+e.getMessage());
        }
    }

    public void loadState() {
        java.io.File file = new java.io.File("factory_state.ser");
        if (!file.exists()) return;

        try (java.io.ObjectInputStream in = new java.io.ObjectInputStream(new java.io.FileInputStream(file))) {
            this.bodyStorage = (Storage<Body>) in.readObject();
            this.motorStorage = (Storage<Motor>) in.readObject();
            this.accessoryStorage = (Storage<Accessory>) in.readObject();
            this.carStorage = (ObservableStorage) in.readObject();
        } catch (Exception e) {
            log.error("Problem with loadState"+e.getMessage());
        }
    }
}