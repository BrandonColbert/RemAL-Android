package exn.database.remal.devices;

import org.json.JSONException;
import org.json.JSONObject;

import exn.database.remal.core.RemAL;
import exn.database.remal.deck.ActionValidCallback;
import exn.database.remal.events.DeviceDisconnectEvent;

public abstract class RemoteDevice implements IRemoteDevice {
    protected String name;
    protected int order;
    protected volatile boolean isConnecting;

    /**
     * Creates a device with the specified name
     * @param name Name for the device
     */
    public RemoteDevice(String name) {
        this.name = name;
        isConnecting = false;
        order = RemAL.getDevices().length;
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
}