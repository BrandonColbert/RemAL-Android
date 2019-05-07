package exn.database.remal.ui;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import exn.database.remal.core.IRemalEventListener;
import exn.database.remal.core.RemAL;
import exn.database.remal.core.RemalEvent;
import exn.database.remal.deck.ITile;
import exn.database.remal.events.TileChangedEvent;

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
        setText(tile.getName());
    }

    public ITile getTile() {
        return tile;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        RemAL.register(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        RemAL.unregister(this);
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

            if(e.tile.getPosition() == tile.getPosition())
                setTile(e.tile);
        }
    }
}