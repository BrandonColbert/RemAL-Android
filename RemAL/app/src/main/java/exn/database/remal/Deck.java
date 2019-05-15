package exn.database.remal;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;

import exn.database.remal.config.PersistenceUtils;
import exn.database.remal.config.PersistentValues;
import exn.database.remal.core.IRemalEventListener;
import exn.database.remal.core.RemAL;
import exn.database.remal.core.RemalEvent;
import exn.database.remal.deck.ITile;
import exn.database.remal.deck.TileLevelTracker;
import exn.database.remal.devices.IRemoteDevice;
import exn.database.remal.devices.RemoteWiFiDevice;
import exn.database.remal.events.ColumnAmountChangedEvent;
import exn.database.remal.events.DeckColorChangedEvent;
import exn.database.remal.events.DeviceTileCreateEvent;
import exn.database.remal.events.MaxTilesChangedEvent;
import exn.database.remal.events.TileChangedEvent;
import exn.database.remal.events.TileDestroyedEvent;
import exn.database.remal.ui.SquareView;
import exn.database.remal.ui.TileButton;

/**
 * The deck is where interactions with {@link exn.database.remal.deck.DeckTile tiles} occurs
 */
public class Deck extends AppCompatActivity implements IRemalEventListener {
    private TableLayout appTable;
    private View hoverView;
    private boolean isEditing, isFullscreen;
    private int scrollDirection, scrollSpeedModifier, tileEditColor, tileColor, tilePlaceholderColor, textColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck);
        appTable = findViewById(R.id.app_table);

        //Setup RemAl
        RemAL.setMainActivity(this);
		RemAL.register(this);

		//Load preferences, devices, and tiles
		PersistenceUtils.setPreferences(getSharedPreferences("exn.database.remal", Context.MODE_PRIVATE));
        RemAL.loadDevices();
        RemAL.connectAllDevices();
        TileLevelTracker.rebuild();

        //Set the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Clicking on the view sets to fullscreen
        appTable.setOnClickListener(view -> {
            if(isFullscreen)
                toggleFullscreen(true);
        });

        //Update the view, colors, and default editing to off
        updateView();
        updateDeckColors();
        toggleEditMode(false);

        //Allow dragging near the top and bottom during edit mode to scroll
        setupDragScroll();

        //Set to fullscreen if at least one tile exists
        toggleFullscreen(TileLevelTracker.getTileCount() != 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        //Make the back button on the deck toggle fullscreen
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
        if(event instanceof ColumnAmountChangedEvent || event instanceof MaxTilesChangedEvent || event instanceof DeviceTileCreateEvent) {
            runOnUiThread(this::updateView);
        } else if(event instanceof TileDestroyedEvent) {
            int index = ((TileDestroyedEvent)event).tile.getPosition();
            int columns = PersistentValues.getColumns();
            int column = index % columns;

            TableRow tableRow = (TableRow)appTable.getChildAt(index / columns);
            tableRow.removeViewAt(column);
            tableRow.addView(createPlaceholderView(), column);
        } else if(event instanceof DeckColorChangedEvent) {
            if(((DeckColorChangedEvent)event).didReset)
                resetDeckColors();
            else
                updateDeckColors();
        } else if(event instanceof TileChangedEvent) {
            TileChangedEvent e = (TileChangedEvent)event;
            int index = e.tile.getPosition();
            int columns = PersistentValues.getColumns();

            View v = ((TableRow)appTable.getChildAt(index / columns)).getChildAt(index % columns);

            if(v instanceof TileButton)
                ((TileButton)v).setTile(e.tile);
        }
    }

    /**
     * Allows scrolling when moving tiles by moving near the top and bottom of the screen
     */
    private void setupDragScroll() {
        final float maxSpeed = 3f;
        scrollDirection = 0;
        scrollSpeedModifier = 1;

        final ScrollView deckScroll = findViewById(R.id.deckScroll);
        final Handler scrollHandler = new Handler();
        final Runnable scrollRun = new Runnable() {
            public void run() {
                //Only scroll when a direction is given
                if(scrollDirection != 0) {
                    deckScroll.smoothScrollBy(0, scrollDirection * 10 * scrollSpeedModifier);
                    scrollHandler.postDelayed(this, 5);
                }
            }
        };

        //Handle upwards scrolling
        findViewById(R.id.deckScrollTop).setOnDragListener((v, e) -> {
            int action = e.getAction();

            switch(e.getAction()) {
                case DragEvent.ACTION_DRAG_ENTERED:
                    //Start scrolling
                    scrollDirection = -1;
                    scrollHandler.removeCallbacks(scrollRun);
                    scrollHandler.post(scrollRun);
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    //Scale
                    scrollSpeedModifier = Math.round(maxSpeed * (1f - e.getY() / (float)v.getHeight()));
                    break;
                case DragEvent.ACTION_DRAG_ENDED: case DragEvent.ACTION_DRAG_EXITED:
                    //Reset scroll
                    scrollDirection = 0;
                    scrollSpeedModifier = 1;
                    scrollHandler.removeCallbacks(scrollRun);
                    break;
            }

            if(action == DragEvent.ACTION_DROP || action == DragEvent.ACTION_DRAG_ENTERED || action == DragEvent.ACTION_DRAG_LOCATION) {
                //Find the row which uses the event
                for(int i = 0; i < appTable.getChildCount(); i++) {
                    Rect bounds = new Rect();
                    View sub = appTable.getChildAt(i);
                    sub.getHitRect(bounds);

                    //Determine if the event occurred in the bounds of the TableRow
                    if(bounds.contains(Math.round(e.getX() + deckScroll.getScrollX()), Math.round(e.getY() + deckScroll.getScrollY()))) {
                        sub.dispatchDragEvent(e);
                        break;
                    }
                }
            }

            return true;
        });

        //Handle downwards scrolling
        findViewById(R.id.deckScrollBottom).setOnDragListener((v, e) -> {
            int action = e.getAction();

            switch(action) {
                case DragEvent.ACTION_DRAG_ENTERED:
                    scrollDirection = 1;
                    scrollHandler.removeCallbacks(scrollRun);
                    scrollHandler.post(scrollRun);
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    scrollSpeedModifier = Math.round(maxSpeed * e.getY() / (float)v.getHeight());
                    break;
                case DragEvent.ACTION_DRAG_ENDED: case DragEvent.ACTION_DRAG_EXITED:
                    scrollDirection = 0;
                    scrollSpeedModifier = 1;
                    scrollHandler.removeCallbacks(scrollRun);
                    break;
            }

            if(action == DragEvent.ACTION_DROP || action == DragEvent.ACTION_DRAG_ENTERED || action == DragEvent.ACTION_DRAG_LOCATION) {
                for(int i = 0; i < appTable.getChildCount(); i++) {
                    Rect bounds = new Rect();
                    View sub = appTable.getChildAt(i);
                    sub.getHitRect(bounds);

                    if(bounds.contains(Math.round(e.getX() + deckScroll.getScrollX()), Math.round(v.getY() - v.getHeight() + e.getY() + deckScroll.getScrollY()))) {
                        sub.dispatchDragEvent(e);
                        break;
                    }
                }
            }

            return true;
        });
    }

    /**
     * Toggles fullscreen mode
     * @param value Whether to be fullscreen (toggles edit mode off if true)
     */
    private void toggleFullscreen(boolean value) {
        isFullscreen = value;

        ActionBar actionBar = getSupportActionBar();

        if(value) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            if(isEditing)
                toggleEditMode(false);

            if(actionBar != null)
                actionBar.hide();
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            if(actionBar != null)
                actionBar.show();
        }
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

            //Find where the first tile is to start making elements of the TableRows visible
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

                if(v instanceof SquareView) {
                    //Set gone if there hasn't been a tile yet so it doesn't take up unnecessary room outside of edit mode
                    v.setVisibility(value ? View.VISIBLE : (anyTilesVisibleYet ? View.INVISIBLE : View.GONE));
                } else if(v instanceof TileButton) {
                    //Set the color to editing or normal based on editing status
                    v.getBackground().mutate().setColorFilter(value ? tileEditColor : tileColor, PorterDuff.Mode.SRC_IN);
					v.invalidate();
                }
            }
        }
    }

    /**
     * Updates the view
     */
    private void updateView() {
        final int maxTiles = PersistentValues.getMaxTiles(), columns = PersistentValues.getColumns(), rows = (int)Math.ceil((double)maxTiles / (double)columns);

        //Make sure there are not more rows than possible
        int atCount;
        while((atCount = appTable.getChildCount()) > rows)
            appTable.removeViewAt(atCount - 1);

        loop:for(int i = 0; i < rows; i++) {
            TableRow rowView = (TableRow)appTable.getChildAt(i);

            //Create new row if missing
            if(rowView == null) {
                rowView = new TableRow(this);

                TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                rowView.setLayoutParams(params);

                appTable.addView(rowView);
            }

            //Make sure the elements have the same size
            rowView.setWeightSum(columns);

            //Reposition tiles based on index
            for(int j = 0; j < columns; j++) {
                int index = i * columns + j;

                if(index >= maxTiles)
                    break loop;

                View view = rowView.getChildAt(j);
                ITile tile = RemAL.getTile(index);

                if(tile != null) {
                    if(view instanceof TileButton) {
                        ((TileButton)view).setTile(tile);
                    } else {
                        if(view != null)
                            rowView.removeViewAt(j);

                        rowView.addView(createTileButton(tile), j);
                    }
                } else if(view instanceof TileButton) {
                    rowView.removeViewAt(j);
                    rowView.addView(createPlaceholderView(), j);
                } else if(view == null) {
                    rowView.addView(createPlaceholderView(), j);
                }
            }

            //Make sure there are not more columns in a row than possible
            int count;
            while((count = rowView.getChildCount()) > columns)
                rowView.removeViewAt(count - 1);
        }

        //Make sure there are not more rows than possible
        int count;
        while((count = appTable.getChildCount()) > rows)
            appTable.removeViewAt(count - 1);
    }

    /**
     * Creates a button for a tile
     * @param tile Tile
     */
    private TileButton createTileButton(ITile tile) {
        TileButton button = new TileButton(this);
        button.setTextColor(textColor);
        button.setTile(tile);

        button.getBackground().mutate().setColorFilter(isEditing ? tileEditColor : tileColor, PorterDuff.Mode.SRC_IN);
        button.invalidate();

        TableRow.LayoutParams params = new TableRow.LayoutParams(0, 0, 1f);
        params.setMargins(5, 5, 5, 5);
        params.gravity = Gravity.CENTER;
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

        button.setOnLongClickListener(v -> {
            if(isEditing) {
                String posString = String.valueOf(button.getTile().getPosition());

                ClipData data = new ClipData(posString, new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, new ClipData.Item(posString));
                View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);

                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
                    v.startDrag(data, shadow, v, 0);
                else
                    v.startDragAndDrop(data, shadow, v, 0);
            }

            return true;
        });

        button.setOnDragListener((v, e) -> {
            switch(e.getAction()) {
                case DragEvent.ACTION_DROP:
                    int index = Integer.valueOf(e.getClipData().getItemAt(0).getText().toString());
                    int columns = PersistentValues.getColumns();
                    int staticIndex = appTable.indexOfChild((TableRow)v.getParent()) * columns + ((TableRow)v.getParent()).indexOfChild(v);

                    ITile movedTile = RemAL.getTile(index);
                    ITile staticTile = RemAL.getTile(staticIndex);

                    if(movedTile != null)
                        movedTile.setPosition(staticIndex);

                    if(staticTile != null)
                        staticTile.setPosition(index);

                    RemAL.saveTile(movedTile);
                    RemAL.saveTile(staticTile);

                    RemAL.post(new TileChangedEvent(movedTile));
                    RemAL.post(new TileChangedEvent(staticTile));

                    setHoverView(null);
                    break;
                case DragEvent.ACTION_DRAG_ENTERED: case DragEvent.ACTION_DRAG_LOCATION:
                    setHoverView(v);
                    break;
                case DragEvent.ACTION_DRAG_ENDED: case DragEvent.ACTION_DRAG_EXITED:
					v.getBackground().mutate().setColorFilter(tileEditColor, PorterDuff.Mode.SRC_IN);
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
        view.setBackground(getResources().getDrawable(R.drawable.ic_baseline_border_clear_24px));
        view.setVisibility(isEditing ? View.VISIBLE : View.INVISIBLE);

        view.getBackground().mutate().setColorFilter(tilePlaceholderColor, PorterDuff.Mode.SRC_IN);
        view.invalidate();

        TableRow.LayoutParams params = new TableRow.LayoutParams(0, 0, 1f);
        params.setMargins(5, 5, 5, 5);
        params.gravity = Gravity.CENTER;
        view.setLayoutParams(params);

        view.setOnClickListener(v -> {
            int columns = PersistentValues.getColumns();

            TableRow rowView = (TableRow)v.getParent();
            int relativeIndex = rowView.indexOfChild(v);
            int index = appTable.indexOfChild(rowView) * columns + relativeIndex;

            IRemoteDevice[] devices = RemAL.getDevices();
            ITile tile = RemAL.createTile(devices.length > 0 ? devices[0] : null, index);
            RemAL.saveTile(tile);

            rowView.removeViewAt(relativeIndex);
            rowView.addView(createTileButton(tile), relativeIndex);

            TileLevelTracker.notify(index, true);

            Intent intent = new Intent(this, TileOptions.class);
            intent.putExtra(TileOptions.TO_EXTRA, tile);

            startActivity(intent);
        });

        view.setOnDragListener((v, e) -> {
            switch(e.getAction()) {
                case DragEvent.ACTION_DROP:
                    int index = Integer.valueOf(e.getClipData().getItemAt(0).getText().toString());
                    int columns = PersistentValues.getColumns();

                    TableRow originalRow = (TableRow)appTable.getChildAt(index / columns);
                    if(originalRow != null) {
                        int column = index % columns;
                        originalRow.removeViewAt(column);
                        originalRow.addView(createPlaceholderView(), column);
                    }

                    TableRow rowView = (TableRow)v.getParent();
                    if(rowView != null) {
                        int relativeIndex = rowView.indexOfChild(v);
                        int newIndex = appTable.indexOfChild(rowView) * columns + relativeIndex;

                        ITile movedTile = RemAL.getTile(index);
                        if(movedTile != null) {
                            RemAL.deleteTile(movedTile);
                            movedTile.setPosition(newIndex);
                            RemAL.saveTile(movedTile);
                        }

                        rowView.removeViewAt(relativeIndex);
                        rowView.addView(createTileButton(movedTile), relativeIndex);

                        TileLevelTracker.notify(index, newIndex);
                    }

                    setHoverView(null);
                    break;
                case DragEvent.ACTION_DRAG_ENTERED: case DragEvent.ACTION_DRAG_LOCATION:
                    setHoverView(v);
                    break;
                case DragEvent.ACTION_DRAG_ENDED: case DragEvent.ACTION_DRAG_EXITED:
					v.getBackground().mutate().setColorFilter(tilePlaceholderColor, PorterDuff.Mode.SRC_IN);
					v.invalidate();
                    break;
            }

            return true;
        });

        return view;
    }

    /**
     * Sets the view being hovered over
     * @param v View being hovered over
     */
    private void setHoverView(View v) {
        if(hoverView != null) {
            hoverView.getBackground().mutate().setColorFilter((hoverView instanceof TileButton) ? tileEditColor : tilePlaceholderColor, PorterDuff.Mode.SRC_IN);
            hoverView.invalidate();
        }

        if((hoverView = v) != null) {
            v.getBackground().mutate().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_IN);
            v.invalidate();
        }
    }

    /**
     * Resets the colors of the deck to their original
     */
    private void resetDeckColors() {
        PersistentValues.resetDeckBackgroundColor(this);
        PersistentValues.resetDeckTileColor(this);
        PersistentValues.resetDeckTextColor(this);

        updateDeckColors();
    }

    /**
     * Updates the colors currently viewed on the deck
     */
    private void updateDeckColors() {
        findViewById(R.id.deckScroll).setBackgroundColor(Color.parseColor(PersistentValues.getDeckBackgroundColor(this)));

        textColor = Color.parseColor(PersistentValues.getDeckTextColor(this));
        tileColor = Color.parseColor(PersistentValues.getDeckTileColor(this));

        final int colorDelta = 50, r = Color.red(tileColor), g = Color.green(tileColor), b = Color.blue(tileColor);

        tileEditColor = Color.argb(
                50,
                Math.min(255, r + colorDelta),
                Math.min(255, g + colorDelta),
                Math.min(255, b + colorDelta)
        );

        tilePlaceholderColor = Color.rgb(
                Math.min(255, r + colorDelta),
                Math.min(255, g + colorDelta),
                Math.min(255, b + colorDelta)
        );

        for(int i = appTable.getChildCount() - 1; i >= 0; i--) {
            TableRow row = (TableRow)appTable.getChildAt(i);
            int count = row.getChildCount();

            for(int j = 0; j < count; j++) {
                View v = row.getChildAt(j);

                boolean isButton = v instanceof TileButton;
                if(isButton)
                    ((TileButton)v).setTextColor(textColor);

                v.getBackground().mutate().setColorFilter(isButton ? tileColor : tilePlaceholderColor, PorterDuff.Mode.SRC_IN);
                v.invalidate();
            }
        }
    }
}