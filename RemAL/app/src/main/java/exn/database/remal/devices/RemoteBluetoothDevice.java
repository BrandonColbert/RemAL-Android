package exn.database.remal.devices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.UUID;

import exn.database.remal.config.PersistentValues;
import exn.database.remal.core.RemAL;
import exn.database.remal.deck.DeviceActionCallback;
import exn.database.remal.deck.ITile;
import exn.database.remal.deck.TileLevelTracker;
import exn.database.remal.events.DeviceConfigChangedEvent;
import exn.database.remal.events.DeviceDisconnectEvent;
import exn.database.remal.events.DeviceTileCreateEvent;

public class RemoteBluetoothDevice extends RemoteDevice {
    private static final UUID ID = UUID.fromString("228e1e8b-745b-4754-8c9d-8efb6b21189b");
    private static final String TILE_CREATE = "TILE_CREATE";

    private String btName, address;
    private boolean connected, connecting;
    private Thread btThread;
    private BluetoothSocket socket;
    private BufferedWriter writer;
    private BufferedReader reader;

    public RemoteBluetoothDevice(String name) {
        super(name);
        address = btName = "";
    }

    public RemoteBluetoothDevice() {
        super();
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public boolean isConnecting() {
        return connecting;
    }

    @Override
    public void disconnect() {
        super.disconnect();

        if(connected) {
            connected = false;

            try {
                writer.close();
                reader.close();
                socket.close();
            } catch(IOException e) {
                e.printStackTrace();
            }

            if(btThread != null)
                btThread.interrupt();

            RemAL.post(new DeviceDisconnectEvent(this));
        }
    }

    @Override
    public void connect(DeviceActionCallback callback) {
        super.connect(callback);

        if(!connected && !connecting) {
            connecting = true;

            if(setupConnection()) {
                connected = true;

                (btThread = new Thread(() -> {
                    while(connected && !Thread.currentThread().isInterrupted()) {
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
                            onMessage(input);

                    }
                })).start();
            }

            callback.run(connected);
            connecting = false;
        }
    }

    @Override
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

    public String getConnectionName() {
        return "Bluetooth";
    }

    @Override
    public String getConnectionDescription() {
        return getConnectionName() + " | " + address;
    }

    @Override
    public JSONObject save(JSONObject data) throws JSONException {
        data.put("address", address);
        data.put("btName", btName);

        return super.save(data);
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        super.load(data);
        address = data.getString("address");
        btName = data.getString("btName");
    }

    /**
     * Called when the a message is received from the device
     * @param msg The message
     */
    private void onMessage(String msg) {
        switch(msg) {
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

    private boolean setupConnection() {
        BluetoothDevice d = getBluetoothDevice();

        if(d != null) {
            try {
                (socket = d.createRfcommSocketToServiceRecord(ID)).connect();
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                return true;
            } catch(IOException e) {
                e.printStackTrace();

                return false;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Sets the address for the connection
     */
    public void setAddress(String address) {
        if(!address.isEmpty()) {
            this.address = address;

            BluetoothDevice d = getBluetoothDevice();
            if(d != null)
                btName = d.getName();

            RemAL.post(new DeviceConfigChangedEvent(this));
        }
    }

    /**
     * @return Name tied to the  address
     */
    public String getBluetoothName() {
        return btName;
    }

    /**
     * @return Address for the connection
     */
    public String getAddress() {
        return address;
    }

    /**
     * The string[0] is entries and string[1] is values
     * @return The list of available bluetooth devices
     */
    public String[][] findBluetoothDevices() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        String[] entries = new String[]{"None found"};
        String[] entryValues = new String[]{""};

        if(adapter != null) {
            if(adapter.isEnabled()) {
                Set<BluetoothDevice> devices = adapter.getBondedDevices();
                int length = devices.size(), i = 0;

                if(length > 0) {
                    entries = new String[length];
                    entryValues = new String[length];

                    for(BluetoothDevice device : devices) {
                        entries[i] = device.getName();
                        entryValues[i++] = device.getAddress();
                    }
                }
            } else {
                entries[0] = "Bluetooth not enabled";
            }
        } else {
            entries[0] = "Bluetooth not supported";
        }

        return new String[][]{entries, entryValues};
    }

    private BluetoothDevice getBluetoothDevice() {
        if(!address.isEmpty()) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

            if(adapter != null && adapter.isEnabled())
                return adapter.getRemoteDevice(address);
        }

        return null;
    }
}
