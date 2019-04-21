package exn.database.remal.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import exn.database.remal.devices.IRemoteDevice;
import exn.database.remal.devices.RemoteMultiDevice;
import exn.database.remal.events.DeviceCreatedEvent;
import exn.database.remal.events.DeviceDestroyedEvent;
import exn.database.remal.events.DeviceRenamedEvent;
import exn.database.remal.macros.IRemoteMacro;
import exn.database.remal.macros.ShellMacro;

public final class RemAL {
    /** List of existing devices */
    private static final HashMap<String, IRemoteDevice> devices = new HashMap<>();
    private static final List<IRemoteMacro> macros = new ArrayList<>();
    private static final List<IRemalEventListener> listeners = new ArrayList<>();

    private RemAL() {}

    /**
     * @return A newly created macro
     */
    public static IRemoteMacro createMacro() {
        IRemoteMacro macro = new ShellMacro(null);

        macros.add(macro);

        return macro;
    }

    public static IRemoteDevice[] getDevices() {
        List<IRemoteDevice> sortedDevices = new ArrayList<>(devices.values());
        Collections.sort(sortedDevices, (lhs, rhs) -> lhs.getName().compareTo(rhs.getName()));

        return sortedDevices.toArray(new IRemoteDevice[0]);
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

            post(new DeviceCreatedEvent(device));

            return true;
        }

        return false;
    }

    public static boolean renameDevice(String oldName, String newName) {
        IRemoteDevice device = devices.remove(oldName);

        if(device != null) {
            device.setName(newName);
            devices.put(newName, device);

            post(new DeviceRenamedEvent(device, oldName));

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

        post(new DeviceDestroyedEvent(device));
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

    /**
     * Registers an object to receive RemAL events
     * @param listener Object to receive events
     */
    public static void register(IRemalEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Unregisters an object from receiving RemAL events
     * @param listener Object to stop receiving events
     */
    public static void unregister(IRemalEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Posts a RemAL event that updates all listeners
     * @param event Event to post
     */
    public static void post(RemalEvent event) {
        for(IRemalEventListener listener : listeners)
            listener.onRemalEvent(event);
    }
}
