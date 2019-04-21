package exn.database.remal.events;

import exn.database.remal.core.DeviceEvent;
import exn.database.remal.devices.IRemoteDevice;

public class DeviceDisconnectEvent extends DeviceEvent {
    public DeviceDisconnectEvent(IRemoteDevice device) {
        super(device);
    }
}
