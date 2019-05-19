package draw.space;

/**
 * Created by Tommy on 2/6/18.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class SaveViewUtil {
    private final static String TAG = "SaveViewUtil";
    /** Save picture to file */
    public static boolean saveScreen(Bitmap bitmap, Context context){

//        view.setDrawingCacheEnabled(true);
//        view.buildDrawingCache();
//        Bitmap bitmap = view.getDrawingCache();
/*
        ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("noDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
*/
//inserted by me
        try {
            String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
            String pictureFile = "IMG_" + timeStamp;

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, pictureFile);
            values.put(MediaStore.Images.Media.DISPLAY_NAME, pictureFile);
            values.put(MediaStore.Images.Media.MIME_TYPE, "png");
            values.put(MediaStore.Images.Media.WIDTH, bitmap.getWidth());
            values.put(MediaStore.Images.Media.HEIGHT, bitmap.getHeight());
            values.put(MediaStore.Images.Media.SIZE, bitmap.getByteCount());



            // Add the date meta data to ensure the image is added at the front of the gallery
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            Uri url = null;
            String stringUrl = null;

            url = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (bitmap != null) {
                OutputStream imageOut = context.getContentResolver().openOutputStream(url);
                try {
                    bitmap.compress(CompressFormat.PNG, 100, imageOut);
                } finally {
                    imageOut.close();
                }
            } else {
                context.getContentResolver().delete(url, null, null);
                url = null;
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }finally{

            //view.setDrawingCacheEnabled(false);
            //bitmap = null;
        }
    }


    public static void saveToInternalStorage(Bitmap bitmap, Context context){
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        File directory = cw.getDir("noDir", Context.MODE_PRIVATE);
        if(!directory.exists()){
            directory.mkdir();
        }
        File mypath=new File(directory,"profile.png");
        Log.d(TAG, "mpath " + mypath);

        FileOutputStream fos = null;

//        view.setDrawingCacheEnabled(true);
//        view.buildDrawingCache();
//        Bitmap bitmap = view.getDrawingCache();

        try {
            fos = new FileOutputStream(mypath);
            bitmap.compress(CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            //view.setDrawingCacheEnabled(false);
            bitmap = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Bitmap loadImageFromStorage(Context context) {
        try {
            ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
            File directory = cw.getDir("noDir", Context.MODE_PRIVATE);

            File f=new File(directory, "profile.png");
            Log.d(TAG, "f " + f);

            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            Log.d(TAG, "file found");
            return b;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            Log.d(TAG, "file not found");
            return null;
        }
    }

//    private static final File rootDir = new File(Environment.getExternalStorageDirectory()+File.separator+"huaban/");
//
//    /** Save picture to file */
//    public static boolean saveScreen(View view, Context context){
//        //determine if SDCARD is available
//        if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
//            return false;
//        }
//        if(!rootDir.exists()){
//            rootDir.mkdir();
//        }
//        view.setDrawingCacheEnabled(true);
//        view.buildDrawingCache();
//        Bitmap bitmap = view.getDrawingCache();
//
//
////inserted by me
//        try {
//            String timeStamp = new SimpleDateFormat("yyyy_MM_dd").format(new Date());
//            File pictureFile = new File(rootDir,"IMG_" + timeStamp + ".jpg");
//            FileOutputStream out = new FileOutputStream(pictureFile);
//            bitmap.compress(CompressFormat.JPEG, 100, out);
//            out.flush();
//            out.close();
//
//
//            MediaStore.Images.Media.insertImage(context.getContentResolver(), pictureFile.getAbsolutePath(), timeStamp, null);
////          send a broadcast message to refresh the gallery
//            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//            Uri uri = Uri.fromFile(pictureFile);intent.setData(uri);
//            context.sendBroadcast(intent);
////end
//
//            return true;
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            return false;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }finally{
//
//            view.setDrawingCacheEnabled(false);
//            bitmap = null;
//        }
//    }
}