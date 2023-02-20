package com.example.user.store3c;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatCallback;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeIntents;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragmentX;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.example.user.store3c.DeveloperKey.YOUTUBE_API_KEY;
import static com.example.user.store3c.MainActivity.isTab;
import static com.example.user.store3c.MainActivity.mAuth;
import static com.example.user.store3c.MainActivity.rotationScreenWidth;
import static com.example.user.store3c.MainActivity.rotationTabScreenWidth;
import static com.example.user.store3c.MainActivity.userImg;

public class CakeActivity extends YouTubeFailureRecoveryActivity
implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, YouTubePlayer.OnInitializedListener, AppCompatCallback {

    private ProductAdapter cakeAdapter = null;
    private static ArrayList<ProductItem> CakeData = new ArrayList<>();
    private static DatabaseReference cakeAmount;
    private static DatabaseReference cakeName;
    private static DatabaseReference cakeImgName;
    private static DatabaseReference cakePrice;
    private static DatabaseReference cakeIntro;
    private static StorageReference mStorageRef;
    private static ArrayList<StorageReference> imagesRef = new ArrayList<> ();
    private static ArrayList<String> picListName = new ArrayList<> ();
    private static ArrayList<Bitmap> picListImg = new ArrayList<> ();
    private static ArrayList<String> picListPrice = new ArrayList<> ();
    private static ArrayList<String> picListIntro = new ArrayList<> ();
    private static ArrayList<Integer> picListIndex = new ArrayList<> ();
    private static final long ONE_MEGABYTE = 1024 * 1024;
    private static int cakeProductAmount = 0;
    private static int cakeProductImgCount = 0, cakeProductPriceCount = 0;
    private static int cakeProductNameCount = 0, cakeProductIntroCount = 0;
    private static ProgressDialog dialog;
    private static int threadComplete = 0;
    private static int Reload = 0;
    private ImageView logoImage;
    private byte[] dbUserPicture;
    private YouTubePlayerSupportFragmentX YouTubeF;
    private NavigationView navigationView;
    private handler1 handlerDownload1 = new handler1();
    private static handler2 handlerDownload2 = new handler2();
    private static handler3 handlerDownload3 = new handler3();
    private static handler4 handlerDownload4;
    private static handler5 handlerDownload5 = new handler5();
    private static handler6 handlerDownload6 = new handler6();
    private String cakeVideoId = "JRC9XC3DFY8";
    private YouTubePlayer YPlayer = null;
    private AppCompatDelegate delegate;

    private YouTubePlayer.PlayerStateChangeListener playerStateChangeListener = new YouTubePlayer.PlayerStateChangeListener() {
        @Override
        public void onAdStarted() {
            //Toast.makeText(CakeActivity.this, "AdStarted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            String error = errorReason.toString();
            //Toast.makeText(CakeActivity.this, error, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onLoaded(String arg0) {
            //Toast.makeText(CakeActivity.this, "Loaded", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLoading() {
            //Toast.makeText(CakeActivity.this, "Loading", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onVideoEnded() {
            // finish();
        }

        @Override
        public void onVideoStarted() {
            //Toast.makeText(CakeActivity.this, "VideoStarted", Toast.LENGTH_SHORT).show();
        }
    };

    private YouTubePlayer.PlaybackEventListener playbackEventListener = new YouTubePlayer.PlaybackEventListener() {

        @Override
        public void onBuffering(boolean arg0) {
            //Toast.makeText(CakeActivity.this, "Buffering", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            //Toast.makeText(CakeActivity.this, "cake Pause", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPlaying() {
            //Toast.makeText(CakeActivity.this, "Playing", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSeekTo(int arg0) {
            //Toast.makeText(CakeActivity.this, "Seek", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStopped() {
            //Toast.makeText(CakeActivity.this, "cake Stop", Toast.LENGTH_SHORT).show();
        }

    };

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
        setContentView(R.layout.activity_cake);

        RecyclerView cakeRecyclerView;
        AccountDbAdapter dbHelper;

        //let's create the delegate, passing the activity at both arguments (Activity, AppCompatCallback)
        delegate = AppCompatDelegate.create(this, this);
        //we need to call the onCreate() of the AppCompatDelegate
        delegate.onCreate(savedInstanceState);
        //we use the delegate to inflate the layout
        delegate.setContentView(R.layout.activity_cake);
        //Finally, let's add the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarCake);
        delegate.setSupportActionBar(toolbar);
        if (delegate.getSupportActionBar() != null) {
            delegate.getSupportActionBar().setLogo(R.drawable.store_logo);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_cake);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = findViewById(R.id.nav_view_cake);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        DrawerLayout.DrawerListener listene = new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if (YPlayer != null) {
                    try {
                        if (YPlayer.isPlaying()) {
                            YPlayer.pause();
                            //Toast.makeText(CakeActivity.this, "play pause", Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e) {
                        Toast.makeText(CakeActivity.this, "YPayer have released: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        };
        drawer.addDrawerListener(listene);

        if (navigationView.getHeaderCount() > 0) {
            View header = navigationView.getHeaderView(0);
            logoImage = header.findViewById(R.id.logoImage_id);
            dbHelper = new AccountDbAdapter(this);
            if (!dbHelper.IsDbUserEmpty()) {
                try {
                    if (userImg == null) {
                        ArrayList<Cursor> cursor = dbHelper.getFullUserData();
                        if (cursor.size() == 1) {
                            dbUserPicture = cursor.get(0).getBlob(4);
                        } else {
                            int cursorSize = cursor.size(), num, length = 0;
                            int remainLength = cursor.get(cursorSize - 2).getBlob(0).length;
                            byte[] picture = new byte[1000000 * (cursorSize - 2) + remainLength];
                            for (num = 0; num < cursorSize - 2; num++) {
                                System.arraycopy(cursor.get(num).getBlob(0), 0, picture, length, cursor.get(num).getBlob(0).length);
                                length = length + cursor.get(num).getBlob(0).length;
                            }
                            System.arraycopy(cursor.get(num).getBlob(0), 0, picture, length, cursor.get(num).getBlob(0).length);
                            dbUserPicture = picture;
                            Toast.makeText(CakeActivity.this, "picture size: " + dbUserPicture.length, Toast.LENGTH_LONG).show();
                        }
                        userImg = BitmapFactory.decodeByteArray(dbUserPicture, 0, dbUserPicture.length);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (userImg != null) {
                    logoImage.setImageBitmap(userImg);
                }
            }
            dbHelper.close();
        }

        YouTubePlayerView youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youTubePlayerViewCake_id);

        if (InternetConnection.checkConnection(CakeActivity.this)) {

            if (YouTubeIntents.isYouTubeInstalled(CakeActivity.this) ||
                    (YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(CakeActivity.this) == YouTubeInitializationResult.SUCCESS)) {
                youTubePlayerView.initialize(YOUTUBE_API_KEY, this);
                //Toast.makeText(CakeActivity.this, "YouTube ok ", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CakeActivity.this, "YouTube busy ", Toast.LENGTH_SHORT).show();
                youTubePlayerView.initialize(YOUTUBE_API_KEY, this);
            }
        } else {
            Toast.makeText(CakeActivity.this, "網路未連線! ", Toast.LENGTH_SHORT).show();
        }

        if (CakeData.size() == 0) {
            cakeRecyclerView = findViewById(R.id.cakeRecyclerView_id);
            cakeAdapter = new ProductAdapter(CakeData, this, "CAKE");
            cakeRecyclerView.setAdapter(cakeAdapter);
            cakeRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            if (InternetConnection.checkConnection(CakeActivity.this)) {
                mAuth = FirebaseAuth.getInstance();
                if (mAuth.getCurrentUser() == null) {
                    mAuth.signInAnonymously()
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.i("Anonymously===>", "signInAnonymously:success");
                                        setUserAccountText();
                                        startDownload();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.i("Anonymously===>", "signInAnonymously:failure", task.getException());
                                        Toast.makeText(CakeActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                else {
                    setUserAccountText();
                    startDownload();
                }
            }
            else {
                Toast.makeText(CakeActivity.this, "網路未連線! ", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            if (InternetConnection.checkConnection(CakeActivity.this)) {
                mAuth = FirebaseAuth.getInstance();
            }
            else {
                Toast.makeText(CakeActivity.this, "網路未連線! ", Toast.LENGTH_SHORT).show();
            }
            setUserAccountText();
            cakeRecyclerView = findViewById(R.id.cakeRecyclerView_id);
            cakeAdapter = new ProductAdapter(CakeData, this, "CAKE");
            cakeRecyclerView.setAdapter(cakeAdapter);
            cakeRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        }

        logoImage.setOnClickListener(this);

    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult result) {
        int RECOVERY_REQUEST = 1;
        Toast.makeText(CakeActivity.this, "Failured to Initialize!", Toast.LENGTH_LONG).show();
        if (result.isUserRecoverableError()) {
            result.getErrorDialog(CakeActivity.this, RECOVERY_REQUEST).show();
        } else {
            String error = String.format(getString(R.string.player_error), result.toString());
            Toast.makeText(CakeActivity.this, error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
        /** add listeners to YouTubePlayer instance **/
        //player.setPlaybackEventListener(playbackEventListener);
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        player.setPlayerStateChangeListener(playerStateChangeListener);
        player.setPlaybackEventListener(playbackEventListener);

        if (!wasRestored) {
            YPlayer = player;
            if (isTab){
                if (screenWidth > rotationTabScreenWidth) {
                    if (YPlayer.isPlaying()) {
                        YPlayer.setFullscreen(true);
                        YPlayer.play();
                    } else {
                        YPlayer.setFullscreen(true);
                        YPlayer.cueVideo(cakeVideoId);
                    }
                } else {
                    if (YPlayer.isPlaying()) {
                        Toast.makeText(CakeActivity.this, "isPlaying", Toast.LENGTH_LONG).show();
                        YPlayer.setFullscreen(false);
                        YPlayer.play();
                    } else {
                        YPlayer.setFullscreen(false);
                        YPlayer.cueVideo(cakeVideoId);
                    }
                }
            }
            else {
                if (screenWidth > rotationScreenWidth) {
                    if (YPlayer.isPlaying()) {
                        YPlayer.setFullscreen(true);
                        YPlayer.play();
                    } else {
                        YPlayer.setFullscreen(true);
                        YPlayer.cueVideo(cakeVideoId);
                    }
                } else {
                    if (YPlayer.isPlaying()) {
                        Toast.makeText(CakeActivity.this, "isPlaying", Toast.LENGTH_LONG).show();
                        YPlayer.setFullscreen(false);
                        YPlayer.play();
                    } else {
                        YPlayer.setFullscreen(false);
                        YPlayer.cueVideo(cakeVideoId);
                    }
                }
            }
        } else {
            Toast.makeText(CakeActivity.this, "youTubePlayerBoolean error", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        Toast.makeText(CakeActivity.this, "youTubePlayerProvider", Toast.LENGTH_LONG).show();
        return (YouTubePlayerView) findViewById(R.id.youTubePlayerViewCake_id);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        ProductAdapter.screenWidth = screenWidth;
        cakeAdapter.notifyDataSetChanged();
        if (YPlayer != null) {
            try {
                if (isTab) {
                    if (screenWidth > rotationTabScreenWidth) {
                        if (YPlayer.isPlaying()) {
                            YPlayer.setFullscreen(true);
                            YPlayer.play();
                        } else {
                            YPlayer.setFullscreen(true);
                            YPlayer.play();
                        }
                    } else {
                        if (YPlayer.isPlaying()) {
                            YPlayer.setFullscreen(false);
                            YPlayer.play();
                        } else {
                            YPlayer.setFullscreen(false);
                            YPlayer.play();
                        }
                    }
                }
                else {
                    if (screenWidth > rotationScreenWidth) {
                        if (YPlayer.isPlaying()) {
                            YPlayer.setFullscreen(true);
                            YPlayer.play();
                        } else {
                            YPlayer.setFullscreen(true);
                            YPlayer.play();
                        }
                    } else {
                        if (YPlayer.isPlaying()) {
                            YPlayer.setFullscreen(false);
                            YPlayer.play();
                        } else {
                            YPlayer.setFullscreen(false);
                            YPlayer.play();
                        }
                    }
                }
            }catch (Exception e) {
                Toast.makeText(CakeActivity.this, "YPayer have released: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.logoImage_id) {
            Intent intentItem = new Intent();
            Bundle bundleItem = new Bundle();
            bundleItem.putString("Menu", "CAKE");
            intentItem.putExtras(bundleItem);
            intentItem.setClass(CakeActivity.this, UserActivity.class);
            startActivity(intentItem);
            CakeActivity.this.finish();
        }

    }

    private void startDownload() {
        FirebaseDatabase dbCake;
        DatabaseReference cakeRef;

        dbCake = FirebaseDatabase.getInstance();
        try {
            if (!MainActivity.setFirebaseDbPersistence) {
                dbCake.setPersistenceEnabled(true);
                MainActivity.setFirebaseDbPersistence = true;
            }
        }
        catch (Exception e) {
            Toast.makeText(CakeActivity.this, "Pick image timeout, reload data.", Toast.LENGTH_SHORT).show();
            Log.i("Pick image timeout: " , "reload data.");
        }
        cakeRef = dbCake.getReference("cake");
        cakeRef.keepSynced(true);
        cakeAmount = dbCake.getReference("cakeAmount");
        cakeAmount.keepSynced(true);
        cakeName = cakeRef.child("cakeName").getRef();
        cakeName.keepSynced(true);
        cakeImgName = cakeRef.child("cakeImgName").getRef();
        cakeImgName.keepSynced(true);
        cakePrice = cakeRef.child("cakePrice").getRef();
        cakePrice.keepSynced(true);
        cakeIntro = cakeRef.child("cakeIntro").getRef();
        cakeIntro.keepSynced(true);
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://store3c-137123.appspot.com");
        handlerDownload4  = new handler4(cakeAdapter);
        new Thread(runnable1).start();
        dialog = new ProgressDialog(CakeActivity.this);
        dialog.setMessage("正在載入...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnCancelListener(new ProgressDialog.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                // DO SOME STUFF HERE
            }
        });
        dialog.show();
    }

    private void setUserAccountText() {
        TextView userName;
        String user, userAccount = "User Account";

        if (InternetConnection.checkConnection(CakeActivity.this)) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                if (navigationView.getHeaderCount() > 0) {
                    View header = navigationView.getHeaderView(0);
                    userName = header.findViewById(R.id.userName_id);
                    user = currentUser.isAnonymous() ? "Anonymous" : currentUser.getEmail();
                    userName.setText(user);
                }
            } else {
                if (navigationView.getHeaderCount() > 0) {
                    View header = navigationView.getHeaderView(0);
                    userName = header.findViewById(R.id.userName_id);
                    userName.setText(userAccount);
                }
            }
        }
    }

    static class handler1 extends Handler {
        @Override
        public void handleMessage(Message msg1) {
            super.handleMessage(msg1);

            Long messageTransaction = (Long)msg1.obj;
            cakeProductAmount = messageTransaction.intValue();
            Log.i("Firebase ==>", "Download Cake Amount Complete: " + messageTransaction);
            new Thread(runnable2).start();
            new Thread(runnable3).start();
            new Thread(runnable5).start();
            new Thread(runnable6).start();
        }
    }

    Runnable runnable1 = new Runnable() {

        @Override
        public void run() {
            // TODO: http request.
            // Read from the database
            cakeAmount.addValueEventListener(new ValueEventListener() {
                //String messageLoad = "Download Cake Amount Complete";
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    Long value = dataSnapshot.getValue(Long.class);

                    //Log.i("Firebase ==>", "Cake amount is: " + value);
                    Message msg1 = new Message();
                    msg1.obj = value;
                    handlerDownload1.sendMessage(msg1);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Failed to read value
                    Log.i("Firebase ==>", "Failed to read amount.", error.toException());
                    Toast.makeText(CakeActivity.this, "DatabaseError, cakeAmount: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    };

    static class handler2 extends Handler {
        @Override
        public void handleMessage(Message msg2) {
            super.handleMessage(msg2);

            String messageTransaction = (String)msg2.obj;
            Log.i("Firebase ==>", messageTransaction);

        }
    }

    static Runnable runnable2 = new Runnable() {

        @Override
        public void run() {
            // TODO: http request.
            cakeName.addChildEventListener(new ChildEventListener() {
                String messageLoad = "Download Cake Name Complete";
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    String cakeProductName = dataSnapshot.getValue(String.class);
                    picListName.add(cakeProductName);
                    cakeProductNameCount++;
                    //Log.i("Firebase ==>", "cake name is: " + cakeProductName);
                    if (cakeProductNameCount == cakeProductAmount) {
                        Message msg2_1 = new Message();
                        Message msg2_2 = new Message();
                        msg2_1.obj = messageLoad;
                        msg2_2.obj = messageLoad;
                        cakeProductNameCount = 0;
                        handlerDownload2.sendMessage(msg2_1);
                        handlerDownload4.sendMessage(msg2_2);
                    }

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i("Firebase error ==>", "Failed to read value.", databaseError.toException());
                    //Toast.makeText(CakeActivity.this, "DatabaseError, cakeName: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    };

    static class handler3 extends Handler {
        @Override
        public void handleMessage(Message msg3) {
            super.handleMessage(msg3);

            String messageTransaction = (String)msg3.obj;
            Log.i("Firebase ==>", messageTransaction);
            new Thread(runnable4).start();
        }
    }

    static Runnable runnable3 = new Runnable() {

        @Override
        public void run() {
            // TODO: http request.
            cakeImgName.addChildEventListener(new ChildEventListener() {
                String messageLoad = "Download Cake Image Name Complete";
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    String ImgFileName = dataSnapshot.getValue(String.class);
                    cakeProductImgCount++;
                    String cakeImageName = "cake/" + ImgFileName;
                    imagesRef.add(mStorageRef.child(cakeImageName));
                    //Log.i("Firebase ==>", "cake image name is: " + ImgFileName);

                    if (cakeProductImgCount == cakeProductAmount) {
                        Message msg3 = new Message();
                        msg3.obj = messageLoad;
                        cakeProductImgCount = 0;
                        handlerDownload3.sendMessage(msg3);
                    }

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i("Firebase error ==>", "Failed to read value.", databaseError.toException());
                    //Toast.makeText(CakeActivity.this, "DatabaseError, cakeImgName: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    };

    static class handler4 extends Handler {
        private WeakReference<ProductAdapter> weakRefCakeAdapter;

        handler4(ProductAdapter hCakeAdapter) {
            weakRefCakeAdapter = new WeakReference<>(hCakeAdapter);
        }

        @Override
        public void handleMessage(Message msg4) {
            super.handleMessage(msg4);

            String messageTransaction = (String)msg4.obj;
            //Log.i("Firebase thread  ==>", messageTransaction);
            if (messageTransaction.equals("Download Cake Image file Complete")) {
                cakeProductImgCount++;
                if (cakeProductImgCount == cakeProductAmount) {
                    RangeImgSequence();
                    threadComplete++;
                }
            }
            else {
                threadComplete++;
            }
            if (threadComplete == 4) {
                if (Reload == 0) {
                    for (int i = 0; i < cakeProductAmount; i++) {
                        CakeData.add(new ProductItem(picListImg.get(i), picListName.get(i), picListPrice.get(i), picListIntro.get(i)));
                    }
                    Reload = 1;
                }
                ProductAdapter hmCakeAdapter = weakRefCakeAdapter.get();
                if (hmCakeAdapter != null) {
                    hmCakeAdapter.notifyDataSetChanged();
                }
                dialog.dismiss();

            }
        }

        void RangeImgSequence() {
            ArrayList<Bitmap> picListImg1 = new ArrayList<> ();

            for (int i=0; i<cakeProductAmount; i++) {
                for (int j=0; j<cakeProductAmount; j++) {
                    if (i == picListIndex.get(j)) {
                        picListImg1.add(picListImg.get(j));
                    }
                }
            }
            picListImg.subList(0, cakeProductAmount).clear();
            for (int n=0; n<cakeProductAmount; n++) {
                picListImg.add(picListImg1.get(n));
            }

        }
    }

    static Runnable runnable4 = new Runnable() {
        private int indexImg = 0;

        @Override
        public void run() {
            // TODO: http request.
            for (indexImg=0; indexImg < cakeProductAmount; indexImg++) {

                imagesRef.get(indexImg).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    int indexLoad = indexImg;
                    String messageLoad = "Download Cake Image file Complete";
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Data for "images/island.jpg" is returns, use this as needed

                        if (bytes.length != 0) {
                            picListImg.add(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                            picListIndex.add(indexLoad);
                        }
                        //Log.i("Firebase ==>", "Download cake image file success.");
                        Message msg4 = new Message();
                        msg4.obj = messageLoad;
                        handlerDownload4.sendMessage(msg4);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        Log.i("Firebase ==>", "Download file fail.");
                    }
                });

            }
        }
    };

    static class handler5 extends Handler {
        @Override
        public void handleMessage(Message msg5) {
            super.handleMessage(msg5);

            String messageTransaction = (String)msg5.obj;
            Log.i("Firebase ==>", messageTransaction);

        }
    }

    static Runnable runnable5 = new Runnable() {

        @Override
        public void run() {
            // TODO: http request.
            cakePrice.addChildEventListener(new ChildEventListener() {
                String messageLoad = "Download Cake Price Complete";
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    String cakeProductPrice = dataSnapshot.getValue(String.class);
                    picListPrice.add(cakeProductPrice);
                    cakeProductPriceCount++;
                    //Log.i("Firebase ==>", "cake price is: " + cakeProductPrice);
                    if (cakeProductPriceCount == cakeProductAmount) {
                        Message msg5_1 = new Message();
                        Message msg5_2 = new Message();
                        msg5_1.obj = messageLoad;
                        msg5_2.obj = messageLoad;
                        cakeProductPriceCount = 0;
                        handlerDownload5.sendMessage(msg5_1);
                        handlerDownload4.sendMessage(msg5_2);
                    }

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i("Firebase error ==>", "Failed to read value.", databaseError.toException());
                    //Toast.makeText(CakeActivity.this, "DatabaseError, cakePrice: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    };

    static class handler6 extends Handler {
        @Override
        public void handleMessage(Message msg6) {
            super.handleMessage(msg6);

            String messageTransaction = (String)msg6.obj;
            Log.i("Firebase ==>", messageTransaction);

        }
    }

    static Runnable runnable6 = new Runnable() {

        @Override
        public void run() {
            // TODO: http request.
            cakeIntro.addChildEventListener(new ChildEventListener() {
                String messageLoad = "Download Cake Introduction Complete";
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    String cakeIntro = dataSnapshot.getValue(String.class);
                    picListIntro.add(cakeIntro);
                    cakeProductIntroCount++;
                    //Log.i("Firebase ==>", "cake introduction is: " + cakeIntro);
                    if (cakeProductIntroCount == cakeProductAmount) {
                        Message msg6_1 = new Message();
                        Message msg6_2 = new Message();
                        msg6_1.obj = messageLoad;
                        msg6_2.obj = messageLoad;
                        cakeProductIntroCount = 0;
                        handlerDownload6.sendMessage(msg6_1);
                        handlerDownload4.sendMessage(msg6_2);
                    }

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i("Firebase error ==>", "Failed to read value.", databaseError.toException());
                    //Toast.makeText(CakeActivity.this, "DatabaseError, cakeIntro: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    };

    @Override
    public void onBackPressed() {
        Intent intent;
        DrawerLayout drawer = findViewById(R.id.drawer_layout_cake);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            intent = new Intent();
            intent.setClass(CakeActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(intent);
            CakeActivity.this.finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.cake, menu);
        delegate.getMenuInflater().inflate(R.menu.cake, menu);
        if (InternetConnection.checkConnection(CakeActivity.this)) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            menu.add(0,R.id.action_login_status,100,"使用者");
            if (currentUser == null || currentUser.isAnonymous()) {
                menu.findItem(R.id.action_login_status).setTitle("使用者登入");
            } else {
                menu.findItem(R.id.action_login_status).setTitle("使用者登出");
            }
            menu.findItem(R.id.action_login_status).setShowAsAction(SHOW_AS_ACTION_NEVER);
        }
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, @NonNull Menu menu) {
        if (YPlayer != null) {
            try {
                if (YPlayer.isPlaying()) {
                    YPlayer.pause();
                    //Toast.makeText(CakeActivity.this, "play pause", Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e) {
                Toast.makeText(CakeActivity.this, "YPayer have released: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem searchMenuItem = menu.findItem(R.id.action_cake_search);
        final MenuItem shoppingCarMenuItem = menu.findItem(R.id.action_cake_shopping_car);
        FrameLayout rootView = (FrameLayout) searchMenuItem.getActionView();
        FrameLayout rootViewShoppingCar = (FrameLayout) shoppingCarMenuItem.getActionView();

        ImageView searchIcon = rootView.findViewById(R.id.search_icon_id);
        ImageView shoppingCarIcon = rootViewShoppingCar.findViewById(R.id.shopping_car_icon_id);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(searchMenuItem);
            }
        });
        rootViewShoppingCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(shoppingCarMenuItem);
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intentItem;
        Bundle bundleItem;

        switch (id) {
            case R.id.action_position:
                intentItem = new Intent();
                bundleItem = new Bundle();
                bundleItem.putString("Menu", "CAKE");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(CakeActivity.this, PositionActivity.class);
                if (YPlayer != null) {
                    try {
                        if (YPlayer.isPlaying()) {
                            YPlayer.pause();
                            //Toast.makeText(CakeActivity.this, "play pause", Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e) {
                        Toast.makeText(CakeActivity.this, "YPayer have released: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    YPlayer.release();
                }
                startActivity(intentItem);
                CakeActivity.this.finish();
                //Toast.makeText(this.getBaseContext(),"The setting item", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_user_account:
                //  Toast.makeText(this.getBaseContext(),"The user account item", Toast.LENGTH_SHORT).show();
                intentItem = new Intent();
                bundleItem = new Bundle();
                bundleItem.putString("Menu", "CAKE");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(CakeActivity.this,UserActivity.class);
                startActivity(intentItem);
                CakeActivity.this.finish();
                break;
            case R.id.action_login_status:
                if (InternetConnection.checkConnection(CakeActivity.this)) {
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser == null || currentUser.isAnonymous()) {
                        intentItem = new Intent();
                        bundleItem = new Bundle();
                        bundleItem.putString("Menu", "CAKE");
                        intentItem.putExtras(bundleItem);
                        intentItem.setClass(CakeActivity.this, LoginActivity.class);
                        startActivity(intentItem);
                        CakeActivity.this.finish();
                    } else {
                        mAuth.signOut();
                        Toast.makeText(CakeActivity.this, "登出成功 ! ", Toast.LENGTH_SHORT).show();
                        setUserAccountText();
                        item.setTitle("使用者登入");
                    }
                } else {
                    Toast.makeText(CakeActivity.this, "請先開啟網路連線 ! ", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_memo:
                intentItem = new Intent();
                bundleItem = new Bundle();
                bundleItem.putString("Menu", "CAKE");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(CakeActivity.this, MemoActivity.class);
                startActivity(intentItem);
                CakeActivity.this.finish();
                //Toast.makeText(this.getBaseContext(),"The setting item", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_cake_shopping_car:
                intentItem = new Intent();
                bundleItem = new Bundle();
                bundleItem.putString("Menu", "CAKE");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(CakeActivity.this, OrderActivity.class);
                startActivity(intentItem);
                CakeActivity.this.finish();
                break;
            case R.id.action_cake_search:
                intentItem = new Intent();
                bundleItem = new Bundle();
                bundleItem.putString("Menu", "CAKE");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(CakeActivity.this, SearchActivity.class);
                startActivity(intentItem);
                CakeActivity.this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_dish) {
            Intent intentItem = new Intent();
            intentItem.setClass(CakeActivity.this, MainActivity.class);
            intentItem.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(intentItem);
            CakeActivity.this.finish();
        } else if (id == R.id.nav_cake) {

        } else if (id == R.id.nav_phone) {
            Intent intentItem = new Intent();
            intentItem.setClass(CakeActivity.this, PhoneActivity.class);
            startActivity(intentItem);
            CakeActivity.this.finish();
        } else if (id == R.id.nav_camera) {
            //FragmentManager fragmentManager = getSupportFragmentManager();
            //fragmentManager.popBackStack();
            //fragmentManager.beginTransaction().remove(YouTubeFragmentCake.youtubePlayerFragment).commit();
            //fragmentManager.executePendingTransactions();

            Intent intentItem = new Intent();
            intentItem.setClass(CakeActivity.this, CameraActivity.class);
            if (YPlayer != null) {
                YPlayer.release();
            }
            startActivity(intentItem);
            CakeActivity.this.finish();
        } else if (id == R.id.nav_book) {
            Intent intentItem = new Intent();
            intentItem.setClass(CakeActivity.this, BookActivity.class);
            startActivity(intentItem);
            CakeActivity.this.finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_cake);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
