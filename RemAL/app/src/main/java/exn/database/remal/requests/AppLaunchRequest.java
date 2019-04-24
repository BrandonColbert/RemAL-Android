package exn.database.remal.requests;

import exn.database.remal.devices.IRemoteDevice;

public class AppLaunchRequest extends RemoteRequest {
    public AppLaunchRequest(IRemoteDevice device) {
        super(device);
    }

    @Override
    public void setRequest(String request) {
        this.request = "app:" + request;
    }
}
