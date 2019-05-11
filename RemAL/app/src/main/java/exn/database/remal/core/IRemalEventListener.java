package exn.database.remal.core;

/**
 * Allows a class be notified when {@link RemAL#post(RemalEvent)} is called
 */
public interface IRemalEventListener {
    /**
     * Called when {@link RemAL#post(RemalEvent)} is called
     * @param event Received event
     */
    void onRemalEvent(RemalEvent event);
}
