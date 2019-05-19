package draw.space;

/**
 * Created by Tommy on 2/5/18.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.v4.view.GravityCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;

public class DrawingView extends View {
    private Context context;
    private static final String TAG = "DrawingView";
    private Bitmap oldBitmap;
    private Bitmap savedBitmap;
    private Bitmap cacheBitmap;
    private Canvas cacheCanvas;
    private Paint BitmapPaint;
    private Paint paint;
    private Path path;
    private int height;
    private int width;
    private ArrayList<DrawnItem> mDrawnItem = new ArrayList<DrawnItem>();
    private ArrayList<DrawnItem> undoneDrawnItem = new ArrayList<DrawnItem>();
    private DrawnItem mCurrentItem;
    private int mStyle;
    private int mWidth;
    private int mColor;
    private float mCurX = 0f;
    private float mCurY = 0f;
    private static float MIN_ZOOM = 0.8f;
    private static float MAX_ZOOM = 10f;
    private float scaleFactor = 0.8f;
    private float previousScaleFactor = 0.8f;
    private ScaleGestureDetector detector;
    //These two variables keep track of the X and Y coordinate of the finger when it first
    //touches the screen
    private float startX = 0f;
    private float startY = 0f;

    private float newX = 0f;
    private float newY = 0f;
    //These two variables keep track of the amount we need to translate the canvas along the X
    //and the Y coordinate
    private float translateX = 20f;
    private float translateY = 20f;
    //These two variables keep track of the amount we translated the X and Y coordinates, the last time we
    //panned.
    private float previousTranslateX = 0f;
    private float previousTranslateY = 0f;
    private boolean isOpen;

    // Used for set first translate to a quarter of screen
    private float displayWidth;
    private float displayHeight;
    Context ctx;
    private float mZoomCenterX = -1.0f;
    private float mZoomCenterY = -1.0f;
    private float prevZoomCenterX = -1.0f;
    private float prevZoomCenterY = -1.0f;
    private Rect mCanvasClipBounds;
    private int mActivePointerId;
    private MainActivity main;
    private CustomDrawerLayout drawer;
    private boolean isCurrentlyOpen = true;
    private int previousCount = 2;
    private boolean track = false;
    private boolean zoom = false;
    private boolean drag = true;
    private boolean enableModeSelection = true;
    double distance;
    private boolean zoomClick;

    /*
    public DrawingView(MainActivity _context){
        main = _context;
    }
    */

    /** get the height and width */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        height = h; //emulator is 1584. at least height of the canvas
        width = w; //emulator is 1080. which produces approximately 3MB picture
        Log.d(TAG,"height: " + height);
        Log.d(TAG,"width: " + width);
        init();
    }

    private void init(){
        drawer = ((MainActivity)getContext()).findViewById(R.id.drawer_layout);
        //drawer = main.getDrawer();
        //main = new MainActivity();
        //Log.d(TAG,"Is Drawer open" + main.isDrawerOpen());
        BitmapPaint = new Paint();
        BitmapPaint.setColor(Color.WHITE);
        BitmapPaint.setStyle(Paint.Style.FILL);
        BitmapPaint.setFilterBitmap(true);
        BitmapPaint.setDither(false);
        BitmapPaint.setAntiAlias(true);
        if(savedBitmap == null){
            cacheBitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
            cacheCanvas = new Canvas(cacheBitmap);
            cacheCanvas.drawColor(Color.WHITE);
        }
        mCurrentItem = new DrawnItem();
        paint = new Paint();
        path = new Path();
        mColor = Color.BLACK;
        mStyle = STROKE;
        mWidth = 20;
        initPaint(paint,mColor);
        mCanvasClipBounds = new Rect();

    }


    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;

        //WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        //Display display = wm.getDefaultDisplay();

        //DisplayMetrics metrics = new DisplayMetrics();

        //display.getMetrics(metrics);

        //displayWidth = metrics.widthPixels;
        //displayHeight = metrics.heightPixels;

        //translateX = displayWidth/4;
        //translateY = displayHeight/4;

        //previousTranslateX = displayWidth/4;
        //previousTranslateY = displayHeight/4;

        detector = new ScaleGestureDetector(context, new  ScaleListener());
        detector.setQuickScaleEnabled(false);

        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.d(TAG, "pointer ID" + event.getPointerId(0));
        mActivePointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(0);
        detector.onTouchEvent(event);

        if(event.getPointerCount() ==2){
            track = true;
        }
        if (event.getPointerCount() == 1) {
            float X = (event.getX())/scaleFactor + mCanvasClipBounds.left; //messes up when zooming in, but it's correct in the location when zoomed out
            float Y = (event.getY())/scaleFactor + mCanvasClipBounds.top;
            //Log.d(TAG, "X " + event.getX());
            //Log.d(TAG, "X adjusted" + X);
            //Log.d(TAG,"Scale Factor" + scaleFactor);
            //Log.d(TAG, "mZoomCenterX" + mZoomCenterX);
            //Log.d(TAG,"mCanvasClipbounds.left" + mCanvasClipBounds.left);


            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    undoneDrawnItem.clear();
                    path.moveTo(X, Y);
                    mCurX = X;
                    mCurY = Y;
                    // There is no end point yet, so don't waste cycles invalidating.
                    //Log.d(TAG, "action_down");

                    return true;

                case MotionEvent.ACTION_MOVE:
                    if(track == false) {
                        if (isOpen) {
                            if (event.getX() <= 100) {
                                if (isCurrentlyOpen == true) {
                                    //touch_tolerance = 30;
                                    drawer.closeDrawer(GravityCompat.START);
                                    isCurrentlyOpen = false;
                                }
                            }
                        }
                        path.quadTo(mCurX, mCurY, (X + mCurX) / 2, (Y + mCurY) / 2);

                        mCurX = X;
                        mCurY = Y;
                    }
                    //Log.d(TAG, "action_move");
                    break;
                case MotionEvent.ACTION_UP:
                    if(track == true){
                        mCurrentItem.setPaint(paint);
                        mCurrentItem.setPath(path);
                        mDrawnItem.add(mCurrentItem); //list
                        cacheCanvas.drawPath(path, paint);
                        mCurrentItem = new DrawnItem();
                        path = new Path();
                        paint = new Paint();
                        initPaint(paint, mColor);
                        //Log.d(TAG,"track is true");
                        onClickUndo();
                    }
                    if(track == false) {
                        // No more fingers on screen

                        path.quadTo(mCurX, mCurY, X, Y);
                        mCurrentItem.setPaint(paint);
                        mCurrentItem.setPath(path);
                        mDrawnItem.add(mCurrentItem); //list
                        cacheCanvas.drawPath(path, paint);
                        mCurrentItem = new DrawnItem();
                        path = new Path();
                        paint = new Paint();
                        initPaint(paint, mColor);

                        if (isOpen) {
                            if (isCurrentlyOpen == false) {
                                drawer.openDrawer(GravityCompat.START);
                                isCurrentlyOpen = true;
                            }
                        }
                    }
                    track = false;
                    //Log.d(TAG, "action_up");
                    break;
                default:
                    //Log.d(TAG, "Ignored touch event: " + event.toString());
                    return false;
            }
        }


        if (event.getPointerCount() == 2) {
            //float X = (event.getX()) / scaleFactor + mCanvasClipBounds.left; //messes up when zooming in, but it's correct in the location when zoomed out
            //float Y = (event.getY()) / scaleFactor + mCanvasClipBounds.top;
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    // First finger is on screen
                    //We assign the current X and Y coordinate of the finger to startX and startY minus the previously translated
                    //amount for each coordinates This works even when we are translating the first time because the initial
                    //values for these two variables is zero.
                    return true;
                case MotionEvent.ACTION_POINTER_DOWN:
                    startX = detector.getFocusX() - previousTranslateX;
                    startY = detector.getFocusY() - previousTranslateY;
                    newX = detector.getFocusX();
                    newY = detector.getFocusY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    // first finger is moving on screen
                    // update translation value to apply on Path


                    translateX = detector.getFocusX() - startX;
                    //Log.d(TAG,"startX " + startX);

                    translateY = detector.getFocusY() - startY;
                    //Log.d(TAG,"translateX " + translateX);

                    distance = Math.sqrt(Math.pow(detector.getFocusX() - (newX),2) + Math.pow(detector.getFocusY() - (newY),2));
                    //Log.d(TAG, "distance " + distance);
                    //Log.d(TAG,"drag " + drag);
//                    if(enableModeSelection){
//                        if(distance > 100){
//                            Log.d(TAG, "drag");
//                            drag = true;
//                            //zoom = false;
//                        }else{
//                            Log.d(TAG, "zoom");
//                            zoom = true;
//                            //
//                            // drag = false;
//                        }
//                    }

                    break;
                case MotionEvent.ACTION_POINTER_UP:

                    // All fingers went up, so let's save the value of translateX and translateY into previousTranslateX and
                    //previousTranslate
                    if(drag == false) {
                        translateX = previousTranslateX;
                        translateY = previousTranslateY;
                    }
                    drag = true;
                    //Log.d(TAG,"action up");
                    zoom = false;
                    enableModeSelection = true;


                    break;
                default:
                    //Log.d(TAG, "Ignored touch event: " + event.toString());
                    return false;
            }
        }



        invalidate();
        return true;



    }

    @Override
    protected void onDraw(Canvas canvas) {

//        //We need to divide by the scale factor here, otherwise we end up with excessive panning based on our zoom level
//        //because the translation amount also gets scaled according to how much we've zoomed into the canvas.
        canvas.save();
        if(zoom == true){
            previousScaleFactor = scaleFactor;
            prevZoomCenterX = mZoomCenterX;
            prevZoomCenterY = mZoomCenterY;
            canvas.scale(scaleFactor,scaleFactor,(mCanvasClipBounds.left + mCanvasClipBounds.right)/2,(mCanvasClipBounds.top + mCanvasClipBounds.bottom)/2);
            // counts this as a real scale and remembers
        }else{
            scaleFactor = previousScaleFactor; // effectively erases any scale that occurs
            mZoomCenterX = prevZoomCenterX;
            mZoomCenterY = prevZoomCenterY;
            canvas.scale(scaleFactor,scaleFactor,(mCanvasClipBounds.left + mCanvasClipBounds.right)/2,(mCanvasClipBounds.top + mCanvasClipBounds.bottom)/2);
        }
            //enableModeSelection = false;

        if(drag) {
            previousTranslateX = translateX;
            previousTranslateY = translateY;
        }else{
            translateX = previousTranslateX;
            translateY = previousTranslateY;
        }
        //Log.d(TAG,"previous scalefactor " + previousScaleFactor);
        Log.d(TAG, "mCanvasClipBoundns before" + mCanvasClipBounds.left);
        canvas.translate(translateX / scaleFactor/scaleFactor, translateY / scaleFactor/scaleFactor);
            //enableModeSelection = false;

        //Log.d(TAG,"translateX " + translateX/ scaleFactor + " translateY " + translateY/ scaleFactor);
        canvas.getClipBounds(mCanvasClipBounds);
        Log.d(TAG, "mCanvasClipBoundns after" + mCanvasClipBounds.left);

        //Log.d(TAG,"mZoomCenterX" + mZoomCenterX);
        //Log.d(TAG,"clip left " + mCanvasClipBounds.left + " clip right " + mCanvasClipBounds.right);

        if(zoomClick){
            zoomClick = false;
            zoom = false;
        }

        if (savedBitmap != null){
            canvas.drawBitmap(savedBitmap, 0, 0, BitmapPaint);

        }else {
            canvas.drawBitmap(cacheBitmap, 0, 0, BitmapPaint);

        }
        /*
        for (int i = 0; i < mDrawnItem.size(); i++){
            canvas.drawPath(mDrawnItem.get(i).getPath(), mDrawnItem.get(i).getPaint());
            Log.d(TAG, "path" + mDrawnItem.get(i).getPath());
        }
        */
        if(path!=null) {

            canvas.drawPath(path, paint);
        }
        canvas.restore();
        super.onDraw(canvas);

    }

    public void onClickUndo () {
        //Log.d(TAG,"mDrawItem.size" + mDrawnItem.size());

        if (mDrawnItem.size()>0) {
            undoneDrawnItem.add(mDrawnItem.remove(mDrawnItem.size()-1));
            if (savedBitmap != null){
                savedBitmap = oldBitmap.copy(Bitmap.Config.RGB_565, true);
                cacheCanvas = new Canvas(savedBitmap);
            }else{
                cacheBitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
                cacheCanvas = new Canvas(cacheBitmap);
                cacheCanvas.drawColor(Color.WHITE);
            }
            for (int i = 0; i < mDrawnItem.size(); i++){
                cacheCanvas.drawPath(mDrawnItem.get(i).getPath(), mDrawnItem.get(i).getPaint());
                //Log.d(TAG, "path" + mDrawnItem.get(i).getPath());
            }
            invalidate();
        }
    }

    public void onClickRedo (){
        if (undoneDrawnItem.size()>0) {
            mDrawnItem.add(undoneDrawnItem.remove(undoneDrawnItem.size()-1));
            if (savedBitmap != null){
                savedBitmap = oldBitmap.copy(Bitmap.Config.RGB_565, true);
                cacheCanvas = new Canvas(savedBitmap);
            }else{
                cacheBitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
                cacheCanvas = new Canvas(cacheBitmap);
                cacheCanvas.drawColor(Color.WHITE);
            }
            for (int i = 0; i < mDrawnItem.size(); i++){
                cacheCanvas.drawPath(mDrawnItem.get(i).getPath(), mDrawnItem.get(i).getPaint());
                //Log.d(TAG, "path" + mDrawnItem.get(i).getPath());
            }
            invalidate();
        }
    }

    public void setColor(int color){
        paint.setColor(color);
        mColor = color;
    }

    public void setPaintWidth(int width){
        paint.setStrokeWidth(width);
        mWidth = width;
    }

    public static final int STROKE = 1;
    public static final int FILL = 2;

    public void setStyle(int style){
        switch(style){
            case STROKE:
                paint.setStyle(Paint.Style.STROKE);
                mStyle = STROKE;
                break;
            case FILL:
                paint.setStyle(Paint.Style.FILL);
                mStyle = FILL;
                break;
        }
    }

    public void zoomIn(){
        zoom = true;
        scaleFactor = 5;
        zoomClick = true;

        invalidate();
        //zoom = false;
    }

    public void zoomOut(){
        zoom = true;
        scaleFactor = 0.8f;
        zoomClick = true;
        translateX = 20;
        translateY = 20;
        invalidate();
        //zoom = false;
    }

    /** clear your drawing*/
    public void clearScreen(){
        undoneDrawnItem.clear();
        mDrawnItem.clear();
        savedBitmap = null;
        cacheBitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
        cacheCanvas = new Canvas(cacheBitmap);
        cacheCanvas.drawColor(Color.WHITE);
        /* //permanent deletion if desired
        if(canvas != null){
            setDrawingCacheEnabled(false);
            onSizeChanged(width, height, width, height);
            invalidate();
            setDrawingCacheEnabled(true);
         }
         */

//        while(mDrawnItem.size()>0) {
//            undoneDrawnItem.add(mDrawnItem.remove(mDrawnItem.size() - 1));
//
//        }
//
//        if (savedBitmap != null){
//            cacheCanvas = new Canvas(savedBitmap);
//        }else{
//            cacheBitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
//            cacheCanvas = new Canvas(cacheBitmap);
//            cacheCanvas.drawColor(Color.WHITE);
//        }
//        for (int i = 0; i < mDrawnItem.size(); i++){
//            cacheCanvas.drawPath(mDrawnItem.get(i).getPath(), mDrawnItem.get(i).getPaint());
//            Log.d(TAG, "path" + mDrawnItem.get(i).getPath());
//        }
        invalidate();
    }

    private void initPaint(Paint paint, int color){
        paint.setColor(color);
        paint.setDither(false);
        paint.setStrokeWidth(mWidth);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        //final BlurMaskFilter filter = new BlurMaskFilter(100, BlurMaskFilter.Blur.NORMAL);
        //paint.setMaskFilter(filter);
        switch(mStyle) {
            case STROKE:
                paint.setStyle(Paint.Style.STROKE);
                break;
            case FILL:
                paint.setStyle(Paint.Style.FILL);
                break;
        }
    }

    public void setSavedBitmap(Bitmap bitmap){
        if (bitmap != null){
            oldBitmap = Bitmap.createBitmap(bitmap);
            savedBitmap = oldBitmap.copy(Bitmap.Config.RGB_565, true);
            cacheCanvas = new Canvas(savedBitmap);

        }
    }

    public Bitmap getBitmap() {
        if (savedBitmap != null){
            return savedBitmap;
        }else {
            return cacheBitmap;
        }
    }

    public void setDrawerOpen(boolean question){
        isOpen = question;
    }

    class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            //Log.d(TAG,"scale begin");
//            if(enableModeSelection){
//                if(distance > 100){
//                    Log.d(TAG, "drag");
//                    enableModeSelection = false; //this may not be necessary
//
//                    //zoom = false;
//                }else{
//                    Log.d(TAG, "zoom");
//                    zoom = true;
//                    drag = false;
//                    enableModeSelection = false;
//                }
//            }
            if(distance < 30){
                //Log.d(TAG, "zoom");
                zoom = true;
                drag = false;
            }

            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
            mZoomCenterX = detector.getFocusX();
            mZoomCenterY = detector.getFocusY();
            invalidate();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector){
        }

    }

}