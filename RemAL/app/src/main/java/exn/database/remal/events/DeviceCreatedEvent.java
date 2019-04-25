package exn.database.remal.events;

import exn.database.remal.devices.IRemoteDevice;

public class DeviceCreatedEvent extends DeviceEvent {
    public DeviceCreatedEvent(IRemoteDevice device) {
        super(device);
    }
}
