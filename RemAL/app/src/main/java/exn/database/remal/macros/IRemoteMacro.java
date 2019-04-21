package exn.database.remal.macros;

import exn.database.remal.devices.IRemoteDevice;

public interface IRemoteMacro {
    /**
     * Sets the device to execute commands on
     * @param device Device to execute commands on
     */
    void setTargetDevice(IRemoteDevice device);

    /**
     * @return The device that commands will be executed on
     */
    IRemoteDevice getTargetDevice();

    /**
     * Set the command to be sent when executed
     * @param command Command to be sent
     */
    void setCommand(String command);

    /**
     * @return The command that will be executed
     */
    String getCommand();

    /**
     * Executes the macro on the targeted device
     * @return Whether the command was successfully received
     */
    default void execute(ActionValidCallback callback) {
        if(getTargetDevice() != null)
            getTargetDevice().sendCommand(getCommand(), callback);
        else
            callback.run(false);
    }
}
