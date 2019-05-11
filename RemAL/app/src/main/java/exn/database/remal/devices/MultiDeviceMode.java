package exn.database.remal.devices;

/**
 * Different modes that an {@link RemoteMultiDevice} can be in
 */
public enum MultiDeviceMode {
    NONE,
    USB,
    LAN,
    BLUETOOTH,
    WIFI,
    SSH
}