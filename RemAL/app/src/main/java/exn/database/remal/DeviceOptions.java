package exn.database.remal;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import exn.database.remal.core.IRemalEventListener;
import exn.database.remal.core.RemAL;
import exn.database.remal.core.RemalEvent;
import exn.database.remal.devices.IRemoteDevice;
import exn.database.remal.devices.RemoteMultiDevice;
import exn.database.remal.events.DeviceRenamedEvent;

public class DeviceOptions  extends AppCompatActivity implements IRemalEventListener {
    private IRemoteDevice device;
    private ActionBar bar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_options);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bar = getSupportActionBar();
        if(bar != null)
            bar.setDisplayHomeAsUpEnabled(true);

        device = RemAL.getDevice(getIntent().getStringExtra(EditDevices.DO_EXTRA));

        if(device instanceof RemoteMultiDevice) {
            DeviceOptionsFragment frag = new DeviceOptionsFragment();
            frag.setDevice((RemoteMultiDevice)device);

            getSupportFragmentManager().beginTransaction().replace(R.id.device_options_content, frag).commit();
        }

        updateTitle();

        RemAL.register(this);
    }

    public void onDestroy() {
        super.onDestroy();

        RemAL.unregister(this);
    }

    @Override
    public void onRemalEvent(RemalEvent event) {
        if(event instanceof DeviceRenamedEvent) {
            DeviceRenamedEvent e = (DeviceRenamedEvent)event;

            if(e.device == device)
                updateTitle();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_device_options, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.device_reconnect:
                if(device.isConnected())
                    RemAL.displayText("Already connected");
                else
                    device.connect(valid -> RemAL.displayText(valid ? "Connected" : "Couldn't connect"));

                return true;
            case R.id.device_disconnect:
                if(device.isConnected()) {
                    device.disconnect();
                    RemAL.displayText("Disconnected");
                } else {
                    RemAL.displayText("Not connected");
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateTitle() {
        if(bar != null) {
            StringBuilder name = new StringBuilder();

            name.append(device.getName());
            name.append(": ");
            name.append(device.getConnectionDescription());

            bar.setTitle(name);
        }
    }
}
