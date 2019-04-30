package exn.database.remal.requests;

import exn.database.remal.config.IPersistent;
import exn.database.remal.devices.IRemoteDevice;

/**
 * A request that can be sent to a device
 */
public interface IRemoteRequest extends IPersistent {
    /**
     * Sets the device to send requests to
     * @param device The device
     */
    void setTargetDevice(IRemoteDevice device);

    /**
     * @return The device that commands will be sent to
     */
    IRemoteDevice getTargetDevice();

    /**
     * Set the request to be sent
     * @param request The request
     */
    void setRequest(String request);

    /**
     * @return The request that will be sent
     */
    String getRequest();

    /**
     * Sends the request to the target device
     * @return Whether the request was successfully received
     */
    default void send(ActionValidCallback callback) {
        if(getTargetDevice() != null)
            getTargetDevice().sendRequest(getRequest(), callback);
        else
            callback.run(false);
    }
}
