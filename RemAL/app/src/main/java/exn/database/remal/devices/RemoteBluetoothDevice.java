package exn.database.remal.devices;

import org.json.JSONException;
import org.json.JSONObject;

import exn.database.remal.requests.ActionValidCallback;

public class RemoteBluetoothDevice extends RemoteDevice {
    public RemoteBluetoothDevice(String name) {
        super(name);
    }

    public RemoteBluetoothDevice() {
        super();
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void connect(ActionValidCallback callback) {
        callback.run(false);
    }

    @Override
    public void sendRequest(String request, ActionValidCallback callback) {
        callback.run(false);
    }

    public String getConnectionName() {
        return "Bluetooth";
    }
}
