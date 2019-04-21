package exn.database.remal.events;

import exn.database.remal.core.DeviceEvent;
import exn.database.remal.devices.IRemoteDevice;

public class DeviceRenamedEvent extends DeviceEvent {
    public final String oldName;

    public DeviceRenamedEvent(IRemoteDevice device, String oldName) {
        super(device);
        this.oldName = oldName;
    }
}