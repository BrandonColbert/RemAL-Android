package exn.database.remal;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import exn.database.remal.core.RemAL;
import exn.database.remal.deck.ITile;
import exn.database.remal.deck.TileLevelTracker;

public class TileOptions extends AppCompatActivity {
    public static final String TO_EXTRA = "exn.database.remal.devices.TILE_OPTIONS_EXTRA";

    private ITile tile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tile_options);

        tile = getIntent().getParcelableExtra(TO_EXTRA);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        if(bar != null)
            bar.setDisplayHomeAsUpEnabled(true);

        TileOptionsFragment frag = new TileOptionsFragment();
        frag.setTile(tile);
        getSupportFragmentManager().beginTransaction().replace(R.id.tile_options_content, frag).commit();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_tile_options, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.delete_tile:
                RemAL.deleteTile(tile);
                TileLevelTracker.notify(tile.getPosition(), false);
                finish();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}