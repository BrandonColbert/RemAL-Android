package exn.database.remal.devices;

import exn.database.remal.deck.DeviceActionCallback;

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
    public boolean isConnecting() {
        return false;
    }

    @Override
    public void disconnect() {
        super.disconnect();
    }

    @Override
    public void connect(DeviceActionCallback callback) {
        super.connect(callback);
        callback.run(false);
    }

    @Override
    public void sendRequest(String request, DeviceActionCallback callback) {
        callback.run(false);
    }

    public String getConnectionName() {
        return "USB";
    }
}
