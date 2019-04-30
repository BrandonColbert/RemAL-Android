package exn.database.remal;

import android.content.Context;
import android.widget.Button;

import exn.database.remal.devices.IRemoteDevice;

public class DeviceOptionButton extends Button {
    private IRemoteDevice device;

    public DeviceOptionButton(Context context, IRemoteDevice device) {
        super(context);
    }

    public void updateText() {
        setText(device.getName() + ": " + (device.isConnected() ? "Connected" : "Disconnected"));
    }
}
