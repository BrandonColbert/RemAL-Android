package exn.database.remal.config;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import exn.database.remal.core.RemAL;
import exn.database.remal.deck.DeckTile;
import exn.database.remal.deck.ITile;
import exn.database.remal.devices.IRemoteDevice;
import exn.database.remal.devices.RemoteMultiDevice;

/**
 * Used to load, save, and remove complex objects with a {@link SharedPreferences} instance
 */
public class PersistenceUtils {
    private static final String PATH_DEVICES = "devices",
                                PATH_TILES = "tiles";

    protected static SharedPreferences preferences;

    /**
     * Sets the current shared preferences instance
     * @param preferences Instance to use
     */
    public static void setPreferences(SharedPreferences preferences) {
        PersistenceUtils.preferences = preferences;
    }

	/**
	 * Clears all the preferences
	 */
	public static void clearPreferences() {
        preferences.edit().clear().apply();
    }

	/**
	 * @return The preferences as a list of strings
	 */
	public static String[] getPreferenceStrings() {
        List<String> ss = new ArrayList<>();
        
        for(Map.Entry<String, ?> e : preferences.getAll().entrySet())
            ss.add(e.getKey() + ": " + e.getValue());
        
        return ss.toArray(new String[0]);
    }

	/**
	 * Removes a value from preferences
	 * @param name Name of the value
	 */
	protected static void removeValue(String name) {
        preferences.edit().remove(name).apply();
    }

	/**
	 * Creates a {@link DeckTile} by loading it from preferences using based on its {@link ITile#getPosition() position}
	 * @param index Index of the {@link ITile}
	 * @return The tile
	 * @throws JSONException If the the value can't be converted to a {@link JSONObject}
	 */
    public static ITile loadTile(int index) throws JSONException {
        String key = PATH_TILES + "." + index;

        if(preferences.contains(key)) {
            ITile tile = new DeckTile();
            tile.load(new JSONObject(preferences.getString(key, "{}")));

            return tile;
        }

        return null;
    }

	/**
	 * Saves the values of a tile its {@link ITile#getPosition() position}
	 * @param tile Tile to save
	 * @throws JSONException
	 */
	public static void saveTile(ITile tile) throws JSONException {
        preferences.edit().putString(PATH_TILES + "." + tile.getPosition(), tile.save()).apply();
    }

	/**
	 * Removes the values at the tile's {@link ITile#getPosition() position}
	 * @param tile Tile to get position from
	 */
	public static void removeTile(ITile tile) {
        removeValue(PATH_TILES + "." + tile.getPosition());
    }

	/**
	 * Adds the device's {@link IRemoteDevice#getName() name} to the array of device names
	 * @param device Device whose name to add
	 * @throws JSONException
	 */
	public static void addToDevicePath(IRemoteDevice device) throws JSONException {
        JSONArray savedDevices = new JSONArray(preferences.getString(PATH_DEVICES, "[]"));
        savedDevices.put(device.getName());

        preferences.edit().putString(PATH_DEVICES, savedDevices.toString()).apply();
    }

	/**
	 * Saves the values of the device to its {@link IRemoteDevice#getName() name}
	 * @param device Device to save
	 * @throws JSONException
	 */
	public static void saveDevice(IRemoteDevice device) throws JSONException {
        //Get actual device to prevent saving subdevice specifically
        String name = device.getName();
        device = RemAL.getDevice(name);

        if(device != null) //Make sure it exists
            preferences.edit().putString(PATH_DEVICES + "." + device.getName(), device.save()).apply();
    }

	/**
	 * Removes the values saved in preferences at the device's {@link IRemoteDevice#getName()} name
	 * @param device Device whose name to use
	 */
	public static void removeDeviceSave(IRemoteDevice device) {
        preferences.edit().remove(device.getName()).apply();
    }

	/**
	 * Removes the device's {@link IRemoteDevice#getName() name} from the array of device names
	 * @param device Device whose name to remove
	 * @throws JSONException
	 */
	public static void removeFromDevicePath(IRemoteDevice device) throws JSONException {
        JSONArray savedDevices = new JSONArray(preferences.getString(PATH_DEVICES, "[]")), array = new JSONArray();

        //Copy saveDevices to array
        for(int i = 0; i < savedDevices.length(); i++) {
            String s = savedDevices.getString(i);

            //Exclude the specified device's name
            if(!s.equals(device.getName()))
				array.put(s);
        }

        //Overwrite
        preferences.edit().putString(PATH_DEVICES, array.toString()).apply();
    }

	/**
	 * @return All the devices loaded into an array of {@link RemoteMultiDevice RemoteMultiDevices}
	 */
	public static IRemoteDevice[] loadDevices() {
        JSONArray savedDevices = null;

        try {
        	//Get list of device names
            savedDevices = new JSONArray(preferences.getString(PATH_DEVICES, "[]"));
        } catch(JSONException e) {
            e.printStackTrace();
        }

        IRemoteDevice[] devices = new IRemoteDevice[0];

        if(savedDevices != null) {
            devices = new IRemoteDevice[savedDevices.length()];

            //Load the device at each name into a multidevice
            for(int i = 0; i < devices.length; i++) {
                IRemoteDevice device = new RemoteMultiDevice();

                try {
                    device.load(new JSONObject(preferences.getString(PATH_DEVICES + "." + savedDevices.getString(i), "{}")));
                } catch(JSONException e) {
                    e.printStackTrace();
                }

                devices[i] = device;
            }
        }

        return devices;
    }
}