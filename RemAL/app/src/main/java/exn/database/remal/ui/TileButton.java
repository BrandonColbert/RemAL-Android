package exn.database.remal.ui;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;

import exn.database.remal.core.IRemalEventListener;
import exn.database.remal.core.RemAL;
import exn.database.remal.core.RemalEvent;
import exn.database.remal.deck.ITile;
import exn.database.remal.events.TileChangedEvent;

/**
 * A button with the same width/height based on which value is greater that also stores a tile
 */
public class TileButton extends AppCompatButton {
    private ITile tile;

    public TileButton(Context context) {
        super(context);
    }

    public TileButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TileButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTile(ITile tile) {
        this.tile = tile;
        setText(tile.getName());
    }

    public ITile getTile() {
        return tile;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int side = width > height ? width : height;

        //Set both dimensions to same value
        setMeasuredDimension(side, side);
    }
}