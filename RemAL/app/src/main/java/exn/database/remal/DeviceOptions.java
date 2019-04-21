package exn.database.remal;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import exn.database.remal.devices.IRemoteDevice;

public class DeviceOptions  extends AppCompatActivity {
    private IRemoteDevice device;
    private String lastDeviceName;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_options);
        getSupportFragmentManager().beginTransaction().replace(R.id.option_content, new DeviceOptionsFragment()).commit();

        /*
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        if(bar != null)
            bar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        lastDeviceName = intent.getStringExtra(EditDevices.DO_EXTRA);
        device = RemAL.getDevice(lastDeviceName);

        EditText textDeviceName = findViewById(R.id.device_name);
        textDeviceName.setText(lastDeviceName);
        textDeviceName.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            public void afterTextChanged(Editable s) {
                String newName = s.toString();

                RemAL.renameDevice(lastDeviceName, newName);

                lastDeviceName = newName;
            }
        });
        */
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_device_options, menu);

        return true;
    }
    */
}
