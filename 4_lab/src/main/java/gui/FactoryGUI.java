package gui;

import factory.FactoryStat;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;

public class FactoryGUI extends  JFrame implements ActionListener {
    private final FactoryListener factoryObserver;

    private int currentRow = 1;

    private final ControlPanel bodySupSlider;
    private final ControlPanel motorSupSlider;
    private final ControlPanel accessorySupSlider;
    private final ControlPanel dealerSlider;

    private final JLabel carsOnStorage;
    private final JLabel carsProduced;
    private final JLabel tasksInQueue;

    public FactoryGUI(FactoryListener factoryObserver) {
        this.factoryObserver = factoryObserver;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Factory");

        URL iconURL = getClass().getResource("/iconCar.jpg");
        if (iconURL == null) {
            throw new RuntimeException("Cannot find icon.jpg");
        }
        ImageIcon icon = new ImageIcon(iconURL);
        setIconImage(icon.getImage());

        setBounds(500, 250, 900, 550);
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(255, 255, 255));
        mainPanel.setLayout(new GridBagLayout());
        setContentPane(mainPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        bodySupSlider = new ControlPanel(gbc, this,"Body sup");
        bodySupSlider.addChangeListener(e -> {
            int delay = bodySupSlider.getValue();
            factoryObserver.setBodySupplierDelay(delay);
        });

        motorSupSlider = new ControlPanel(gbc, this, "Motor sup");
        motorSupSlider.addChangeListener(e -> {
            int delay = motorSupSlider.getValue();
            factoryObserver.setMotorSupplierDelay(delay);
        });

        accessorySupSlider = new ControlPanel(gbc, this, "Accessory sup");
        accessorySupSlider.addChangeListener(e -> {
            int delay = accessorySupSlider.getValue();
            factoryObserver.setAccessorySupplierDelay(delay);
        });

        dealerSlider = new ControlPanel(gbc, this, "Dealers");
        dealerSlider.addChangeListener(e -> {
            int delay = dealerSlider.getValue();
            factoryObserver.setDealerDelay(delay);
        });
        gbc.gridy = 1;
        gbc.gridx = 2;
        JPanel carInfo = new JPanel(new GridLayout(1, 3, 5, 10));

        carsOnStorage = new JLabel("Cars on storage: ");
        carInfo.add(carsOnStorage);
        carsProduced = new JLabel("Cars produced: ");
        carInfo.add(carsProduced);
        tasksInQueue = new JLabel("Cars in queue: ");
        carInfo.add(tasksInQueue);
        add(carInfo, gbc);
        gbc.gridx = 0;

        Timer timer = new Timer(10, this);
        timer.start();

        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                factoryObserver.stop();
                dispose();
                System.exit(0);
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        FactoryStat factoryStat = factoryObserver.getFactoryStat();
        bodySupSlider.setTotalProduced(factoryStat.bodiesProduced());
        bodySupSlider.setOnStorage(factoryStat.bodiesOnStorage());

        motorSupSlider.setTotalProduced(factoryStat.motorsProduced());
        motorSupSlider.setOnStorage(factoryStat.motorsOnStorage());

        accessorySupSlider.setTotalProduced(factoryStat.accessoriesProduced());
        accessorySupSlider.setOnStorage(factoryStat.accessoriesOnStorage());

        carsProduced.setText("Cars produced: " + factoryStat.carsProduced());
        tasksInQueue.setText("Cars in queue: " + factoryStat.queueSize());
        carsOnStorage.setText("Cars on storage: " + factoryStat.carsOnStorage());
    }

    public int getCurrentRow() { return currentRow; }

    public void setCurrentRow(int currentRow) {
        this.currentRow = currentRow;
    }
}
