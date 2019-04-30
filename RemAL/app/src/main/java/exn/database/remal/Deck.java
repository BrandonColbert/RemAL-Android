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

import exn.database.remal.config.PersistenceUtils;
import exn.database.remal.events.DeviceEvent;
import exn.database.remal.core.IRemalEventListener;
import exn.database.remal.core.RemAL;
import exn.database.remal.core.RemalEvent;
import exn.database.remal.devices.IRemoteDevice;
import exn.database.remal.events.DeviceConfigChangedEvent;
import exn.database.remal.events.DeviceCreatedEvent;
import exn.database.remal.events.DeviceDestroyedEvent;
import exn.database.remal.events.DeviceRenamedEvent;

public class Deck extends AppCompatActivity {
    private boolean isFullscreen;
    private View appTable;
    private Toolbar toolbar;
    private boolean isEditing;
    private int columns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck);

        RemAL.setMainActivity(this);
        RemAL.loadAndConnectDevices();

        appTable = findViewById(R.id.app_table);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toggleFullscreen(false);

        appTable.setOnClickListener(view -> {
            if(isFullscreen)
                toggleFullscreen(false);
        });

        //TODO: Load from save
        columns = 5;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            case R.id.edit_settings:
                startActivity(new Intent(this, DeckSettings.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleEditMode() {
        isEditing = !isEditing;

        //TODO: Implement
    }
}
