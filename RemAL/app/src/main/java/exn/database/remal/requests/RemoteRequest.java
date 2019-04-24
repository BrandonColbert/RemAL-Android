package exn.database.remal.requests;

import org.json.JSONException;
import org.json.JSONObject;

import exn.database.remal.devices.IRemoteDevice;

public class RemoteRequest implements IRemoteRequest {
    private IRemoteDevice target;
    protected String request;

    public RemoteRequest(IRemoteDevice device) {
        target = device;
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
    public String save() {
        JSONObject data = new JSONObject();

        try {
            data.put("request", request);
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return data.toString();
    }

    @Override
    public void load(String data) {
        try {
            JSONObject obj = new JSONObject(data);

            request = obj.getString("address");
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }
}
