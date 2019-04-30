package exn.database.remal;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.List;

import exn.database.remal.config.PersistenceUtils;
import exn.database.remal.core.RemAL;
import exn.database.remal.deck.ITile;

public class Deck extends AppCompatActivity {
    private class TileButtonPack {
        public Button button;
        public ITile tile;

        public TileButtonPack(ITile tile, Button button) {
            this.tile = tile;
            this.button = button;
        }
    }

    public static final int MAX_ROWS = 100, MAX_COLUMNS = 10;
    private List<TileButtonPack> tiles = new ArrayList<>();

    private boolean isFullscreen;
    private TableLayout appTable;
    private Toolbar toolbar;
    private boolean isEditing;
    private int columns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck);

        PersistenceUtils.loadPreferences(this);
        RemAL.setMainActivity(this);
        RemAL.loadAndConnectDevices();
        RemAL.loadTiles();

        appTable = findViewById(R.id.app_table);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toggleFullscreen(false);

        appTable.setOnClickListener(view -> {
            if(isFullscreen)
                toggleFullscreen(false);
        });

        setColumns(Integer.valueOf(PersistenceUtils.loadValue("columns")));

        for(int i = 0; i < MAX_COLUMNS; i++) {
            for(int j = 0; j < MAX_ROWS; j++) {

            }
        }

        for(ITile tile : RemAL.getTiles())
            createTileButton(tile, 0, 0);
    }

    public void fillEmpty() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void setColumns(int columns) {
        this.columns = columns;

        for(int i = 0; i < appTable.getChildCount(); i++) {
            TableRow row = (TableRow)appTable.getChildAt(i);
            row.setWeightSum(columns);
        }
    }

    /**
     * Creates a button for a tile
     * @param tile Tile
     * @param row Row starting at 0
     * @param column Column starting at 0
     */
    private void createTileButton(ITile tile, int row, int column) {
        Button button = new Button(this);
        button.setText(tile.getName());
        button.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));

        for(int i = 0; i < appTable.getChildCount(); i++) {
            TableRow r = (TableRow)appTable.getChildAt(i);

            if(r.getChildCount() < columns)
                r.addView(button);
        }

        tiles.add(new TileButtonPack(tile, button));
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