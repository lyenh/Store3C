package com.example.user.store3c;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatCallback;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static com.example.user.store3c.MainActivity.isTab;
import static com.example.user.store3c.MainActivity.rotationScreenWidth;
import static com.example.user.store3c.MainActivity.rotationTabScreenWidth;

public class PositionActivity extends Activity implements View.OnClickListener, YouTubeFragment.OnFragmentInteractionListener, AppCompatCallback {

    private String menu_item;
    private Intent intent = null;
    private YouTubeFragment YouTubeF;
    private AppCompatDelegate delegate;

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {
        //let's leave this empty, for now
    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {
        // let's leave this empty, for now
    }

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position);

        //let's create the delegate, passing the activity at both arguments (Activity, AppCompatCallback)
        delegate = AppCompatDelegate.create(this, this);
        //we need to call the onCreate() of the AppCompatDelegate
        delegate.onCreate(savedInstanceState);
        //we use the delegate to inflate the layout
        delegate.setContentView(R.layout.activity_position);
        //Finally, let's add the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPosition);
        delegate.setSupportActionBar(toolbar);

        //getSupportActionBar().setLogo(R.drawable.store1);
        Button returnBtn, position1Btn, position2Btn, position3Btn, position4Btn, position5Btn, position6Btn, position7Btn;
        FragmentManager fragManager;
        String mallVideoId = "Bq5eNtlz8Fw";
        Intent intentItem = getIntent();
        Bundle bundleItem = intentItem.getExtras();
        if (bundleItem != null) {
            menu_item = bundleItem.getString("Menu");
        }
        returnBtn = findViewById(R.id.positionRtb_id);
        position1Btn = findViewById(R.id.mall1_id);
        position2Btn = findViewById(R.id.mall2_id);
        position3Btn = findViewById(R.id.mall3_id);
        position4Btn = findViewById(R.id.mall4_id);
        position5Btn = findViewById(R.id.mall5_id);
        position6Btn = findViewById(R.id.mall6_id);
        position7Btn = findViewById(R.id.mall7_id);

        if (YouTubeF == null && savedInstanceState == null) {
            YouTubeF = YouTubeFragment.newInstance(mallVideoId);
            getFragmentManager().beginTransaction()
                    .add(R.id.youTubeFrameLayoutPosition_id, YouTubeF,null)
                    .commit();
        }

        returnBtn.setOnClickListener(this);
        position1Btn.setOnClickListener(this);
        position2Btn.setOnClickListener(this);
        position3Btn.setOnClickListener(this);
        position4Btn.setOnClickListener(this);
        position5Btn.setOnClickListener(this);
        position6Btn.setOnClickListener(this);
        position7Btn.setOnClickListener(this);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        if (YouTubeFragment.YPlayer != null) {
            try {
                if (isTab) {
                    if (screenWidth > rotationTabScreenWidth) {
                        if (YouTubeFragment.YPlayer.isPlaying()) {
                            YouTubeFragment.YPlayer.setFullscreen(true);
                            YouTubeFragment.YPlayer.play();
                        } else {
                            YouTubeFragment.YPlayer.setFullscreen(true);
                            YouTubeFragment.YPlayer.play();
                        }
                    } else {
                        if (YouTubeFragment.YPlayer.isPlaying()) {
                            YouTubeFragment.YPlayer.setFullscreen(false);
                            YouTubeFragment.YPlayer.play();
                        } else {
                            YouTubeFragment.YPlayer.setFullscreen(false);
                            YouTubeFragment.YPlayer.play();
                        }
                    }
                }
                else {
                    if (screenWidth > rotationScreenWidth) {
                        if (YouTubeFragment.YPlayer.isPlaying()) {
                            YouTubeFragment.YPlayer.setFullscreen(true);
                            YouTubeFragment.YPlayer.play();
                        } else {
                            YouTubeFragment.YPlayer.setFullscreen(true);
                            YouTubeFragment.YPlayer.play();
                        }
                    } else {
                        if (YouTubeFragment.YPlayer.isPlaying()) {
                            YouTubeFragment.YPlayer.setFullscreen(false);
                            YouTubeFragment.YPlayer.play();
                        } else {
                            YouTubeFragment.YPlayer.setFullscreen(false);
                            YouTubeFragment.YPlayer.play();
                        }
                    }
                }
            }catch (Exception e) {
                Toast.makeText(PositionActivity.this, "YPayer have released: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View v) {
        Bundle bundle;
        if (InternetConnection.checkConnection(PositionActivity.this)) {
            switch (v.getId()) {
                case R.id.mall1_id:
                    intent = new Intent();
                    bundle = new Bundle();
                    bundle.putString("Menu", menu_item);
                    bundle.putFloat("Latitude", (float) 25.053603);
                    bundle.putFloat("Longitude", (float) 121.288672);
                    bundle.putString("Mall", "台茂購物中心");
                    intent.putExtras(bundle);
                    intent.setClass(PositionActivity.this, MapsActivity.class);
                    startActivity(intent);
                    PositionActivity.this.finish();
                    break;
                case R.id.mall2_id:
                    intent = new Intent();
                    bundle = new Bundle();
                    bundle.putString("Menu", menu_item);
                    bundle.putFloat("Latitude", (float) 25.000910);
                    bundle.putFloat("Longitude", (float) 121.228675);
                    bundle.putString("Mall", "大江購物中心");
                    intent.putExtras(bundle);
                    intent.setClass(PositionActivity.this, MapsActivity.class);
                    startActivity(intent);
                    PositionActivity.this.finish();
                    break;
                case R.id.mall3_id:
                    intent = new Intent();
                    bundle = new Bundle();
                    bundle.putString("Menu", menu_item);
                    bundle.putFloat("Latitude", (float) 24.920706);
                    bundle.putFloat("Longitude", (float) 121.213215);
                    bundle.putString("Mall", "大潤發平鎮店");
                    intent.putExtras(bundle);
                    intent.setClass(PositionActivity.this, MapsActivity.class);
                    startActivity(intent);
                    PositionActivity.this.finish();
                    break;
                case R.id.mall4_id:
                    intent = new Intent();
                    bundle = new Bundle();
                    bundle.putString("Menu", menu_item);
                    bundle.putFloat("Latitude", (float) 24.972582);
                    bundle.putFloat("Longitude", (float) 121.253658);
                    bundle.putString("Mall", "家樂福內壢店");
                    intent.putExtras(bundle);
                    intent.setClass(PositionActivity.this, MapsActivity.class);
                    startActivity(intent);
                    PositionActivity.this.finish();
                    break;
                case R.id.mall5_id:
                    intent = new Intent();
                    bundle = new Bundle();
                    bundle.putString("Menu", menu_item);
                    bundle.putFloat("Latitude", (float) 24.985347);
                    bundle.putFloat("Longitude", (float) 121.285624);
                    bundle.putString("Mall", "愛買桃園店");
                    intent.putExtras(bundle);
                    intent.setClass(PositionActivity.this, MapsActivity.class);
                    startActivity(intent);
                    PositionActivity.this.finish();
                    break;
                case R.id.mall6_id:
                    intent = new Intent();
                    bundle = new Bundle();
                    bundle.putString("Menu", menu_item);
                    bundle.putFloat("Latitude", (float) 24.963524);
                    bundle.putFloat("Longitude", (float) 121.155960);
                    bundle.putString("Mall", "好市多中壢店");
                    intent.putExtras(bundle);
                    intent.setClass(PositionActivity.this, MapsActivity.class);
                    startActivity(intent);
                    PositionActivity.this.finish();
                    break;
                case R.id.mall7_id:
                    intent = new Intent();
                    bundle = new Bundle();
                    bundle.putString("Menu", menu_item);
                    bundle.putFloat("Latitude", (float) 25.014282);
                    bundle.putFloat("Longitude", (float) 121.213118);
                    bundle.putString("Mall", "華泰名品城");
                    intent.putExtras(bundle);
                    intent.setClass(PositionActivity.this, MapsActivity.class);
                    startActivity(intent);
                    PositionActivity.this.finish();
                    break;
                case R.id.positionRtb_id:
                    Intent intentItem = new Intent();
                    switch (menu_item) {
                        case "DISH":
                            intentItem.setClass(PositionActivity.this, MainActivity.class);
                            intentItem.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            break;
                        case "CAKE":
                            intentItem.setClass(PositionActivity.this, CakeActivity.class);
                            if (YouTubeFragment.YPlayer != null) {
                                try {
                                    if (YouTubeFragment.YPlayer.isPlaying()) {
                                        YouTubeFragment.YPlayer.pause();
                                        //Toast.makeText(CakeActivity.this, "play pause", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(PositionActivity.this, "YPayer have released: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                YouTubeFragment.YPlayer.release();
                            }
                            break;
                        case "PHONE":
                            intentItem.setClass(PositionActivity.this, PhoneActivity.class);
                            break;
                        case "CAMERA":
                            intentItem.setClass(PositionActivity.this, CameraActivity.class);
                            if (YouTubeFragment.YPlayer != null) {
                                try {
                                    if (YouTubeFragment.YPlayer.isPlaying()) {
                                        YouTubeFragment.YPlayer.pause();
                                        //Toast.makeText(CakeActivity.this, "play pause", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(PositionActivity.this, "YPayer have released: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                YouTubeFragment.YPlayer.release();
                            }
                            break;
                        case "BOOK":
                            intentItem.setClass(PositionActivity.this, BookActivity.class);
                            break;
                        default:
                            Toast.makeText(this.getBaseContext(), "Return to main menu ! ", Toast.LENGTH_SHORT).show();
                            intentItem.setClass(PositionActivity.this, MainActivity.class);
                            intentItem.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    }
                    startActivity(intentItem);
                    PositionActivity.this.finish();
                    break;
            }
        } else {
            if (v.getId() == R.id.positionRtb_id) {
                Intent intentItem = new Intent();
                switch (menu_item) {
                    case "DISH":
                        intentItem.setClass(PositionActivity.this, MainActivity.class);
                        intentItem.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        break;
                    case "CAKE":
                        intentItem.setClass(PositionActivity.this, CakeActivity.class);
                        break;
                    case "PHONE":
                        intentItem.setClass(PositionActivity.this, PhoneActivity.class);
                        break;
                    case "CAMERA":
                        intentItem.setClass(PositionActivity.this, CameraActivity.class);
                        break;
                    case "BOOK":
                        intentItem.setClass(PositionActivity.this, BookActivity.class);
                        break;
                    default:
                        Toast.makeText(this.getBaseContext(), "Return to main menu ! ", Toast.LENGTH_SHORT).show();
                        intentItem.setClass(PositionActivity.this, MainActivity.class);
                        intentItem.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                }
                startActivity(intentItem);
                PositionActivity.this.finish();
            } else {
                Toast.makeText(PositionActivity.this, "請先開啟網路連線 ! ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onFragmentInteractionL(Uri uri) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        intent = new Intent();
        switch (menu_item) {
            case "DISH":
                intent.setClass(PositionActivity.this, MainActivity.class);
                intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                break;
            case "CAKE":
                intent.setClass(PositionActivity.this, CakeActivity.class);
                if (YouTubeFragment.YPlayer != null) {
                    try {
                        if (YouTubeFragment.YPlayer.isPlaying()) {
                            YouTubeFragment.YPlayer.pause();
                            //Toast.makeText(CakeActivity.this, "play pause", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(PositionActivity.this, "YPayer have released: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    YouTubeFragment.YPlayer.release();
                }
                break;
            case "PHONE":
                intent.setClass(PositionActivity.this, PhoneActivity.class);
                break;
            case "CAMERA":
                intent.setClass(PositionActivity.this, CameraActivity.class);
                if (YouTubeFragment.YPlayer != null) {
                    try {
                        if (YouTubeFragment.YPlayer.isPlaying()) {
                            YouTubeFragment.YPlayer.pause();
                            //Toast.makeText(CakeActivity.this, "play pause", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(PositionActivity.this, "YPayer have released: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    YouTubeFragment.YPlayer.release();
                }
                break;
            case "BOOK":
                intent.setClass(PositionActivity.this, BookActivity.class);
                break;
            default:
                Toast.makeText(this.getBaseContext(), "Return to main menu ! ", Toast.LENGTH_SHORT).show();
                intent.setClass(PositionActivity.this, MainActivity.class);
                intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        startActivity(intent);
        PositionActivity.this.finish();

    }

}