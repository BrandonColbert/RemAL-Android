package exn.database.remal.devices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import exn.database.remal.core.RemAL;
import exn.database.remal.events.DeviceConfigChangedEvent;
import exn.database.remal.events.DeviceConnectEvent;
import exn.database.remal.events.DeviceDisconnectEvent;
import exn.database.remal.deck.ActionValidCallback;

/**
 * Handles device access through LAN connection
 */
public class RemoteWiFiDevice extends RemoteDevice {
    private static final String HANDSHAKE = "REMAL_HANDSHAKE",
                                ACTION_VALID = "REMAL_ACTION_VALID",
                                ACTION_INVALID = "REMAL_ACTION_INVALID",
                                REMAL_DC = "REMAL_DISCONNECT";
    private static final int DEFAULT_WIFI_PORT = 24545;

    private boolean connected;
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private String address;
    private int port;
    private Thread wifiThread;
    private ActionValidCallback lastActionCallback;

    public RemoteWiFiDevice(String name) {
        super(name);
        connected = false;
        port = DEFAULT_WIFI_PORT;
        address = "0.0.0.0";
    }

    public RemoteWiFiDevice() {
        super();
        connected = false;
    }

    @Override
    public JSONObject save(JSONObject data) throws JSONException {
        data.put("port", port);
        data.put("address", address);

        return super.save(data);
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        super.load(data);

        port = data.getInt("port");
        address = data.getString("address");
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
        isConnecting = true;

        if(address.isEmpty()) {
            isConnecting = false;
            callback.run(false);

            return;
        }

        boolean didInit = false;

        //Get wifi io
        try {
            socket = new Socket(getAddress(), getPort());
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            didInit = true;
        } catch(Exception e) {
            e.printStackTrace();
        }

        if(didInit) {
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
        } else {
            isConnecting = false;
            callback.run(false);
        }
    }

    public void sendRequest(String request, ActionValidCallback callback) {
        if(lastActionCallback != null)
            lastActionCallback.run(false);

        lastActionCallback = callback;

        try {
            writer.write(request);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
        RemAL.post(new DeviceConfigChangedEvent(this));
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        if(!address.isEmpty()) {
            this.address = address;
            RemAL.post(new DeviceConfigChangedEvent(this));
        }
    }

    public String getConnectionName() {
        return "WiFi";
    }

    public String getConnectionDescription() {
        return getConnectionName() + " | " + address + ":" + port;
    }
}