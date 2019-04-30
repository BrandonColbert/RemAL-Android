package exn.database.remal.devices;

import exn.database.remal.deck.ActionValidCallback;

public class RemoteSSHDevice extends RemoteDevice {
    public RemoteSSHDevice(String name) {
        super(name);
    }

    public RemoteSSHDevice() {
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
        return "SSH";
    }
}
