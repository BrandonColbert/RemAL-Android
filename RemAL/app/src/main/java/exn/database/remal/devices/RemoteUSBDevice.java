package exn.database.remal.devices;

import exn.database.remal.macros.ActionValidCallback;

public class RemoteUSBDevice implements IRemoteDevice {
    private String name;

    public RemoteUSBDevice(String name) {
        this.name = name;
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
    public void sendCommand(String command, ActionValidCallback callback) {
        callback.run(false);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
