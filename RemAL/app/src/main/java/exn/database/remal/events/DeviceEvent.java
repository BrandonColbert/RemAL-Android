package exn.database.remal.events;

import exn.database.remal.core.RemalEvent;
import exn.database.remal.devices.IRemoteDevice;

public class DeviceEvent extends RemalEvent {
    public final IRemoteDevice device;

    public DeviceEvent(IRemoteDevice device) {
        this.device = device;
    }
}
