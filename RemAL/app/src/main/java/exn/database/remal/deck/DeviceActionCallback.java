package exn.database.remal.deck;

/**
 * Called after an interaction with the device occured
 */
public interface DeviceActionCallback {
    /**
     * Called when the interaction finishes occur
     * @param valid Whether the action that occurred was valid or not
     */
    void run(boolean valid);
}