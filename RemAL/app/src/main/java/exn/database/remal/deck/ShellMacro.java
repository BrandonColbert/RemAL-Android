package exn.database.remal.macros;

import exn.database.remal.devices.IRemoteDevice;
import exn.database.remal.devices.RemoteMultiDevice;

/**
 * Used to hold a macro that can be executed on a specified RemoteMultiDevice
 */
public class ShellMacro implements IRemoteMacro {
    private IRemoteDevice target;
    private String command;

    public ShellMacro(RemoteMultiDevice device) {
        this.target = device;
        command = "";
    }

    public void setTargetDevice(IRemoteDevice device) {
        target = device;
    }

    public IRemoteDevice getTargetDevice() {
        return target;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
