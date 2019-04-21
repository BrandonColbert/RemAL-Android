package exn.database.remal.devices;

import exn.database.remal.macros.ActionValidCallback;

/**
 * Handles connections and command sending to devices
 */
public class RemoteMultiDevice implements IRemoteDevice {
    public static final int USB_INDEX = 0, LAN_INDEX = 1, BLUETOOTH_INDEX = 2, WIFI_INDEX = 3, SSH_INDEX = 4;

    /**
     * The order in which connections will be attempted if multiple exist
     */
    private int[] connectionOrder;
    private IRemoteDevice[] instances;
    private String name;
    private int currentConnectionIndex;

    public RemoteMultiDevice(String name) {
        this.name = name;
        currentConnectionIndex = -1;

        connectionOrder = new int[] {
                USB_INDEX,
                LAN_INDEX,
                BLUETOOTH_INDEX,
                WIFI_INDEX,
                SSH_INDEX
        };

        instances = new IRemoteDevice[] {
                new RemoteUSBDevice(name),
                new RemoteLanDevice(name),
                new RemoteBluetoothDevice(name),
                new RemoteWiFiDevice(name),
                new RemoteSSHDevice(name)
        };
    }

    public <T extends IRemoteDevice> T getInstance(int connectionIndex) {
        return (T)instances[connectionIndex];
    }

    public int[] getConnectionOrder() {
        return connectionOrder;
    }

    public void setConnectionOrder(int[] order) {
        connectionOrder = order;
    }

    public boolean isConnected() {
        return currentConnectionIndex > -1 && instances[currentConnectionIndex].isConnected();
    }

    public void disconnect() {
        if(currentConnectionIndex > -1)
            instances[currentConnectionIndex].disconnect();
    }

    public void sendCommand(String command, ActionValidCallback callback) {
        if(currentConnectionIndex > -1)
            instances[currentConnectionIndex].sendCommand(command, callback);
        else
            callback.run(false);
    }

    public void connect(ActionValidCallback callback) {
        //TODO: Implement
        callback.run(false);

        /*

        for(int connectionIndex : connectionOrder) {
            if(instances[connectionIndex].connect()) {
                currentConnectionIndex = connectionIndex;
            }
        }

        */
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;

        for(IRemoteDevice device : instances)
            device.setName(name);
    }
}
