package exn.database.remal.deck;

import org.json.JSONException;
import org.json.JSONObject;

import exn.database.remal.core.RemAL;
import exn.database.remal.devices.IRemoteDevice;

public class RemoteRequest implements IRemoteRequest {
    protected IRemoteDevice target;
    protected String request;

    public RemoteRequest(IRemoteDevice device) {
        target = device;
        request = "";
    }

    public RemoteRequest() {
        target = null;
        request = "";
    }

    public void setTargetDevice(IRemoteDevice device) {
        target = device;
    }

    public IRemoteDevice getTargetDevice() {
        return target;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getRequest() {
        return request;
    }

    @Override
    public JSONObject save(JSONObject data) throws JSONException {
        data.put("device", getTargetDevice() != null ? getTargetDevice().getName() : "");
        data.put("request", request);

        return data;
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        String deviceName = data.getString("device");

        target = deviceName.isEmpty() ? null : RemAL.getDevice(deviceName);
        request = data.getString("request");
    }
}
