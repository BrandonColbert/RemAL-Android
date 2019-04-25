package exn.database.remal.config;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Allows a class to save and load itself through a string
 */
public interface IPersistent {
    /**
     * Saves this object
     * @param data Data instance to add values to
     * @return A JSON object containing the persistent data for this class instance
     * @throws JSONException
     */
    JSONObject save(JSONObject data) throws JSONException;

    /**
     * Saves using a newly created data instance
     * @return A string containing the data
     * @throws JSONException
     */
    default String save() throws JSONException {
        return save(new JSONObject()).toString();
    }

    /**
     * Loads from JSON object
     * @param data The persistent data for this instance
     */
    void load(JSONObject data) throws JSONException;
}
