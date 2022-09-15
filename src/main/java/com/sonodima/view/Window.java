package com.sonodima.view;

import javax.swing.*;
import java.awt.*;

public class Window extends JFrame {
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    private JButton serverListenButton;
    private JButton serverStopButton;
    private JLabel serverAddressLabel;
    private JLabel serverClientsLabel;
    private JLabel serverStatusLabel;
    private JButton clientConnectButton;
    private JButton clientDisconnectButton;
    private JTextField clientAddressField;
    private JLabel clientStatusLabel;
    private JTextPane logField;
    private JCheckBox encryptionToggle;
    private JPasswordField encryptionSecretField;

    public Window() {
        super("Clippy");

        setSize(500, 340);
        setMinimumSize(new Dimension(500, 340));
        setContentPane(mainPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public JTextPane getLogField() {
        return logField;
    }

    public JCheckBox getEncryptionToggle() {
        return encryptionToggle;
    }

    public JPasswordField getEncryptionSecretField() {
        return encryptionSecretField;
    }

    public JButton getServerListenButton() {
        return serverListenButton;
    }

    public JButton getServerStopButton() {
        return serverStopButton;
    }

    public JLabel getServerAddressLabel() {
        return serverAddressLabel;
    }

    public JLabel getServerClientsLabel() {
        return serverClientsLabel;
    }

    public JTextField getClientAddressField() {
        return clientAddressField;
    }

    public JButton getClientConnectButton() {
        return clientConnectButton;
    }

    public JButton getClientDisconnectButton() {
        return clientDisconnectButton;
    }

    public JLabel getServerStatusLabel() {
        return serverStatusLabel;
    }

    public JLabel getClientStatusLabel() {
        return clientStatusLabel;
    }
}
