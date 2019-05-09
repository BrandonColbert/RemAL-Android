package exn.database.remal;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import exn.database.remal.core.IRemalEventListener;
import exn.database.remal.core.RemAL;
import exn.database.remal.core.RemalEvent;
import exn.database.remal.devices.IRemoteDevice;
import exn.database.remal.devices.MultiDeviceMode;
import exn.database.remal.devices.RemoteMultiDevice;
import exn.database.remal.events.DeviceConnectEvent;
import exn.database.remal.events.DeviceCreatedEvent;
import exn.database.remal.events.DeviceDestroyedEvent;
import exn.database.remal.events.DeviceDisconnectEvent;
import exn.database.remal.events.DeviceEvent;
import exn.database.remal.events.DeviceRenamedEvent;

public class EditDevices extends AppCompatActivity implements IRemalEventListener {
    class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static final String DO_EXTRA = "exn.database.remal.devices.DEVICE_OPTIONS_EXTRA";
    private static final int MAX_DEVICES = 1000;

    private final RecyclerView.Adapter adapter = new RecyclerView.Adapter() {
        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(new Button(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            IRemoteDevice device = RemAL.getDevices()[position];
            Button button = (Button)holder.itemView;

            refreshButtonText(button, device);
            refreshButtonAction(button, device);
        }

        @Override
        public int getItemCount() {
            return RemAL.getDevices().length;
        }
    };

    private final ItemTouchHelper touch = new ItemTouchHelper(new ItemTouchHelper.Callback() {
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END);
        }

        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            int start = viewHolder.getAdapterPosition(), end = target.getAdapterPosition();
            adapter.notifyItemMoved(start, end);

            IRemoteDevice[] devices = RemAL.getDevices();
            IRemoteDevice movedDevice = devices[start];
            movedDevice.setOrder(end);
            RemAL.saveDevice(movedDevice);

            if(start < end) {
                for(int i = start + 1; i <= end; i++) {
                    IRemoteDevice device = devices[i];
                    device.setOrder(i - 1);
                    RemAL.saveDevice(device);
                }
            } else {
                for(int i = end; i < start; i++) {
                    IRemoteDevice device = devices[i];
                    device.setOrder(i + 1);
                    RemAL.saveDevice(device);
                }
            }

            return true;
        }

        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {}
    });

    private RecyclerView devicesLayout;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_devices);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        if(bar != null)
            bar.setDisplayHomeAsUpEnabled(true);

        devicesLayout = findViewById(R.id.devices_table);
        devicesLayout.setLayoutManager(new LinearLayoutManager(this));
        devicesLayout.setAdapter(adapter);
        touch.attachToRecyclerView(devicesLayout);

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

                if(RemAL.getDevice("Device " + index) != null)
                    adapter.notifyItemInserted(RemAL.getDevices().length);

                return true;
            case R.id.reconnect_all_devices:
                RemAL.connectAllDevices();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRemalEvent(RemalEvent event) {
        if(event instanceof DeviceRenamedEvent || event instanceof DeviceConnectEvent || event instanceof DeviceDisconnectEvent)
            runOnUiThread(adapter::notifyDataSetChanged);
        else if(event instanceof DeviceCreatedEvent)
            adapter.notifyItemInserted(((DeviceEvent)event).device.getOrder());
        else if(event instanceof DeviceDestroyedEvent)
            adapter.notifyItemRemoved(((DeviceEvent)event).device.getOrder());
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
}
