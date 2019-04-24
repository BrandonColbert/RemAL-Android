package exn.database.remal;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import exn.database.remal.core.DeviceEvent;
import exn.database.remal.core.IRemalEventListener;
import exn.database.remal.core.RemAL;
import exn.database.remal.core.RemalEvent;
import exn.database.remal.devices.IRemoteDevice;
import exn.database.remal.events.DeviceConfigChangedEvent;
import exn.database.remal.events.DeviceCreatedEvent;
import exn.database.remal.events.DeviceDestroyedEvent;
import exn.database.remal.events.DeviceRenamedEvent;

public class AppLaunch extends AppCompatActivity implements IRemalEventListener {
    private boolean isFullscreen;
    private View appTable;
    private Toolbar toolbar;
    private boolean isEditing;
    private int columns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_launch);

        RemAL.setMainActivity(this);
        RemAL.loadSettings();
        RemAL.connectDevices();

        appTable = findViewById(R.id.app_table);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toggleFullscreen(false);

        appTable.setOnClickListener(view -> {
            if(isFullscreen)
                toggleFullscreen(false);
        });

        RemAL.register(this);

        //TODO: Load from save
        columns = 5;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        RemAL.unregister(this);
    }

    private void toggleFullscreen(boolean value) {
        isFullscreen = value;

        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            if(value)
                actionBar.show();
            else
                actionBar.hide();
        }

        if(value)
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        else
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            toggleFullscreen(!isFullscreen);

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.edit_devices:
                startActivity(new Intent(this, EditDevices.class));
                return true;
            case R.id.edit_app_layout:
                toggleEditMode();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleEditMode() {
        isEditing = !isEditing;


    }

    public void onRemalEvent(RemalEvent event) {
        try {
            if(event instanceof DeviceCreatedEvent) {
                DeviceCreatedEvent e = (DeviceCreatedEvent)event;
                String name = e.device.getName();

                JSONArray savedDevices = new JSONArray(RemAL.loadValue(RemAL.PATH_DEVICES, "[]"));
                savedDevices.put(name);

                RemAL.saveValue(RemAL.PATH_DEVICES, savedDevices.toString());
                RemAL.saveValue(RemAL.PATH_DEVICES + "." + name, e.device.save());
            } else if(event instanceof DeviceRenamedEvent) {
                DeviceRenamedEvent e = (DeviceRenamedEvent)event;

                JSONArray savedDevices = new JSONArray(RemAL.loadValue(RemAL.PATH_DEVICES, "[]"));

                List<String> savedDevicesList = new ArrayList<>();

                for(int i = 0; i < savedDevices.length(); i++) {
                    String s = savedDevices.getString(i);

                    if(!s.equals(e.oldName))
                        savedDevicesList.add(s);
                }

                savedDevicesList.add(e.device.getName());

                savedDevices = new JSONArray();

                for(String s : savedDevicesList)
                    savedDevices.put(s);


                RemAL.saveValue(RemAL.PATH_DEVICES, savedDevices.toString());
                RemAL.removeSave(RemAL.PATH_DEVICES + "." + e.oldName);
                RemAL.saveValue(RemAL.PATH_DEVICES + "." + e.device.getName(), e.device.save());
            } else if(event instanceof DeviceDestroyedEvent) {
                DeviceDestroyedEvent e = (DeviceDestroyedEvent)event;

                JSONArray savedDevices = new JSONArray(RemAL.loadValue(RemAL.PATH_DEVICES, "[]"));

                List<String> savedDevicesList = new ArrayList<>();

                for(int i = 0; i < savedDevices.length(); i++) {
                    String s = savedDevices.getString(i);

                    if(!s.equals(e.device.getName()))
                        savedDevicesList.add(s);
                }

                savedDevices = new JSONArray();

                for(String s : savedDevicesList)
                    savedDevices.put(s);


                RemAL.saveValue(RemAL.PATH_DEVICES, savedDevices.toString());
                RemAL.removeSave(RemAL.PATH_DEVICES + "." + e.device.getName());
            } else if(event instanceof DeviceConfigChangedEvent) {
                DeviceEvent e = (DeviceEvent)event;

                //Get actual device to prevent saving subdevice specifically
                IRemoteDevice device = e.device;
                String name = device.getName();
                device = RemAL.getDevice(name);

                if(device != null)
                    RemAL.saveValue(RemAL.PATH_DEVICES + "." + name, device.save());
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }
}
