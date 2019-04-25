package exn.database.remal.config;

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

import static exn.database.remal.core.RemAL.getMainActivity;

public class PersistenceUtils {
    private static final String PACKAGE = "exn.database.remal",
            PATH_DEVICES = "devices",
            PATH_REQUESTS = "requests";

    private static SharedPreferences preferences;

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
        loadPreferences();

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

    private static void loadPreferences() {
        if(getMainActivity() != null)
            preferences = getMainActivity().getSharedPreferences(PACKAGE, Context.MODE_PRIVATE);
    }

    private static void saveValue(String name, String value) {
        loadPreferences();
        preferences.edit().putString(name, value).apply();
    }

    private static String loadValue(String name, String defaultString) {
        loadPreferences();
        return preferences.getString(name, defaultString);
    }

    private static void removeSave(String name) {
        loadPreferences();
        preferences.edit().remove(name).apply();
    }

    private static String loadValue(String name) {
        return loadValue(name, "");
    }
}
