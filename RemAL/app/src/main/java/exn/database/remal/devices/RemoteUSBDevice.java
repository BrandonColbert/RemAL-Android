package exn.database.remal.devices;

import exn.database.remal.deck.ActionValidCallback;

public class RemoteUSBDevice extends RemoteDevice {
    public RemoteUSBDevice(String name) {
        super(name);
    }

    public RemoteUSBDevice() {
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
        return "USB";
    }
}
