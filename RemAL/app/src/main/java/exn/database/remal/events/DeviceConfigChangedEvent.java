package exn.database.remal.events;

import exn.database.remal.core.DeviceEvent;
import exn.database.remal.devices.IRemoteDevice;

public class DeviceConfigChangedEvent extends DeviceEvent {
    public DeviceConfigChangedEvent(IRemoteDevice device) {
        super(device);
    }
}
