package exn.database.remal.ui;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.ViewGroup;

import exn.database.remal.Deck;
import exn.database.remal.config.PersistenceUtils;
import exn.database.remal.core.IRemalEventListener;
import exn.database.remal.core.RemAL;
import exn.database.remal.core.RemalEvent;
import exn.database.remal.deck.ITile;
import exn.database.remal.events.TileChangedEvent;
import exn.database.remal.events.TileDestroyedEvent;

public class TileButton extends AppCompatButton implements IRemalEventListener {
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

        setMeasuredDimension(side, side);
    }

    @Override
    public void onRemalEvent(RemalEvent event) {
        if(event instanceof TileChangedEvent) {
            TileChangedEvent e = (TileChangedEvent)event;

            if(e.tile.getIndex() == tile.getIndex())
                tile = e.tile;
        }
    }
}