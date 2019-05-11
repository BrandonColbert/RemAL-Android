package exn.database.remal.deck;

import exn.database.remal.config.IPersistent;
import exn.database.remal.core.RemAL;
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
     *
     * @return The request that will be sent
     */
    String getRequest();

    /**
     * Set the type of request to be sent
     * @param type Request type
     */
    void setRequestType(String type);

    /**
     * @return Type of request
     */
    String getRequestType();

    /**
     * Sends the request to the target device
     * @return Whether the request was successfully received
     */
    default void send(DeviceActionCallback callback) {
        IRemoteDevice target = getTargetDevice();
        String request = getRequest();

        if(target != null) {
            if(request.isEmpty()) {
                RemAL.displayText("No action specified");
                callback.run(false);
            } else {
                target.sendRequest(getRequestType() + ":" + request, callback);
            }
        } else {
            RemAL.displayText("No device specified");
            callback.run(false);
        }
    }
}
