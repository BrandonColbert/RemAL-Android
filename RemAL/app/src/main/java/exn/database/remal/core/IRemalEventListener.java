package exn.database.remal.core;

public interface IRemalEventListener {
    /**
     * Catches any and all RemAL events
     * @param event Received event
     */
    void onRemalEvent(RemalEvent event);
}
