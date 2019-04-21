package exn.database.remal.devices;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import exn.database.remal.core.RemAL;
import exn.database.remal.events.DeviceConnectEvent;
import exn.database.remal.events.DeviceDisconnectEvent;
import exn.database.remal.macros.ActionValidCallback;

/**
 * Handles device access through LAN connection
 */
public class RemoteWiFiDevice implements IRemoteDevice {
    private static final String HANDSHAKE = "REMAL_HANDSHAKE",
                                ACTION_VALID = "REMAL_ACTION_VALID",
                                ACTION_INVALID = "REMAL_ACTION_INVALID",
                                REMAL_DC = "REMAL_DISCONNECT";
    private static final int DEFAULT_WIFI_PORT = 2454;

    private String name;
    private boolean connected;
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private String address;
    private int port;
    private Thread wifiThread;
    private ActionValidCallback lastActionCallback;

    public RemoteWiFiDevice(String name) {
        this.name = name;
        connected = false;
        port = DEFAULT_WIFI_PORT;
        address = "";
    }

    public boolean isConnected() {
        return connected;
    }

    public void disconnect() {
        if(connected) {
            connected = false;

            try {
                writer.close();
                reader.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(wifiThread != null)
                wifiThread.interrupt();

            RemAL.post(new DeviceDisconnectEvent(this));
        }
    }

    public void connect(ActionValidCallback callback) {
        if(address.isEmpty()) {
            callback.run(false);

            return;
        }

        //Get wifi io
        try {
            socket = new Socket(getWiFiAddress(), getWiFiPort());
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch(Exception e) {
            e.printStackTrace();
        }

        //Start listener
        wifiThread = new Thread(() -> {
            String input = "";

            do {
                if(input != null) {
                    switch (input) {
                        case HANDSHAKE:
                            connected = true;
                            callback.run(true);
                            RemAL.post(new DeviceConnectEvent(this));
                            break;
                        case ACTION_VALID:
                            if(lastActionCallback != null) {
                                lastActionCallback.run(true);
                                lastActionCallback = null;
                            }
                            break;
                        case ACTION_INVALID:
                            if(lastActionCallback != null) {
                                lastActionCallback.run(false);
                                lastActionCallback = null;
                            }
                            break;
                        case REMAL_DC:
                            disconnect();
                            break;
                    }
                }

                try {
                    input = reader.readLine();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            } while(connected && !Thread.currentThread().isInterrupted());
        });

        wifiThread.start();

        try {
            writer.write(HANDSHAKE);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(String command, ActionValidCallback callback) {
        if(lastActionCallback != null)
            lastActionCallback.run(false);

        lastActionCallback = callback;

        try {
            writer.write(command);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public int getWiFiPort() {
        return port;
    }

    public void setWiFiPort(int port) {
        this.port = port;
    }

    public String getWiFiAddress() {
        return address;
    }

    public void setWiFiAddress(String address) {
        this.address = address;
    }

    public void setName(String name) {
        this.name = name;
    }
}