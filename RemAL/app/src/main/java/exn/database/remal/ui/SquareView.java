package exn.database.remal.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * A view with the same width/height based on which is the greater value
 */
public class SquareView extends View {
    public SquareView(Context context) {
        super(context);
    }

    public SquareView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
