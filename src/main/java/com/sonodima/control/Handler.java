package com.sonodima.control;

import com.sonodima.functions.Clip;
import com.sonodima.network.Client;
import com.sonodima.network.Packet;
import com.sonodima.network.Server;
import com.sonodima.utilities.Utils;
import com.sonodima.view.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Handler implements ActionListener {
    private final Window window;
    private final Utils utils;
    private final Path tempDirectory;

    private Clip clip;
    private Server server;
    private Client client;

    public Handler(Window window, Utils utils) {
        this.window = window;
        this.utils = utils;

        window.getServerListenButton().addActionListener(this);
        window.getServerStopButton().addActionListener(this);
        window.getClientConnectButton().addActionListener(this);
        window.getClientDisconnectButton().addActionListener(this);

        tempDirectory = Utils.createTempDirectory();
        if (tempDirectory != null) {
            utils.writeLog("Temp directory created", "normal");
        } else {
            utils.writeLog("Could not create temp directory", "error");
            return;
        }

        try {
            window.getServerAddressLabel().setText(String.format("Address: %s", InetAddress.getLocalHost().getHostAddress()));
        } catch (UnknownHostException ignored) { }

        initializeClipboard();
    }

    public void initializeClipboard() {
        clip = new Clip();
        utils.writeLog("Clipboard handler initialized", "normal");

        clip.setClipboardChangedListener((packet) -> {
            if (server != null && server.isServerRunning()) {
                server.broadcast(packet);
                utils.writeLog("Clipboard sent to clients", "normal");
            }

            if (client != null && client.isClientRunning()) {
                try {
                    client.send(packet);
                    utils.writeLog("Clipboard sent to server", "normal");
                } catch (IOException exception) {
                    utils.writeLog("Could not send clipboard to server", "warning");
                }
            }
        });
    }

    public void handlePacket(Packet packet) {
        clip.updateLastPacketTime();

        switch (packet.getType()) {
            case files -> {
                List<File> fileList = packet.getFileList();
                utils.writeLog(String.format("Handling clipboard with %d file(s)", fileList.size()), "normal");
                List<File> tempList = new ArrayList<>();
                for (int i = 0; i < fileList.size(); i++) {
                    String fileName = fileList.get(i).getName();
                    byte[] fileData = packet.getFileData(i);

                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(tempDirectory + File.separator + fileName);
                        fileOutputStream.write(fileData);
                        fileOutputStream.close();

                        utils.writeLog(String.format("File written > %s, %d bytes", fileName, fileData.length), "normal");
                        tempList.add(new File(tempDirectory + File.separator + fileName));
                    } catch (IOException exception) {
                        utils.writeLog(String.format("Could not write file > %s", fileName), "warning");
                    }
                }
                clip.write(tempList);
            }
            case string -> {
                utils.writeLog("Handling clipboard with text", "normal");
                clip.write(packet.getString());
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == window.getServerListenButton()) {
            // Server listen
            try {
                server = new Server(8519);
            } catch (IOException exception) {
                utils.writeLog(exception.getMessage(), "error");
                utils.writeStatus(exception.getMessage(), "error", "server");
                return;
            }

            utils.writeLog("Server listening", "success");
            utils.writeStatus("Server listening", "success", "server");

            window.getServerListenButton().setEnabled(false);
            window.getServerStopButton().setEnabled(true);

            server.setClientConnectedListener((client) -> {
                utils.writeLog(String.format("Client connected > %s", client.getInetAddress().getHostAddress()), "success");
                window.getServerClientsLabel().setText(String.format("Clients: %d", server.getConnectedClients()));
            });

            server.setClientDisconnectedListener((client) -> {
                utils.writeLog(String.format("Client disconnected > %s", client.getInetAddress().getHostAddress()), "normal");
                window.getServerClientsLabel().setText(String.format("Clients: %d", server.getConnectedClients()));
            });

            server.setMessageReceivedListener((data, source) -> {
                //server.broadcast(data, source);

                handlePacket((Packet) data);
            });
        } else if (event.getSource() == window.getServerStopButton()) {
            // Server stop
            try {
                server.stopServer();
            } catch (IOException exception) {
                utils.writeLog(exception.getMessage(), "warning");
                utils.writeStatus(exception.getMessage(), "error", "warning");
                return;
            }

            utils.writeLog("Server stopped", "warning");
            utils.writeStatus("Server stopped", "warning", "server");
            window.getServerClientsLabel().setText(String.format("Clients: %d", server.getConnectedClients()));

            window.getServerListenButton().setEnabled(true);
            window.getServerStopButton().setEnabled(false);
        } else if (event.getSource() == window.getClientConnectButton()) {
            // Client connect
            String address = window.getClientAddressField().getText();
            if (!Utils.validateAddress(address)) {
                utils.writeLog("Address is not a valid IPv4", "warning");
                utils.writeStatus("Address is not a valid IPv4", "warning", "client");
                return;
            }

            try {
                client = new Client(InetAddress.getByName(address), 8519);
            } catch (UnknownHostException exception) {
                utils.writeLog("Host is unreachable", "error");
                utils.writeStatus("Host is unreachable", "error", "client");
                return;
            } catch (IOException exception) {
                utils.writeLog(exception.getMessage(), "error");
                utils.writeStatus(exception.getMessage(), "error", "client");
                return;
            }

            utils.writeLog("Connected to server", "success");
            utils.writeStatus("Connected to server", "success", "client");

            window.getClientConnectButton().setEnabled(false);
            window.getClientDisconnectButton().setEnabled(true);
            window.getClientAddressField().setEnabled(false);

            client.setMessageReceivedListener((data) -> handlePacket((Packet) data));

            client.setServerDisconnectedListener(() -> {
                utils.writeLog("Server connection lost", "error");
                utils.writeStatus("Server connection lost", "error", "client");

                window.getClientConnectButton().setEnabled(true);
                window.getClientDisconnectButton().setEnabled(false);
                window.getClientAddressField().setEnabled(true);
            });
        } else if (event.getSource() == window.getClientDisconnectButton()) {
            // Client disconnect
            try {
                client.stopClient();
            } catch (IOException exception) {
                utils.writeLog(exception.getMessage(), "error");
                utils.writeStatus(exception.getMessage(), "error", "client");
                return;
            }

            utils.writeLog("Disconnected from server", "normal");
            utils.writeStatus("Disconnected from server", "normal", "client");

            window.getClientConnectButton().setEnabled(true);
            window.getClientDisconnectButton().setEnabled(false);
            window.getClientAddressField().setEnabled(true);
        }
    }
}
