package exn.database.remal.devices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import exn.database.remal.core.RemAL;
import exn.database.remal.requests.ActionValidCallback;

/**
 * Handles connections and command sending to devices
 */
public class RemoteMultiDevice extends RemoteDevice {
    private HashMap<MultiDeviceMode, SubDevicePack> subdevices = new HashMap<>();
    private HashMap<Class<? extends IRemoteDevice>, MultiDeviceMode> classmap = new HashMap<>();
    private MultiDeviceMode currentMode;

    public RemoteMultiDevice(String name) {
        super(name);
        currentMode = MultiDeviceMode.NONE;

        register(MultiDeviceMode.USB, RemoteUSBDevice.class, new SubDevicePack(new RemoteUSBDevice(name), 0));
        register(MultiDeviceMode.LAN, RemoteLanDevice.class, new SubDevicePack(new RemoteLanDevice(name), 1));
        register(MultiDeviceMode.BLUETOOTH, RemoteBluetoothDevice.class, new SubDevicePack(new RemoteBluetoothDevice(name), 2));
        register(MultiDeviceMode.WIFI, RemoteWiFiDevice.class, new SubDevicePack(new RemoteWiFiDevice(name), 3));
        register(MultiDeviceMode.SSH, RemoteSSHDevice.class, new SubDevicePack(new RemoteSSHDevice(name), 4));
    }

    public RemoteMultiDevice() {
        super();
        currentMode = MultiDeviceMode.NONE;

        register(MultiDeviceMode.USB, RemoteUSBDevice.class, new SubDevicePack(new RemoteUSBDevice()));
        register(MultiDeviceMode.LAN, RemoteLanDevice.class, new SubDevicePack(new RemoteLanDevice()));
        register(MultiDeviceMode.BLUETOOTH, RemoteBluetoothDevice.class, new SubDevicePack(new RemoteBluetoothDevice()));
        register(MultiDeviceMode.WIFI, RemoteWiFiDevice.class, new SubDevicePack(new RemoteWiFiDevice()));
        register(MultiDeviceMode.SSH, RemoteSSHDevice.class, new SubDevicePack(new RemoteSSHDevice()));
    }

    private void register(MultiDeviceMode mode, Class<? extends IRemoteDevice> deviceClass, SubDevicePack pack) {
        subdevices.put(mode, pack);
        classmap.put(deviceClass, mode);
    }

    /**
     * @param mode Mode tied to the subdevice
     * @return The subdevice for the mode
     */
    public SubDevicePack getPack(MultiDeviceMode mode) {
        if(subdevices.containsKey(mode))
            return subdevices.get(mode);

        return null;
    }

    /**
     * @param type Class for the subdevice
     * @param <T> Remote device type
     * @return The subdevice tied to the class
     */
    public <T extends IRemoteDevice> T getSubDevice(Class<T> type) {
        if(classmap.containsKey(type))
            return (T)getPack(classmap.get(type)).getDevice();

        return null;
    }

    public boolean isConnected() {
        return currentMode != MultiDeviceMode.NONE && subdevices.get(currentMode).getDevice().isConnected();
    }

    public void disconnect() {
        if(currentMode != MultiDeviceMode.NONE)
            subdevices.get(currentMode).getDevice().disconnect();
    }

    public void sendRequest(String request, ActionValidCallback callback) {
        if(currentMode != MultiDeviceMode.NONE)
            subdevices.get(currentMode).getDevice().sendRequest(request, callback);
        else
            callback.run(false);
    }

    private volatile boolean connectionResult;
    private volatile boolean connectionWaiting;

    public void connect(ActionValidCallback callback) {
        isConnecting = true;

        List<SubDevicePack> devices = new ArrayList<>();

        for(Map.Entry<MultiDeviceMode, SubDevicePack> entry : subdevices.entrySet())
            devices.add(entry.getValue());

        Collections.sort(devices, (lhs, rhs) -> Integer.compare(lhs.getOrder(), rhs.getOrder()));

        connectionResult = false;

        new Thread(() -> {
            for(int i = 0; i < devices.size(); i++) {
                SubDevicePack pack = devices.get(i);

                if(pack.isEnabled()) {
                    IRemoteDevice device = pack.getDevice();

                    RemAL.displayText("Trying '" + name + "' through " + device.getConnectionName() + "...");

                    connectionWaiting = true;

                    try {
                        device.connect(valid -> {
                            connectionResult = valid;
                            connectionWaiting = false;
                        });
                    } catch(Exception e) {
                        connectionWaiting = false;
                    }

                    while(connectionWaiting);

                    if(connectionResult)
                        break;
                }
            }

            isConnecting = false;
            callback.run(connectionResult);
        }).start();
    }

    @Override
    public void setName(String name) {
        super.setName(name);

        for(Map.Entry<MultiDeviceMode, SubDevicePack> pair : subdevices.entrySet())
            pair.getValue().getDevice().setName(name);
    }

    public String getConnectionName() {
        return currentMode != MultiDeviceMode.NONE ? subdevices.get(currentMode).getDevice().getConnectionName() : "Generic";
    }

    public String getConnectionDescription() {
        return currentMode != MultiDeviceMode.NONE ? subdevices.get(currentMode).getDevice().getConnectionDescription() : super.getConnectionDescription();
    }

    /**
     * @return The mode for the subdevice currently being used
     */
    public MultiDeviceMode getCurrentMode() {
        return currentMode;
    }

    @Override
    public String save() {
        JSONObject data = new JSONObject();

        try {
            data.put("name", name);

            for(Map.Entry<MultiDeviceMode, SubDevicePack> entry : subdevices.entrySet()) {
                SubDevicePack pack = entry.getValue();
                IRemoteDevice device = pack.getDevice();
                String key = entry.getKey().name();

                data.put("subdevice." + key, device.save());
                data.put("subdevice." + key + ".order", pack.getOrder());
                data.put("subdevice." + key + ".enabled", pack.isEnabled());
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return data.toString();
    }

    @Override
    public void load(String data) {
        try {
            JSONObject obj = new JSONObject(data);

            name = obj.getString("name");

            Iterator<String> keys = obj.keys();

            while(keys.hasNext()) {
                String key = keys.next();

                if(!key.startsWith("subdevice.") || (key.endsWith(".order") || key.endsWith(".enabled")))
                    continue;

                SubDevicePack pack = subdevices.get(MultiDeviceMode.valueOf(key.substring("subdevice.".length())));
                pack.getDevice().load(obj.getString(key));
                pack.setOrder(obj.getInt(key + ".order"));
                pack.setEnabled(obj.getBoolean(key + ".enabled"));
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }
}
