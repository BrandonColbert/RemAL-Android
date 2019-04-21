package exn.database.remal.devices;

import exn.database.remal.macros.ActionValidCallback;

public interface IRemoteDevice {
    /**
     * @return True if this device is connected, false otherwise
     */
    boolean isConnected();

    /**
     * Disconnect from this device
     */
    void disconnect();

    /**
     * Connects to this device and updates connection status
     * @param callback Callback passing true/false for whether the connection was successful
     */
    void connect(ActionValidCallback callback);

    /**
     * Sends a command to this device
     * @param command Command to send
     * @param callback Callback passing true if the command was both received and executed successfully, or false otherwise
     */
    void sendCommand(String command, ActionValidCallback callback);

    /**
     * @return The localized name of this device
     */
    String getName();

    /**
     * Sets the localized name of this device
     * @param name Name for the device
     */
    void setName(String name);
}
