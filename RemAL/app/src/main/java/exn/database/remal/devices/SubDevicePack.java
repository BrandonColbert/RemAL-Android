package exn.database.remal.devices;

import exn.database.remal.core.RemAL;
import exn.database.remal.events.DeviceConfigChangedEvent;

/**
 * Contains the the enabled status and order of a subdevice
 */
public class SubDevicePack {
    private boolean enabled; //Whether connecting uses the device will be considered
    private int order; //The order in which connections will be attempted if multiple exist
    private IRemoteDevice device;

    /**
     * Pack a subdevice
     * @param device The contained device
     * @param order The order in which it will be used
     * @param enabled Whether or not the subdevice is enabled
     */
    public SubDevicePack(IRemoteDevice device, int order, boolean enabled) {
        this.device = device;
        this.order = order;
        this.enabled = enabled;
    }

    public SubDevicePack(IRemoteDevice device, int order) {
        this(device, order, true);
    }

    public SubDevicePack(IRemoteDevice device) {
        this(device, 0);
    }

    /**
     * Set whether the subdevice is enabled
     * @param value Subdevice enabled status
     */
    public void setEnabled(boolean value) {
        enabled = value;

        if(!value)
            device.disconnect();

        RemAL.post(new DeviceConfigChangedEvent(device));
    }

    /**
     * @return Whether the subdevice is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set when this subdevice will be used relative to the others
     * @param value Order
     */
    public void setOrder(int value) {
        order = value;
        RemAL.post(new DeviceConfigChangedEvent(device));
    }

    /**
     * @return The order for this subdevice
     */
    public int getOrder() {
        return order;
    }

    /**
     * @return The contained subdevice
     */
    public IRemoteDevice getDevice() {
        return device;
    }
}