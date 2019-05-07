package exn.database.remal.core;

import android.app.Activity;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import exn.database.remal.config.PersistenceUtils;
import exn.database.remal.devices.IRemoteDevice;
import exn.database.remal.devices.RemoteMultiDevice;
import exn.database.remal.events.DeviceCreatedEvent;
import exn.database.remal.events.DeviceDestroyedEvent;
import exn.database.remal.events.DeviceRenamedEvent;
import exn.database.remal.events.TileCreatedEvent;
import exn.database.remal.deck.DeckTile;
import exn.database.remal.deck.ITile;
import exn.database.remal.events.TileDestroyedEvent;

public final class RemAL {
    /** List of existing devices */
    private static final HashMap<String, IRemoteDevice> devices = new HashMap<>();
    private static final List<IRemalEventListener> listeners = new ArrayList<>();
    private static Activity activity;

    private RemAL() {}

    public static void log(Object o) {
        System.out.println("[RemAL-Log] " + o.toString());
    }

    public static void setMainActivity(Activity activity) {
        RemAL.activity = activity;
    }

    public static Activity getMainActivity() {
        return activity;
    }

    /**
     * Displays a toast
     * @param text Text to show in the toast
     */
    public static void displayText(String text) {
        activity.runOnUiThread(() -> Toast.makeText(activity, text, Toast.LENGTH_SHORT).show());
    }

    /**
     * @return A newly created app tile
     */
    public static ITile createTile(IRemoteDevice device, int index) {
        ITile tile = new DeckTile(device, index);
        post(new TileCreatedEvent(tile));

        return tile;
    }

    /**
     * @param index Index of the tile
     * @return The tile at the index
     */
    public static ITile getTile(int index) {
        try {
            return PersistenceUtils.loadTile(index);
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void saveTile(ITile tile) {
        try {
            PersistenceUtils.saveTile(tile);
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    public static void deleteTile(ITile tile) {
        PersistenceUtils.removeTile(tile);
        post(new TileDestroyedEvent(tile));
    }

    /**
     * Loads and connects to the saved devices
     */
    public static void loadAndConnectDevices() {
        for(IRemoteDevice device : PersistenceUtils.loadDevices()) {
            devices.put(device.getName(), device);
            device.connect(valid -> {});
        }
    }

    /**
     * @return An array of all the devices sorted by name
     */
    public static IRemoteDevice[] getDevices() {
        List<IRemoteDevice> sortedDevices = new ArrayList<>(devices.values());
        Collections.sort(sortedDevices, (lhs, rhs) -> lhs.getName().compareTo(rhs.getName()));

        return sortedDevices.toArray(new IRemoteDevice[0]);
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

            try {
                PersistenceUtils.addToDevicePath(device);
                PersistenceUtils.saveDevice(device);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            post(new DeviceCreatedEvent(device));

            return true;
        }

        return false;
    }

    public static boolean renameDevice(String oldName, String newName) {
        if(!newName.isEmpty() && !devices.containsKey(newName)) {
            IRemoteDevice device = devices.remove(oldName);

            if(device != null) {
                try {
                    PersistenceUtils.removeFromDevicePath(device);
                    PersistenceUtils.removeDeviceSave(device);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                device.setName(newName);
                devices.put(newName, device);

                try {
                    PersistenceUtils.addToDevicePath(device);
                    PersistenceUtils.saveDevice(device);
                } catch(JSONException e) {
                    e.printStackTrace();
                }

                post(new DeviceRenamedEvent(device, oldName));

                return true;
            }
        }

        return false;
    }

    public static void saveDevice(IRemoteDevice device) {
        try {
            PersistenceUtils.saveDevice(device);
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disconnects from and deletes a device
     * @param name Name of device to remove
     */
    public static void deleteDevice(String name) {
        IRemoteDevice device = devices.remove(name);

        if(device != null) {
            if(device.isConnected())
                device.disconnect();

            try {
                PersistenceUtils.removeFromDevicePath(device);
                PersistenceUtils.removeDeviceSave(device);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            post(new DeviceDestroyedEvent(device));
        }
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

    //EVENTS

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
        IRemalEventListener[] arr = listeners.toArray(new IRemalEventListener[0]);

        for(IRemalEventListener listener : arr)
            listener.onRemalEvent(event);
    }
}