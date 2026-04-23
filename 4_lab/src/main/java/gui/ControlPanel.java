package gui;

import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JSlider {
    private JLabel onStorage;
    private JLabel totalProduced;
    public ControlPanel(GridBagConstraints gbc, FactoryGUI window, String name) {
        super(JSlider.HORIZONTAL, 0, 5000, 2500);
        gbc.gridy = window.getCurrentRow();
        window.setCurrentRow(gbc.gridy + 1);
        JPanel textFiledPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        textFiledPanel.add(new JLabel(name + " delay (ms)"));
        if (!name.equals("Dealers")) {
            onStorage = new JLabel("On storage: ");
            textFiledPanel.add(onStorage);
            totalProduced = new JLabel("Produced: ");
            textFiledPanel.add(totalProduced);
        }
        window.add(textFiledPanel, gbc);
        gbc.gridy = window.getCurrentRow();
        window.setCurrentRow(gbc.gridy + 1);
        setMajorTickSpacing(1000);
        setMinorTickSpacing(500);
        setPaintTicks(true);
        setPaintLabels(true);
        window.add(this, gbc);
    }

    public void setOnStorage(int num) {
        onStorage.setText("On storage: " + num);
    }

    public void setTotalProduced(long num) {
        totalProduced.setText("Produced: " + num);
    }
}
