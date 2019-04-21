package exn.database.remal.core;

import exn.database.remal.devices.IRemoteDevice;

public class DeviceEvent extends RemalEvent {
    public final IRemoteDevice device;

    public DeviceEvent(IRemoteDevice device) {
        this.device = device;
    }
}
