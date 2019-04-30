package exn.database.remal.ui;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class TileButton extends AppCompatButton {
    private int scaleX, scaleY;

    public TileButton(Context context) {
        super(context);
        scaleX = scaleY = 1;
    }

    public TileButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        scaleX = scaleY = 1;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int side = width > height ? width : height;

        super.onMeasure(side * scaleX, side * scaleY);
    }

    public void setScaleX(int value) {
        this.scaleX = value;
    }

    public void setScaleY(int value) {
        this.scaleY = value;
    }
}