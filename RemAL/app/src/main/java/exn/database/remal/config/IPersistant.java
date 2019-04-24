package exn.database.remal.config;

/**
 * Allows a class to save and load itself through a string
 */
public interface IPersistant {
    /**
     * @return A string containing the persistant data for this class instance
     */
    String save();

    /**
     * @param data The persistant data for this instance
     */
    void load(String data);
}
