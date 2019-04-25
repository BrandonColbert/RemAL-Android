package exn.database.remal.events;

import exn.database.remal.devices.IRemoteDevice;

public class DeviceDestroyedEvent extends DeviceEvent {
    public DeviceDestroyedEvent(IRemoteDevice device) {
        super(device);
    }
}
