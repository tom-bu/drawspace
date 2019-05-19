package draw.space;
import android.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.BottomNavigationView;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.AdRequest;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;

import java.io.IOException;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private BrushPreview brushPreview;
    private DrawingView hbView;
    private AlertDialog dialog;
    private AlertDialog colorDialogue;
    private View dialogView;
    private View colorView;
    private TextView shouWidth;
    private SeekBar widthSb;
    private int paintWidth;
    private InterstitialAd mInterstitialAd;
    private Handler mhandler;
//    private Location mLocation;
//    private LocationManager locationManager;
//    private LocationListener locationListener;
//    String locationProvider = LocationManager.GPS_PROVIDER;
    private AdView mAdView;
    private FirebaseAnalytics mFirebaseAnalytics;
    private CustomDrawerLayout drawer;
    private ImageButton closeD;
    private ImageButton openD;
    private NavigationView navigationView;
    private Menu menu;
    private static final int WRITE_EXTERNAL_REQUEST_CODE = 200;


    private final Runnable mAd = new Runnable() {
        @Override
        public void run() {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                Log.d(TAG, "The interstitial wasn't loaded yet.");
            }
        }
    };


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            item.setCheckable(false);
            switch (item.getItemId()) {
                case R.id.navigation_undo:
                    //item.setCheckable(false);
                    hbView.onClickUndo();
                    Log.d(TAG, "undo");
                    mFirebaseAnalytics.logEvent("undo", null);
                    return true;
                case R.id.navigation_redo:
                    //item.setCheckable(false);
                    hbView.onClickRedo();
                    Log.d(TAG,"redo");
                    mFirebaseAnalytics.logEvent("redo", null);
                    return true;
            }
            return false;
        }

    };

    private NavigationView.OnNavigationItemSelectedListener dOnNavigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener(){

        @SuppressWarnings("StatementWithEmptyBody")
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            // Handle navigation view item clicks here.
            int id = item.getItemId();

            if (id == R.id.color){
                colorDialogue.show();
                mFirebaseAnalytics.logEvent("color", null);
            } else if (id == R.id.size) {
                dialog.show();
                mFirebaseAnalytics.logEvent("size", null);
                //} //else if (id == R.id.stroke) {
                //  hbView.setStyle(DrawingView.STROKE);
                //  mFirebaseAnalytics.logEvent("stroke", null);
                //} else if (id == R.id.fill) {
                //    hbView.setStyle(DrawingView.FILL);
                //    mFirebaseAnalytics.logEvent("fill", null);
            }else if (id == R.id.zoom_in) {
                hbView.zoomIn();
                item.setVisible(false);
                MenuItem out = menu.findItem(R.id.zoom_ou);
                out.setVisible(true);
            }else if (id == R.id.zoom_ou) {
                hbView.zoomOut();
                item.setVisible(false);
                MenuItem in = menu.findItem(R.id.zoom_in);
                in.setVisible(true);
            }else if (id == R.id.upload){
                openGallery();
            } else if (id == R.id.clear) {
                hbView.clearScreen();
                mFirebaseAnalytics.logEvent("clear", null);
            } else if (id == R.id.save) {
                mFirebaseAnalytics.logEvent("save", null);
                if(SaveViewUtil.saveScreen(hbView.getBitmap(), getApplicationContext())){
                    Toast.makeText(getApplicationContext(), "Drawing saved to photo gallery!", Toast.LENGTH_LONG).show();
                    mhandler.postDelayed(mAd,0);
                }else{
                    Toast.makeText(getApplicationContext(), "Failed to save. Please check your SD card", Toast.LENGTH_SHORT).show();
                    mhandler.postDelayed(mAd,0);

                }
            } else if (id == R.id.exit) {
                finish();

            }

            //drawer.closeDrawer(GravityCompat.START);
            //drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED,navigationView);
            //Log.d(TAG,"lock mode");
            //int drawerLockMode = drawer.getDrawerLockMode(GravityCompat.START);
            //Log.d(TAG,"lock mode on menu: " + drawerLockMode);
            return true;
        }
    };
    private static final int PICK_IMAGE = 100;
    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    Uri imageUri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode, data);
        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            imageUri = data.getData();
            //imageview.setImageURI(imageUri);
            try{
                hbView.setSavedBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri));

            }catch(IOException e){
                e.printStackTrace();
            }

        }
    }


    private void initView(){
        //mLocation = new Location("dummyprovider");
        dialogView = getLayoutInflater().inflate(R.layout.dialog_width_set, null);
        brushPreview = (BrushPreview)dialogView.findViewById(R.id.brushPreview);
        shouWidth = (TextView) dialogView.findViewById(R.id.textView1);
        widthSb = (SeekBar) dialogView.findViewById(R.id.seekBar1);
        widthSb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                shouWidth.setText("Current Width："+(progress+1));
                paintWidth = progress+1;
                brushPreview.setPaintWidth(paintWidth);
                Log.d(TAG,"paintWidth: " + paintWidth);
                brushPreview.invalidate();

            }
        });

        colorView = getLayoutInflater().inflate(R.layout.colorwheel, null);
        final ColorPicker picker = (ColorPicker) colorView.findViewById(R.id.picker);
        SVBar svBar = (SVBar) colorView.findViewById(R.id.svbar);
        OpacityBar opacityBar = (OpacityBar) colorView.findViewById(R.id.opacitybar);
        picker.addSVBar(svBar);
        picker.addOpacityBar(opacityBar);
        picker.setOldCenterColor(Color.BLACK);
        //To set the old selected color u can do it like this

        picker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
            }
        });


        colorDialogue = new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_info).setTitle("Set the color, brightness, and opacity").
                setView(colorView).setPositiveButton("Confirm", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                hbView.setColor(picker.getColor());
                brushPreview.setColor(picker.getColor());
                picker.setOldCenterColor(picker.getColor());
                //mFirebaseAnalytics.logEvent("hexcolor: " + picker.getColor(), null);

                Log.d(TAG,"initial color"+ picker.getColor());
            }
        }).setNegativeButton("Cancel", null).create();

        hbView = (DrawingView)findViewById(R.id.DrawingView1);
        hbView.setDrawerOpen(true);
        dialog = new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_info).setTitle("Set the size of your pen").
                setView(dialogView).setPositiveButton("Confirm", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                hbView.setPaintWidth(paintWidth);
                //mFirebaseAnalytics.logEvent("width: " + paintWidth, null);

            }
        }).setNegativeButton("Cancel", null).create();

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_EXTERNAL_REQUEST_CODE);
            }
        }

//        // Acquire a reference to the system Location Manager
//        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        // Register the listener with the Location Manager to receive location updates
//        // Define a listener that responds to location updates
//        locationListener = new LocationListener() {
//            public void onLocationChanged(Location location) {
//                // Called when a new location is found by the network location provider.
//                mLocation.set(location);
//                Log.d(TAG,"onLocationChanged");
//            }
//
//            public void onStatusChanged(String provider, int status, Bundle extras) {
//                Log.d(TAG,"onStatusChanged");
//            }
//
//            public void onProviderEnabled(String provider) {
//                Log.d(TAG,"onProviderEnabled");
//            }
//
//            public void onProviderDisabled(String provider) {
//                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                startActivity(intent);
//                Log.d(TAG,"onProviderDisabled");
//            }
//        };
//        if (Build.VERSION.SDK_INT >= 23) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{
//                        Manifest.permission.ACCESS_FINE_LOCATION}, 10);
//                return;
//            }
//        }else{
//            configureButton();
//        }
//        configureButton();
    }

//    private void configureButton(){
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//        Log.d(TAG,"configureButton");
//    }


    private void initAd(){
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, "ca-app-pub-6844692045929818~3692035871");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6844692045929818/8919617291");

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.setLocation(mLocation)
                .addKeyword("drawing")
                .addKeyword("photography").build();
        mAdView.loadAd(adRequest);

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the interstitial ad is closed.
                mInterstitialAd.loadAd(new AdRequest.Builder()
                //        .setLocation(mLocation)
                        .addKeyword("drawing")
                        .addKeyword("photography")
                        .build());
                //Log.d(TAG,"mLocation: " + mLocation);
                //locationManager.removeUpdates(locationListener);
            }
        });
        mInterstitialAd.loadAd(new AdRequest.Builder()
        //        .setLocation(mLocation)
                .addKeyword("drawing")
                .addKeyword("photography")
                .build());
        //Log.d(TAG,"mLocation: " + mLocation);
        //locationManager.removeUpdates(locationListener);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        switch (requestCode){
//            case 10:
//                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
//                    configureButton();
//                return;
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.drawer_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        drawer = (CustomDrawerLayout) findViewById(R.id.drawer_layout);
        Log.d(TAG, "drawer created");
        drawer.openDrawer(GravityCompat.START);
        closeD = (ImageButton) findViewById(R.id.imageButton);

        closeD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.closeDrawer(GravityCompat.START);
                openD.setVisibility(View.VISIBLE);
                closeD.setVisibility(View.INVISIBLE);
                hbView.setDrawerOpen(false);

            }
        });

        openD = (ImageButton) findViewById(R.id.imageButton2);
        openD.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                drawer.openDrawer(GravityCompat.START);
                openD.setVisibility(View.INVISIBLE);
                closeD.setVisibility(View.VISIBLE);
                hbView.setDrawerOpen(true);

            }
        });



        navigationView = (NavigationView) findViewById(R.id.drawer_view);
        menu = navigationView.getMenu();
        navigationView.setNavigationItemSelectedListener(dOnNavigationItemSelectedListener);
        navigationView.setItemIconTintList(null);
        //navigationView.getBackground().setAlpha(130);
        drawer.setScrimColor(getResources().getColor(android.R.color.transparent));
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        drawer.openDrawer(GravityCompat.START);
        openD.setVisibility(View.INVISIBLE);
        closeD.setVisibility(View.VISIBLE);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setItemIconTintList(null);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        initView();
        initAd();
        mhandler = new Handler();
        //mhandler.postDelayed(mAd,5*MINUTES);
        Log.d(TAG, "set Saved Bitmap");
        hbView.setSavedBitmap(SaveViewUtil.loadImageFromStorage(getApplicationContext()));
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "save to internal");
        SaveViewUtil.saveToInternalStorage(hbView.getBitmap(), getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "save to internal");
        SaveViewUtil.saveToInternalStorage(hbView.getBitmap(), getApplicationContext());
    }

    public void closeDrawer(){
        drawer = (CustomDrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void openDrawer(){
        drawer = (CustomDrawerLayout) findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
    }

    public boolean isDrawerOpen(){
        drawer = (CustomDrawerLayout) ((MainActivity)getApplicationContext()).findViewById(R.id.drawer_layout);
        //Log.d(TAG, "is Drawer open" + drawer.isDrawerOpen(GravityCompat.START));
        //Log.d(TAG, "is Drawer open" + drawer);
        return this.drawer.isDrawerOpen(GravityCompat.START);
        //return true;
    }

    public CustomDrawerLayout getDrawer(){
        drawer = (CustomDrawerLayout) findViewById(R.id.drawer_layout);
        return drawer;
    }

}