package exn.database.remal.devices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import exn.database.remal.config.PersistentValues;
import exn.database.remal.core.RemAL;
import exn.database.remal.deck.ITile;
import exn.database.remal.deck.TileLevelTracker;
import exn.database.remal.events.DeviceConfigChangedEvent;
import exn.database.remal.events.DeviceConnectEvent;
import exn.database.remal.events.DeviceDisconnectEvent;
import exn.database.remal.deck.DeviceActionCallback;
import exn.database.remal.events.DeviceTileCreateEvent;

/**
 * Handles device access through LAN connection
 */
public class RemoteWiFiDevice extends RemoteDevice {
    private static final String HANDSHAKE = "REMAL_HANDSHAKE",
                                REMAL_DC = "REMAL_DISCONNECT",
                                TILE_CREATE = "TILE_CREATE";
    private static final int DEFAULT_WIFI_PORT = 24545;

    private boolean connected;
    private volatile boolean isConnecting;
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private String address;
    private int port;
    private Thread wifiThread;

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

    public boolean isConnecting() {
        return isConnecting;
    }

    public void disconnect() {
        super.disconnect();

        if(connected) {
            connected = false;

            //Close the socket and IO

            try {
                writer.close();
                reader.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Let the WiFi thread know it needs to stop
            if(wifiThread != null)
                wifiThread.interrupt();

            RemAL.post(new DeviceDisconnectEvent(this));
        }
    }

    /**
     * Try to get the socket and IO
     * @return True if successful, false otherwise
     */
    private boolean setupConnection() {
        //Get socket and io
        try {
            socket = new Socket();
            socket.setKeepAlive(true);
            socket.connect(new InetSocketAddress(getAddress(), getPort()), 5000);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            return true;
        } catch(SocketTimeoutException e) {
            return false;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void connect(DeviceActionCallback callback) {
        super.connect(callback);

        if(!connected && !isConnecting) {
            isConnecting = true;

            //Ensure address exists to connect to
            if(!address.isEmpty() && setupConnection()) {
                //Start listener
                (wifiThread = new Thread(() -> {
                    do {
                        String input = null;

                        //Read input
                        try {
                            input = reader.readLine();
                        } catch(IOException e) {
                            disconnect();
                            connect(valid -> {});
                            break;
                        } catch(Exception e) {
                            e.printStackTrace();
                        }

                        //Interpret message if not null or empty
                        if(input != null && !input.isEmpty())
                            onMessage(input, callback);

                    } while(connected && !Thread.currentThread().isInterrupted()); //do-while so that the HANDSHAKE response can be received to determine whether connected
                })).start();

                sendRequest(HANDSHAKE, valid -> {});
            } else {
                callback.run(false);
            }

            isConnecting = false;
        }
    }

    public void sendRequest(String request, DeviceActionCallback callback) {
        new Thread(() -> {
            try {
                //Convert to bytes
                byte[] data = request.getBytes();

                //Send first message to define length and second as actual message
                socket.getOutputStream().write(ByteBuffer.allocate(4).putInt(data.length).array());
                writer.write(request);
                writer.flush();
            } catch(SocketException e) {
                disconnect();
                connect(valid -> {
                    if(valid)
                        sendRequest(request, callback);
                    else
                        callback.run(false);
                });
            } catch(Exception e) {
                e.printStackTrace();

                callback.run(false);
            }
        }).start();
    }

    /**
     * @return Port for the connection
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port for the connection
     * @param port Port
     */
    public void setPort(int port) {
        this.port = port;
        RemAL.post(new DeviceConfigChangedEvent(this));
    }

    /**
     * @return Address to connect to
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address to connect to
     * @param address Address
     */
    public void setAddress(String address) {
        if(!address.isEmpty()) {
            this.address = address;
            RemAL.post(new DeviceConfigChangedEvent(this));
        }
    }

    public String getConnectionName() {
        return "WiFi";
    }

    @Override
    public String getConnectionDescription() {
        return getConnectionName() + " | " + address + ":" + port;
    }

    /**
     * Called when the a message is received from the device
     * @param msg The message
     * @param callback Callback to call after interpreting the message
     */
    private void onMessage(String msg, DeviceActionCallback callback) {
        switch(msg) {
            case HANDSHAKE:
                connected = true;
                callback.run(true);
                RemAL.post(new DeviceConnectEvent(this));
                break;
            case REMAL_DC:
                disconnect();
                break;
            default: {
                if(msg.startsWith(TILE_CREATE)) {
                    String path = msg.substring(TILE_CREATE.length() + 1);
                    int extensionEnd = path.lastIndexOf('.'), count = PersistentValues.getMaxTiles();
                    String name = path.substring(Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')) + 1, extensionEnd != -1 ? extensionEnd : path.length());

                    for(int i = 0; i < count; i++) {
                        if(!TileLevelTracker.tileAt(i)) {
                            ITile tile = RemAL.createTile(null, i);
                            tile.setName(name);
                            tile.setRequestType("path");
                            tile.setRequest(path);
                            tile.setTargetDevice(this);
                            RemAL.saveTile(tile);
                            RemAL.post(new DeviceTileCreateEvent(this, tile));
                            break;
                        }
                    }
                }

                break;
            }
        }
    }
}