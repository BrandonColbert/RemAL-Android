package exn.database.remal.devices;

import org.json.JSONException;
import org.json.JSONObject;

import exn.database.remal.core.RemAL;
import exn.database.remal.deck.DeviceActionCallback;

public abstract class RemoteDevice implements IRemoteDevice {
    protected String name;
    protected int order;

    /**
     * Creates a device with the specified name
     * @param name Name for the device
     */
    public RemoteDevice(String name) {
        this.name = name;
        order = RemAL.getDevices().length;
    }

    public RemoteDevice() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String getConnectionDescription() {
        return getConnectionName();
    }

    @Override
    public JSONObject save(JSONObject data) throws JSONException {
        data.put("name", name);
        data.put("order", order);

        return data;
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        name = data.getString("name");
        order = data.getInt("order");
    }

    @Override
    public void connect(DeviceActionCallback callback) {
        if(!isConnected())
            if(isConnecting())
                RemAL.displayText("Already trying '" + getName() + "' through " + getConnectionName() + "...");
            else
                RemAL.displayText("Trying '" + getName() + "' through " + getConnectionName() + "...");
    }

    @Override
    public void disconnect() {
        if(isConnected())
            RemAL.displayText("Disconnected from " + getName());
    }
}