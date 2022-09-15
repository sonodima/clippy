package com.sonodima.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread {
    private final ServerSocket serverSocket;
    private final ArrayList<ClientHandler> clientHandlers;
    private boolean isRunning;

    public Server(int port) throws IOException {
        super();

        serverSocket = new ServerSocket(port);
        clientHandlers = new ArrayList<>();
        isRunning = true;

        start();
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                // Listen for clients
                Socket client = this.serverSocket.accept();
                clientHandlers.add(new ClientHandler(client));
                clientConnectedListener.onClientConnected(client);
            } catch (IOException ignored) { }
        }
    }

    protected class ClientHandler extends Thread {
        private final Socket client;
        private final ObjectOutputStream outputStream;
        private final ObjectInputStream inputStream;
        private boolean isRunning;

        public ClientHandler(Socket client) throws IOException {
            super();

            this.client = client;
            outputStream = new ObjectOutputStream(client.getOutputStream());
            inputStream = new ObjectInputStream(client.getInputStream());
            isRunning = true;

            start();
        }

        @Override
        public void run() {
            while (isRunning) {
                try {
                    Object read = inputStream.readObject();
                    if (read != null) {
                        messageReceivedListener.onMessageReceived(read, client);
                    }
                }
                catch (Exception exception) {
                    try {
                        if (inputStream.read() == -1) {
                            stopHandler();
                            destroyClientHandler(this, client);
                            return;
                        }
                    } catch (IOException ignored) { }

                    // ︻╦╤─ You shut up, I won't tell anybody.
                }

                // Sleep for 100ms every iteration. We do not want to take a whole thread.
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) { }
            }
        }

        public void send(Object data) throws IOException {
            outputStream.writeObject(data);
        }

        public void stopHandler() throws IOException {
            client.close();
            outputStream.close();
            inputStream.close();
            isRunning = false;
        }
    }

    public void broadcast(Object data) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                clientHandler.send(data);
            } catch (IOException ignored) { }
        }
    }

    public void broadcast(Object data, Socket source) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (!source.equals(clientHandler.client)) {
                try {
                    clientHandler.send(data);
                } catch (IOException ignored) { }
            }
        }
    }

    public void destroyClientHandler(ClientHandler handler, Socket client) {
        clientHandlers.remove(handler);
        clientDisconnectedListener.onClientDisconnected(client);
    }

    public void stopServer() throws IOException {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                clientHandler.stopHandler();
            } catch (IOException ignored) { }
        }

        serverSocket.close();
        clientHandlers.clear();
        isRunning = false;
    }

    public int getConnectedClients() {
        return clientHandlers.size();
    }

    public boolean isServerRunning() {
        return isRunning;
    }

    public interface ClientConnectedListener {
        void onClientConnected(Socket client);
    }

    public interface ClientDisconnectedListener {
        void onClientDisconnected(Socket client);
    }

    public interface MessageReceivedListener {
        void onMessageReceived(Object data, Socket source);
    }

    private ClientConnectedListener clientConnectedListener;
    private ClientDisconnectedListener clientDisconnectedListener;
    private MessageReceivedListener messageReceivedListener;

    public void setClientConnectedListener(ClientConnectedListener listener) {
        clientConnectedListener = listener;
    }

    public void setClientDisconnectedListener(ClientDisconnectedListener listener) {
        clientDisconnectedListener = listener;
    }

    public void setMessageReceivedListener(MessageReceivedListener listener) {
        messageReceivedListener = listener;
    }
}