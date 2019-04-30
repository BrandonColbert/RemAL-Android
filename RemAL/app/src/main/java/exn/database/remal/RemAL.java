package exn.database.remal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import exn.database.remal.devices.IRemoteDevice;
import exn.database.remal.devices.RemoteMultiDevice;
import exn.database.remal.macros.IRemoteMacro;
import exn.database.remal.macros.ShellMacro;

public final class RemAL {
    /** List of existing devices */
    private static final HashMap<String, IRemoteDevice> devices = new HashMap<>();
    private static final List<IRemoteMacro> macros = new ArrayList<>();

    private RemAL() {}

    /**
     * @return A newly created macro
     */
    public static IRemoteMacro createMacro() {
        IRemoteMacro macro = new ShellMacro(null);

        macros.add(macro);

        return macro;
    }

    public static Collection<IRemoteDevice> getDevices() {
        return devices.values();
    }

    /**
     * Returns a list of created macros
     */
    public List<IRemoteMacro> getMacros() {
        return macros;
    }

    /**
     * Loads saved macros
     */
    public static void loadMacros() {
        //TODO: Implement
    }

    /**
     * Loads and connects to the saved devices
     */
    public static void loadAndConnectDevices() {
        //TODO: Implement
    }

    /**
     * Creates a new device with the given name
     * @param name Name to give the device
     * @return Whether the device could be created
     */
    public static boolean createDevice(String name) {
        if(!devices.containsKey(name)) {
            IRemoteDevice device = new RemoteMultiDevice(name);
            devices.put(name, device);

            return true;
        }

        return false;
    }

    public static boolean renameDevice(String oldName, String newName) {
        IRemoteDevice device = devices.remove(oldName);

        if(device != null) {
            device.setName(newName);
            devices.put(newName, device);

            return true;
        }

        return false;
    }

    /**
     * Disconnects from and deletes a device
     * @param name Name of device to remove
     */
    public static void deleteDevice(String name) {
        IRemoteDevice device = devices.remove(name);

        if(device != null && device.isConnected())
            device.disconnect();
    }

    /**
     * Gets a device instance based on its name
     * @param name Name of the device
     * @return Device instance
     */
    public static IRemoteDevice getDevice(String name) {
        if(devices.containsKey(name))
            return devices.get(name);

        return null;
    }
}
