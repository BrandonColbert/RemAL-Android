package exn.database.remal.requests;

import exn.database.remal.devices.IRemoteDevice;

public class ShellRequest extends RemoteRequest {
    public ShellRequest(IRemoteDevice device) {
        super(device);
    }

    @Override
    public void setRequest(String request) {
        this.request = "shell:" + request;
    }
}
