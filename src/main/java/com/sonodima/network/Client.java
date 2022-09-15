package com.sonodima.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends Thread {
    private final Socket client;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;
    private boolean isRunning;

    public Client(InetAddress address, int port) throws IOException {
        super();

        client = new Socket(address, port);
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
                    messageReceivedListener.onMessageReceived(read);
                }
            } catch (Exception exception) {
                try {
                    if (inputStream.read() == -1) {
                        stopClient();
                        serverDisconnectedListener.onServerDisconnected();
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

    public void stopClient() throws IOException {
        client.close();
        outputStream.close();
        inputStream.close();
        isRunning = false;
    }

    public void send(Object data) throws IOException {
        outputStream.writeObject(data);
    }

    public boolean isClientRunning() {
        return isRunning;
    }

    public interface MessageReceivedListener {
        void onMessageReceived(Object data);
    }

    public interface ServerDisconnectedListener {
        void onServerDisconnected();
    }

    private MessageReceivedListener messageReceivedListener;
    private ServerDisconnectedListener serverDisconnectedListener;

    public void setMessageReceivedListener(MessageReceivedListener listener) {
        messageReceivedListener = listener;
    }

    public void setServerDisconnectedListener(ServerDisconnectedListener listener) {
        serverDisconnectedListener = listener;
    }
}
