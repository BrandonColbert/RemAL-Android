package exn.database.remal;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.HashMap;

import exn.database.remal.core.DeviceEvent;
import exn.database.remal.devices.MultiDeviceMode;
import exn.database.remal.devices.RemoteMultiDevice;
import exn.database.remal.events.DeviceConnectEvent;
import exn.database.remal.events.DeviceCreatedEvent;
import exn.database.remal.events.DeviceDestroyedEvent;
import exn.database.remal.events.DeviceDisconnectEvent;
import exn.database.remal.events.DeviceRenamedEvent;
import exn.database.remal.core.IRemalEventListener;
import exn.database.remal.core.RemAL;
import exn.database.remal.core.RemalEvent;
import exn.database.remal.devices.IRemoteDevice;

import static android.widget.LinearLayout.LayoutParams;

public class EditDevices extends AppCompatActivity implements IRemalEventListener {
    public static final String DO_EXTRA = "exn.database.remal.devices.DEVICE_OPTIONS_EXTRA";
    private static final int MAX_DEVICES = 1000;

    private ViewGroup devicesLayout;
    private HashMap<String, Button> deviceButtons;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_devices);

        deviceButtons = new HashMap<>();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        if(bar != null)
            bar.setDisplayHomeAsUpEnabled(true);

        devicesLayout = findViewById(R.id.devices_table);
        for(IRemoteDevice device : RemAL.getDevices())
            addEditDeviceButton(device);

        RemAL.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        RemAL.unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_edit_devices, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_device:
                int index = 1;

                while(!RemAL.createDevice("Device " + index) && index < MAX_DEVICES)
                    index++;

                IRemoteDevice device = RemAL.getDevice("Device " + index);

                if(device != null)
                    addEditDeviceButton(device);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onRemalEvent(RemalEvent event) {
        if(event instanceof DeviceRenamedEvent) {
            DeviceRenamedEvent e = (DeviceRenamedEvent)event;
            Button button = deviceButtons.remove(e.oldName);
            deviceButtons.put(e.device.getName(), button);

            if(button != null) {
                refreshButtonText(button, e.device);
                refreshButtonAction(button, e.device);
            }
        } else if(event instanceof DeviceCreatedEvent) {
            addEditDeviceButton(((DeviceEvent)event).device);
        } else if(event instanceof DeviceDestroyedEvent) {
            DeviceEvent e = (DeviceEvent)event;
            Button button = deviceButtons.remove(e.device.getName());

            if(button != null)
                devicesLayout.removeView(button);
        } else if(event instanceof DeviceConnectEvent || event instanceof DeviceDisconnectEvent) {
            DeviceEvent e = (DeviceEvent)event;
            Button button = deviceButtons.get(e.device.getName());

            if(button != null)
                refreshButtonText(button, e.device);
        }
    }

    private void refreshButtonText(Button button, IRemoteDevice device) {
        StringBuilder sb = new StringBuilder();

        sb.append(device.getName());
		sb.append(" - ");

        if((!(device instanceof RemoteMultiDevice) || ((RemoteMultiDevice)device).getCurrentMode() != MultiDeviceMode.NONE) && device.isConnected())
            sb.append(device.getConnectionDescription());
        else
            sb.append("Disconnected");

        button.setText(sb.toString());
    }

    private void refreshButtonAction(Button button, IRemoteDevice device) {
        button.setOnClickListener(v -> {
            Intent intent = new Intent(this, DeviceOptions.class);
            intent.putExtra(DO_EXTRA, device.getName());

            startActivity(intent);
        });
    }

    private void addEditDeviceButton(IRemoteDevice device) {
        String deviceName = device.getName();

        if(!deviceButtons.containsKey(deviceName)) {
            Button button = new Button(this);
            button.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            refreshButtonText(button, device);
            refreshButtonAction(button, device);

            devicesLayout.addView(button);
            deviceButtons.put(deviceName, button);
        }
    }
}
