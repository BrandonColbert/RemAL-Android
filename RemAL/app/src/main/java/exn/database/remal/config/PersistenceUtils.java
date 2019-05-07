package exn.database.remal.config;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import exn.database.remal.core.RemAL;
import exn.database.remal.devices.IRemoteDevice;
import exn.database.remal.devices.RemoteMultiDevice;
import exn.database.remal.deck.DeckTile;
import exn.database.remal.deck.ITile;

public class PersistenceUtils {
    private static final String PACKAGE = "exn.database.remal",
            PATH_DEVICES = "devices",
            PATH_TILES = "tiles";

    private static SharedPreferences preferences;

    public static void loadPreferences(Activity activity) {
        preferences = activity.getSharedPreferences(PACKAGE, Context.MODE_PRIVATE);
    }

    public static SharedPreferences getPreferences() {
        return preferences;
    }

    public static ITile loadTile(int index) throws JSONException {
        String key = PATH_TILES + "." + index;

        if(preferences.contains(key)) {
            ITile tile = new DeckTile();
            tile.load(new JSONObject(loadValue(key, "{}")));

            return tile;
        }

        return null;
    }

    public static void saveTile(ITile tile) throws JSONException {
        saveValue(PATH_TILES + "." + tile.getPosition(), tile.save());
    }

    public static void removeTile(ITile tile) {
        removeValue(PATH_TILES + "." + tile.getPosition());
    }

    public static void addToDevicePath(IRemoteDevice device) throws JSONException {
        JSONArray savedDevices = new JSONArray(loadValue(PATH_DEVICES, "[]"));
        savedDevices.put(device.getName());

        saveValue(PATH_DEVICES, savedDevices.toString());
    }

    public static void saveDevice(IRemoteDevice device) throws JSONException {
        //Get actual device to prevent saving subdevice specifically
        String name = device.getName();
        device = RemAL.getDevice(name);

        if(device != null)
            saveValue(PATH_DEVICES + "." + device.getName(), device.save());
    }

    public static void removeDeviceSave(IRemoteDevice device) {
        preferences.edit().remove(device.getName()).apply();
    }

    public static void removeFromDevicePath(IRemoteDevice device) throws JSONException {
        JSONArray savedDevices = new JSONArray(loadValue(PATH_DEVICES, "[]"));

        List<String> savedDevicesList = new ArrayList<>();

        for(int i = 0; i < savedDevices.length(); i++) {
            String s = savedDevices.getString(i);

            if(!s.equals(device.getName()))
                savedDevicesList.add(s);
        }

        savedDevices = new JSONArray();

        for(String s : savedDevicesList)
            savedDevices.put(s);

        savedDevicesList.add(device.getName());
        saveValue(PATH_DEVICES, savedDevices.toString());
    }

    /**
     * Loads saved devices and requests
     */
    public static IRemoteDevice[] loadDevices() {
        //preferences.edit().clear().apply();
        /*
        System.out.println("PRINTING PREFS");
        for(Map.Entry<String, ?> entry : preferences.getAll().entrySet())
            System.out.println("Prefs: " + entry.getValue().toString());
        System.out.println("DONE PRINTING PREFS");
        */

        JSONArray savedDevices = null;

        try {
            savedDevices = new JSONArray(loadValue(PATH_DEVICES, "[]"));
        } catch(JSONException e) {
            e.printStackTrace();
        }

        IRemoteDevice[] devices = new IRemoteDevice[0];

        if(savedDevices != null) {
            devices = new IRemoteDevice[savedDevices.length()];

            for(int i = 0; i < devices.length; i++) {
                IRemoteDevice device = new RemoteMultiDevice();

                try {
                    device.load(new JSONObject(loadValue(PATH_DEVICES + "." + savedDevices.getString(i), "{}")));
                } catch(JSONException e) {
                    e.printStackTrace();
                }

                devices[i] = device;
            }
        }

        return devices;
    }

    public static void saveValue(String name, String value) {
        preferences.edit().putString(name, value).apply();
    }

    public static String loadValue(String name, String defaultString) {
        return preferences.getString(name, defaultString);
    }

    public static String loadValue(String name) {
        return loadValue(name, "");
    }

    public static void removeValue(String name) {
        preferences.edit().remove(name).apply();
    }
}