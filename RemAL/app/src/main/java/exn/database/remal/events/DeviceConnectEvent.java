package exn.database.remal.events;

import exn.database.remal.devices.IRemoteDevice;

public class DeviceConnectEvent extends DeviceEvent {
    public DeviceConnectEvent(IRemoteDevice device) {
        super(device);
    }
}
