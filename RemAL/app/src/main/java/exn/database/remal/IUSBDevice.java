package exn.database.remal;

public interface IUSBDevice extends IRemoteDevice {
    default void disconnect() {
        //TODO: Implement
    }

    default boolean connect() {
        //TODO: Implement

        return false;
    }

    default boolean sendCommand(String command) {
        //TODO: Implement

        return false;
    }
}
