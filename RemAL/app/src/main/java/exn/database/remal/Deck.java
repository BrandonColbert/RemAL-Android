package exn.database.remal;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.Map;

import exn.database.remal.config.PersistenceUtils;
import exn.database.remal.core.IRemalEventListener;
import exn.database.remal.core.RemAL;
import exn.database.remal.core.RemalEvent;
import exn.database.remal.deck.ITile;
import exn.database.remal.devices.IRemoteDevice;
import exn.database.remal.events.ColumnAmountChangedEvent;
import exn.database.remal.events.TileChangedEvent;
import exn.database.remal.events.TileDestroyedEvent;
import exn.database.remal.ui.SquareView;
import exn.database.remal.ui.TileButton;

public class Deck extends AppCompatActivity implements IRemalEventListener {
    public static final int DEFAULT_COLUMNS = 3, MAX_TILES = 100;

    private boolean isFullscreen;
    private TableLayout appTable;
    private Toolbar toolbar;
    private boolean isEditing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck);

        PersistenceUtils.loadPreferences(this);
        RemAL.setMainActivity(this);
        RemAL.loadAndConnectDevices();

        //TODO: Remove
        //PersistenceUtils.getPreferences().edit().clear().apply();
        for(Map.Entry<String, ?> e : PersistenceUtils.getPreferences().getAll().entrySet())
            RemAL.log(e.getKey() + ": " + e.getValue());

        appTable = findViewById(R.id.app_table);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toggleFullscreen(true);

        appTable.setOnClickListener(view -> {
            if(isFullscreen)
                toggleFullscreen(true);
        });

        int columns = Integer.valueOf(PersistenceUtils.loadValue("appearance_columns", String.valueOf(DEFAULT_COLUMNS)));
        updateView(columns);

        TableRow lastRow = ((TableRow)appTable.getChildAt(appTable.getChildCount() - 1));
        for(int i = 0; i < MAX_TILES; i++) {
            ITile tile = RemAL.getTile(i);

            if(tile != null)
                lastRow.addView(createTileButton(tile));
        }

        updateView(columns);
        toggleEditMode(false);

        RemAL.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                toggleEditMode(!isEditing);
                return true;
            case R.id.edit_settings:
                startActivity(new Intent(this, DeckSettings.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRemalEvent(RemalEvent event) {
        if(event instanceof ColumnAmountChangedEvent) {
            updateView(((ColumnAmountChangedEvent)event).columns);
        } else if(event instanceof TileDestroyedEvent) {
            int index = ((TileDestroyedEvent)event).tile.getPosition();
            int columns = Integer.valueOf(PersistenceUtils.loadValue("appearance_columns", String.valueOf(DEFAULT_COLUMNS)));
            int column = index % columns;

            TableRow tableRow = (TableRow)appTable.getChildAt(index / columns);
            tableRow.removeViewAt(column);
            tableRow.addView(createPlaceholderView(), column);
        }
    }

    /**
     * Toggles fullscreen mode
     * @param value Whether to be fullscreen
     */
    private void toggleFullscreen(boolean value) {
        isFullscreen = value;

        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            if(value)
                actionBar.hide();
            else
                actionBar.show();
        }

        if(value)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * Set whether tiles are being edited
     * @param value Whether editing is occuring
     */
    private void toggleEditMode(boolean value) {
        isEditing = value;

        boolean anyTilesVisibleYet = false;
        for(int i = appTable.getChildCount() - 1; i >= 0; i--) {
            TableRow row = (TableRow)appTable.getChildAt(i);
            int count = row.getChildCount();

            if(!anyTilesVisibleYet) {
                for(int j = 0; j < count; j++) {
                    if(row.getChildAt(j) instanceof TileButton) {
                        anyTilesVisibleYet = true;
                        break;
                    }
                }
            }

            for(int j = 0; j < count; j++) {
                View v = row.getChildAt(j);

                if(v instanceof SquareView)
                    v.setVisibility(value ? View.VISIBLE : (anyTilesVisibleYet ? View.INVISIBLE : View.GONE));
            }
        }
    }

    /**
     * Updates the view based on the number of columns
     * @param columns Number of columns in the app table
     */
    private void updateView(int columns) {
        //Add placeholder views to fill in missing tiles
        for(int i = 0; i < MAX_TILES / columns; i++) {
            TableRow row = (TableRow)appTable.getChildAt(i);

            if(row == null) {
                row = new TableRow(this);

                TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                row.setLayoutParams(params);

                appTable.addView(row);
            }

            //Add placeholder views if lacking
            while(row.getChildCount() < columns)
                row.addView(createPlaceholderView());
        }

        //Reposition DeckTiles based on index
        for(int i = 0; i < appTable.getChildCount(); i++) {
            TableRow rowView = (TableRow)appTable.getChildAt(i);

            for(int j = 0; j < rowView.getChildCount(); j++) {
                View view = rowView.getChildAt(j);

                if(view instanceof TileButton) {
                    TileButton button = (TileButton)view;

                    int index = button.getTile().getPosition();

                    int row = index / columns;
                    int column = index % columns;

                    if(i == row) {
                        if(j != column) {
                            rowView.removeViewAt(j);
                            rowView.addView(button, column);

                            j = 0;
                        }
                    } else {
                        TableRow otherRow = (TableRow)appTable.getChildAt(row);

                        rowView.removeViewAt(j);
                        otherRow.addView(button, column);

                        j = 0;
                    }
                }
            }
        }

        //Remove excess views
        for(int i = 0; i < appTable.getChildCount(); i++) {
            TableRow row = (TableRow)appTable.getChildAt(i);

            //Remove placeholder from end
            int rowElements;
            while((rowElements = row.getChildCount()) > columns)
                row.removeViewAt(rowElements - 1);
        }
    }

    /**
     * Creates a button for a tile
     * @param tile Tile
     */
    private TileButton createTileButton(ITile tile) {
        TileButton button = new TileButton(this);
        button.setTextColor(getResources().getColor(R.color.colorAltText));
        button.setTile(tile);

        TableRow.LayoutParams params = new TableRow.LayoutParams(0, 0, 1f);
        params.setMargins(5, 5, 5, 5);
        button.setLayoutParams(params);

        button.setOnClickListener(v -> {
            if(isEditing) {
                Intent intent = new Intent(this, TileOptions.class);
                intent.putExtra(TileOptions.TO_EXTRA, button.getTile());

                startActivity(intent);
            } else {
                if(!isFullscreen)
                    toggleFullscreen(true);

                button.getTile().send((valid) -> {});
            }


        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            button.setOnLongClickListener(v -> {
                if(isEditing) {
                    String posString = String.valueOf(tile.getPosition());

                    v.startDragAndDrop(
                        new ClipData(posString, new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, new ClipData.Item(posString)),
                        new View.DragShadowBuilder(v),
                        v, 0
                    );
                }

                return true;
            });
        }

        button.setOnDragListener((v, e) -> {
            switch(e.getAction()) {
                case DragEvent.ACTION_DROP:
                    int index = Integer.valueOf(e.getClipData().getItemAt(0).getText().toString());

                    RemAL.log("Swapping " + index + " with " + button.getTile().getPosition());

                    ITile draggedTile = RemAL.getTile(index);
                    draggedTile.setPosition(button.getTile().getPosition());
                    button.getTile().setPosition(index);

                    RemAL.post(new TileChangedEvent(draggedTile));
                    RemAL.post(new TileChangedEvent(button.getTile()));

                    RemAL.saveTile(draggedTile);
                    RemAL.saveTile(button.getTile());

                    v.invalidate();
                    break;
            }

            return true;
        });

        return button;
    }

    /**
     * @return A new placeholder view
     */
    private SquareView createPlaceholderView() {
        SquareView view = new SquareView(this);
        view.setBackground(getResources().getDrawable(R.drawable.ic_baseline_crop_free_24px));
        view.setVisibility(isEditing ? View.VISIBLE : View.INVISIBLE);

        TableRow.LayoutParams params = new TableRow.LayoutParams(0, 0, 1f);
        params.setMargins(5, 5, 5, 5);
        view.setLayoutParams(params);

        view.setOnClickListener(v -> {
            int columns = Integer.valueOf(PersistenceUtils.loadValue("appearance_columns", String.valueOf(DEFAULT_COLUMNS)));

            TableRow rowView = (TableRow)v.getParent();
            int relativeIndex = rowView.indexOfChild(v);
            int index = appTable.indexOfChild(rowView) * columns + relativeIndex;

            IRemoteDevice[] devices = RemAL.getDevices();
            ITile tile = RemAL.createTile(devices.length > 0 ? devices[0] : null, index);
            RemAL.saveTile(tile);

            rowView.removeViewAt(relativeIndex);
            rowView.addView(createTileButton(tile), relativeIndex);

            Intent intent = new Intent(this, TileOptions.class);
            intent.putExtra(TileOptions.TO_EXTRA, tile);

            startActivity(intent);
        });

        view.setOnDragListener((v, e) -> {
            switch(e.getAction()) {
                case DragEvent.ACTION_DROP:
                    int index = Integer.valueOf(e.getClipData().getItemAt(0).getText().toString());

                    int columns = Integer.valueOf(PersistenceUtils.loadValue("appearance_columns", String.valueOf(DEFAULT_COLUMNS)));
                    int row = index / columns;
                    int column = index % columns;
                    TableRow originalRow = (TableRow)appTable.getChildAt(row);
                    originalRow.removeViewAt(column);
                    originalRow.addView(createPlaceholderView(), column);

                    TableRow rowView = (TableRow)v.getParent();
                    int relativeIndex = rowView.indexOfChild(v);
                    rowView.removeViewAt(relativeIndex);
                    rowView.addView(createTileButton(RemAL.getTile(index)), relativeIndex);

                    v.invalidate();
                    break;
            }

            return true;
        });

        return view;
    }
}