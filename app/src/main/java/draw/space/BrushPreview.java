package draw.space;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by Tommy on 3/11/18.
 */

public class BrushPreview extends View{
    private static final String TAG = "BrushPreview";
    float radius;
    Paint paint;
    private float mWidth = 20;
    private int mColor = Color.BLACK;

    public BrushPreview(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPaintWidth(int width){
        mWidth = width;
        radius = mWidth/2;
    }

    public void setColor(int color){
        if (paint == null){
            paint = new Paint();
        }
        paint.setColor(color);
        mColor = color;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        paint = new Paint();
        paint.setColor(mColor);
        radius = mWidth/2;
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        Log.d(TAG,"onSizeChanged");
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        if((w==0) || (h==0)){
            return;
        }

        float x = (float)w/2.0f;
        float y = (float)h/2.0f;

        canvas.drawCircle(x,y,radius,paint);
        Log.d(TAG,"radius: " + radius);
    }


}


