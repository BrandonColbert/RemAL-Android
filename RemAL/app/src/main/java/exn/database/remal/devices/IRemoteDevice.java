package exn.database.remal.devices;

import exn.database.remal.config.IPersistent;
import exn.database.remal.deck.ActionValidCallback;

/**
 * A device that can be connected to and sent commands
 */
public interface IRemoteDevice extends IPersistent {
    /**
     * @return The order of the device for selection and appearance
     */
    int getOrder();

    /**
     * Sets the order of the device for selection and appearance
     */
    void setOrder(int order);

    /**
     * @return True if this device is connected, false otherwise
     */
    boolean isConnected();

    /**
     * @return Whether connecting is currently in progress
     */
    boolean isConnecting();

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
     * Sends a request to this device
     * @param request Request to send
     * @param callback Callback passing true if the command was both received and executed successfully, or false otherwise
     */
    void sendRequest(String request, ActionValidCallback callback);

    /**
     * @return The localized name of this device
     */
    String getName();

    /**
     * Sets the localized name of this device
     * @param name Name for the device
     */
    void setName(String name);

    /**
     * @return Name for the type of connection used
     */
    String getConnectionName();

    /**
     * @return Description of the connection being used
     */
    String getConnectionDescription();
}
