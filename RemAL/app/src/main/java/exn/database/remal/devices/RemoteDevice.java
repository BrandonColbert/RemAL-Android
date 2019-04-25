package exn.database.remal.devices;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class RemoteDevice implements IRemoteDevice {
    protected String name;
    protected volatile boolean isConnecting;

    /**
     * Creates a device with the specified name
     * @param name Name for the device
     */
    public RemoteDevice(String name) {
        this.name = name;
        isConnecting = false;
    }

    public RemoteDevice() {
        isConnecting = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isConnecting() {
        return isConnecting;
    }

    @Override
    public String getConnectionDescription() {
        return getConnectionName();
    }

    @Override
    public JSONObject save(JSONObject data) throws JSONException {
        data.put("name", name);

        return data;
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        name = data.getString("name");
    }
}