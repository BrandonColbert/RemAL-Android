package exn.database.remal.core;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import exn.database.remal.devices.IRemoteDevice;
import exn.database.remal.devices.RemoteMultiDevice;
import exn.database.remal.events.DeviceCreatedEvent;
import exn.database.remal.events.DeviceDestroyedEvent;
import exn.database.remal.events.DeviceRenamedEvent;
import exn.database.remal.requests.ActionValidCallback;
import exn.database.remal.requests.AppLaunchRequest;
import exn.database.remal.requests.IRemoteRequest;
import exn.database.remal.requests.ShellRequest;

public final class RemAL {
    public static final String PACKAGE = "exn.database.remal",
                                PATH_DEVICES = "devices",
                                PATH_REQUESTS = "requests";

    /** List of existing devices */
    private static final HashMap<String, IRemoteDevice> devices = new HashMap<>();
    private static final List<IRemoteRequest> requests = new ArrayList<>();
    private static final List<IRemalEventListener> listeners = new ArrayList<>();
    private static Activity activity;
    private static SharedPreferences preferences;

    private RemAL() {}

    public static void setMainActivity(Activity activity) {
        RemAL.activity = activity;
    }

    public static void displayText(String text) {
        activity.runOnUiThread(() -> Toast.makeText(activity, text, Toast.LENGTH_SHORT).show());
    }

    /**
     * @return A newly created app request
     */
    public static IRemoteRequest createAppRequest(IRemoteDevice device) {
        IRemoteRequest request = new AppLaunchRequest(device);
        requests.add(request);

        return request;
    }

    /**
     * @return A newly created shell request
     */
    public static IRemoteRequest createShellRequest(IRemoteDevice device) {
        IRemoteRequest request = new ShellRequest(device);
        requests.add(request);

        return request;
    }

    public static IRemoteDevice[] getDevices() {
        List<IRemoteDevice> sortedDevices = new ArrayList<>(devices.values());
        Collections.sort(sortedDevices, (lhs, rhs) -> lhs.getName().compareTo(rhs.getName()));

        return sortedDevices.toArray(new IRemoteDevice[0]);
    }

    /**
     * Returns a list of created macros
     */
    public List<IRemoteRequest> getRequests() {
        return requests;
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
        if(!newName.isEmpty() && !devices.containsKey(newName)) {
            IRemoteDevice device = devices.remove(oldName);

            if(device != null) {
                device.setName(newName);
                devices.put(newName, device);
                post(new DeviceRenamedEvent(device, oldName));

                return true;
            }
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
        for(IRemalEventListener listener : listeners)
            listener.onRemalEvent(event);
    }

    //SAVING/LOADING

    private static void loadPreferences() {
        if(activity != null)
            preferences = activity.getSharedPreferences(PACKAGE, Context.MODE_PRIVATE);
    }

    public static void saveValue(String name, String value) {
        loadPreferences();
        preferences.edit().putString(name, value).apply();
    }

    public static String loadValue(String name, String defaultString) {
        loadPreferences();
        return preferences.getString(name, defaultString);
    }

    public static void removeSave(String name) {
        loadPreferences();
        preferences.edit().remove(name).apply();
    }

    public static String loadValue(String name) {
        return loadValue(name, "");
    }

    /**
     * Loads saved devices and requests
     */
    public static void loadSettings() {
        loadPreferences();

        //preferences.edit().clear().apply();
        /*
        System.out.println("PRINTING PREFS");
        for(Map.Entry<String, ?> entry : preferences.getAll().entrySet())
            System.out.println("Prefs: " + entry.getValue().toString());
        System.out.println("DONE PRINTING PREFS");
        */

        try {
            JSONArray savedDevices = new JSONArray(loadValue(PATH_DEVICES, "[]"));

            for(int i = 0; i < savedDevices.length(); i++) {
                String deviceName = savedDevices.getString(i);

                IRemoteDevice device = new RemoteMultiDevice();
                device.load(loadValue(PATH_DEVICES + "." + deviceName, "{}"));

                devices.put(deviceName, device);
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads and connects to the saved devices
     */
    public static void connectDevices() {
        for(Map.Entry<String, IRemoteDevice> entry : devices.entrySet())
            entry.getValue().connect(valid -> {});
    }
}
