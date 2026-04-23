import factory.Factory;
import gui.FactoryGUI;
import utilities.Config;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        Config config;
        if (args.length > 0) {
            config = new Config(args[0]);
        } else {
            config = new Config(null);
        }
        Factory factory = new Factory(config);
        factory.start();
        SwingUtilities.invokeLater(() -> {
            FactoryGUI factoryView = new FactoryGUI(factory);
            factoryView.setVisible(true);
        });
    }
}
