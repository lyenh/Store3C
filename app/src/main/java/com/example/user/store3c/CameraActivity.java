package com.example.user.store3c;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubePlayer;
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
import static com.example.user.store3c.MainActivity.userImg;
import static java.lang.Integer.parseInt;
import static com.example.user.store3c.MainActivity.mAuth;

public class CameraActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, YouTubeFragment.OnFragmentInteractionListener {

    private CameraAdapter camera1Adapter = null, camera2Adapter = null, camera3Adapter = null;
    private static ArrayList<ProductItem> CameraData1 = new ArrayList<>(), CameraData2 = new ArrayList<>(), CameraData3 = new ArrayList<>();
    private static DatabaseReference cameraAmount;
    private static DatabaseReference cameraName;
    private static DatabaseReference cameraImgName;
    private static DatabaseReference cameraPrice;
    private static DatabaseReference cameraIntro;
    private static StorageReference mStorageRef;
    private static ArrayList<StorageReference> imagesRef = new ArrayList<> ();
    private static ArrayList<String> picListName = new ArrayList<> ();
    private static ArrayList<Bitmap> picListImg = new ArrayList<> ();
    private static ArrayList<String> picListPrice = new ArrayList<> ();
    private static ArrayList<String> picListIntro = new ArrayList<> ();
    private static ArrayList<Integer> picListIndex = new ArrayList<> ();
    private static final long ONE_MEGABYTE = 1024 * 1024;
    private static int cameraProductAmount = 0, camera1ProductAmount = 0, camera2ProductAmount = 0, camera3ProductAmount = 0;
    private static int cameraProductImgCount = 0, cameraProductPriceCount = 0;
    private static int cameraProductNameCount = 0, cameraProductIntroCount = 0;
    private static ProgressDialog dialog = null;
    private static int threadComplete = 0;
    private static int Reload = 0;
    private ImageView logoImage;
    private byte[] dbUserPicture;
    private NavigationView navigationView;
    private YouTubeFragment YouTubeF = null;
    private String cameraVideoId = "2O0C4PnULtk";
    private static handler1 handlerDownload1 = new handler1();
    private static handler2 handlerDownload2 = new handler2();
    private static handler3 handlerDownload3 = new handler3();
    private static handler4 handlerDownload4;
    private static handler5 handlerDownload5 = new handler5();
    private static handler6 handlerDownload6 = new handler6();

    private YouTubePlayer.PlayerStateChangeListener playerStateChangeListener = new YouTubePlayer.PlayerStateChangeListener() {
        @Override
        public void onAdStarted() {
            //Toast.makeText(CameraActivity.this, "AdStarted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            String error = errorReason.toString();
            Toast.makeText(CameraActivity.this, error, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onLoaded(String arg0) {
            //Toast.makeText(CameraActivity.this, "Loaded", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLoading() {
            //Toast.makeText(CameraActivity.this, "Loading", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onVideoEnded() {
            // finish();
        }

        @Override
        public void onVideoStarted() {
            //Toast.makeText(CameraActivity.this, "VideoStarted", Toast.LENGTH_SHORT).show();
        }
    };

    private YouTubePlayer.PlaybackEventListener playbackEventListener = new YouTubePlayer.PlaybackEventListener() {

        @Override
        public void onBuffering(boolean arg0) {
            //Toast.makeText(CameraActivity.this, "Buffering", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            //Toast.makeText(CameraActivity.this, "camera Paused", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPlaying() {
            //Toast.makeText(CameraActivity.this, "Playing", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSeekTo(int arg0) {
            //Toast.makeText(CameraActivity.this, "Seek", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStopped() {
            //Toast.makeText(CameraActivity.this, "camera Stop", Toast.LENGTH_SHORT).show();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        RecyclerView camera1RecyclerView, camera2RecyclerView, camera3RecyclerView;
        LinearLayoutManager layoutManager1, layoutManager2, layoutManager3;

        AccountDbAdapter dbHelper;
        Toolbar toolbar = findViewById(R.id.toolbarCamera);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_camera);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view_camera);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        DrawerLayout.DrawerListener listene = new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if (YouTubeFragment.YPlayer != null) {
                    try {
                        if (YouTubeFragment.YPlayer.isPlaying()) {
                            YouTubeFragment.YPlayer.pause();
                            //Toast.makeText(CameraActivity.this, "play pause", Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e) {
                        Toast.makeText(CameraActivity.this, "YPayer have released: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setLogo(R.drawable.store_logo);
        }
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
                            Toast.makeText(CameraActivity.this, "picture size: " + dbUserPicture.length, Toast.LENGTH_LONG).show();
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

        if (YouTubeF == null && savedInstanceState == null) {
            YouTubeF = YouTubeFragment.newInstance(cameraVideoId);
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.youTubeFrameLayout_id, YouTubeF,null)
                    .commit();
        }

        if (CameraData1.size() == 0) {
            camera1RecyclerView = findViewById(R.id.camera1RecyclerView_id);
            camera1Adapter = new CameraAdapter(CameraData1, this, "CAMERA", YouTubeF);
            camera1RecyclerView.setAdapter(camera1Adapter);
            layoutManager1 = new LinearLayoutManager(this);
            layoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
            camera1RecyclerView.setLayoutManager(layoutManager1);

            camera2RecyclerView = findViewById(R.id.camera2RecyclerView_id);
            camera2Adapter = new CameraAdapter(CameraData2, this, "CAMERA", YouTubeF);
            camera2RecyclerView.setAdapter(camera2Adapter);
            layoutManager2 = new LinearLayoutManager(this);
            layoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
            camera2RecyclerView.setLayoutManager(layoutManager2);

            camera3RecyclerView = findViewById(R.id.camera3RecyclerView_id);
            camera3Adapter = new CameraAdapter(CameraData3, this, "CAMERA", YouTubeF);
            camera3RecyclerView.setAdapter(camera3Adapter);
            layoutManager3 = new LinearLayoutManager(this);
            layoutManager3.setOrientation(LinearLayoutManager.HORIZONTAL);
            camera3RecyclerView.setLayoutManager(layoutManager3);
            if (InternetConnection.checkConnection(CameraActivity.this)) {
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
                                        Toast.makeText(CameraActivity.this, "Authentication failed.",
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
                Toast.makeText(CameraActivity.this, "網路未連線! ", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            setUserAccountText();
            camera1RecyclerView = findViewById(R.id.camera1RecyclerView_id);
            camera1Adapter = new CameraAdapter(CameraData1, this, "CAMERA", YouTubeF);
            camera1RecyclerView.setAdapter(camera1Adapter);
            layoutManager1 = new LinearLayoutManager(this);
            layoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
            camera1RecyclerView.setLayoutManager(layoutManager1);

            camera2RecyclerView = findViewById(R.id.camera2RecyclerView_id);
            camera2Adapter = new CameraAdapter(CameraData2, this, "CAMERA", YouTubeF);
            camera2RecyclerView.setAdapter(camera2Adapter);
            layoutManager2 = new LinearLayoutManager(this);
            layoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
            camera2RecyclerView.setLayoutManager(layoutManager2);

            camera3RecyclerView = findViewById(R.id.camera3RecyclerView_id);
            camera3Adapter = new CameraAdapter(CameraData3, this, "CAMERA", YouTubeF);
            camera3RecyclerView.setAdapter(camera3Adapter);
            layoutManager3 = new LinearLayoutManager(this);
            layoutManager3.setOrientation(LinearLayoutManager.HORIZONTAL);
            camera3RecyclerView.setLayoutManager(layoutManager3);
        }

        logoImage.setOnClickListener(this);
    }

    @Override
    public void onFragmentInteractionL(Uri uri) {

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        if (YouTubeFragment.YPlayer != null) {
            try {
                if (screenWidth > 800) {
                    if (YouTubeFragment.YPlayer.isPlaying()) {
                        YouTubeFragment.YPlayer.setFullscreen(true);
                        YouTubeFragment.YPlayer.play();
                    } else {
                        YouTubeFragment.YPlayer.setFullscreen(true);
                        YouTubeFragment.YPlayer.cueVideo(cameraVideoId);
                    }
                } else {
                    if (YouTubeFragment.YPlayer.isPlaying()) {
                        YouTubeFragment.YPlayer.setFullscreen(false);
                        YouTubeFragment.YPlayer.play();
                    } else {
                        YouTubeFragment.YPlayer.setFullscreen(false);
                        YouTubeFragment.YPlayer.cueVideo(cameraVideoId);
                    }
                }
            }catch (Exception e) {
                Toast.makeText(CameraActivity.this, "YPayer have released: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            bundleItem.putString("Menu", "CAMERA");
            intentItem.putExtras(bundleItem);
            intentItem.setClass(CameraActivity.this,UserActivity.class);
            startActivity(intentItem);
            CameraActivity.this.finish();
        }
    }

    private void startDownload() {
        FirebaseDatabase dbCamera;
        DatabaseReference cameraRef;

        dbCamera = FirebaseDatabase.getInstance();
        try {
            if (!MainActivity.setFirebaseDbPersistence) {
                dbCamera.setPersistenceEnabled(true);
                MainActivity.setFirebaseDbPersistence = true;
            }
        }
        catch (Exception e) {
            Toast.makeText(CameraActivity.this, "Pick image timeout, reload data.", Toast.LENGTH_SHORT).show();
            Log.i("Pick image timeout: " , "reload data.");
        }
        cameraRef = dbCamera.getReference("camera");
        cameraRef.keepSynced(true);
        cameraAmount = dbCamera.getReference("cameraAmount");
        cameraAmount.keepSynced(true);
        cameraName = cameraRef.child("cameraName").getRef();
        cameraName.keepSynced(true);
        cameraImgName = cameraRef.child("cameraImgName").getRef();
        cameraImgName.keepSynced(true);
        cameraPrice = cameraRef.child("cameraPrice").getRef();
        cameraPrice.keepSynced(true);
        cameraIntro = cameraRef.child("cameraIntro").getRef();
        cameraIntro.keepSynced(true);
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://store3c-137123.appspot.com");
        handlerDownload4  = new handler4(camera1Adapter, camera2Adapter, camera3Adapter);
        new Thread(runnable1).start();
        dialog = new ProgressDialog(CameraActivity.this);
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

        if (InternetConnection.checkConnection(CameraActivity.this)) {
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

            int messageTransaction = (int)msg1.obj;
            Log.i("Firebase ==>", "Download Camera Amount Complete: " + messageTransaction);
            new Thread(runnable2).start();
            new Thread(runnable3).start();
            new Thread(runnable5).start();
            new Thread(runnable6).start();
        }
    }

    static Runnable runnable1 = new Runnable() {

        @Override
        public void run() {
            // TODO: http request.
            // Read from the database
            cameraAmount.addValueEventListener(new ValueEventListener() {
                //String messageLoad = "Download Camera Amount Complete";
                String[] tokens;
                int[] amount = new int[3];
                int i = 0;
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    String value = dataSnapshot.getValue(String.class);
                    if (value != null) {
                        tokens = value.split(",");
                    }
                    for (String token:tokens) {
                        amount[i] = parseInt(token);
                        i++;
                    }
                    camera1ProductAmount = amount[0];
                    camera2ProductAmount = amount[1];
                    camera3ProductAmount = amount[2];
                    cameraProductAmount = camera1ProductAmount + camera2ProductAmount + camera3ProductAmount;
                    //Log.i("Firebase ==>", "Camera amount is: " + cameraProductAmount);
                    Message msg1 = new Message();
                    msg1.obj = cameraProductAmount;
                    handlerDownload1.sendMessage(msg1);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Failed to read value
                    Log.i("Firebase ==>", "Failed to read amount.", error.toException());
                    //Toast.makeText(CameraActivity.this, "DatabaseError, cameraAmount: " + error.getMessage(), Toast.LENGTH_SHORT).show();

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
            cameraName.addChildEventListener(new ChildEventListener() {
                String messageLoad = "Download Camera Name Complete";
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    String cameraProductName = dataSnapshot.getValue(String.class);
                    picListName.add(cameraProductName);
                    cameraProductNameCount++;
                    //Log.i("Firebase ==>", "camera name is: " + cameraProductName);
                    if (cameraProductNameCount == cameraProductAmount) {
                        Message msg2_1 = new Message();
                        Message msg2_2 = new Message();
                        msg2_1.obj = messageLoad;
                        msg2_2.obj = messageLoad;
                        cameraProductNameCount = 0;
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
                    //Toast.makeText(CameraActivity.this, "DatabaseError, cameraName: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
            cameraImgName.addChildEventListener(new ChildEventListener() {
                String messageLoad = "Download Camera Image Name Complete";
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    String ImgFileName = dataSnapshot.getValue(String.class);
                    cameraProductImgCount++;
                    String cameraImageName = "camera/" + ImgFileName;
                    imagesRef.add(mStorageRef.child(cameraImageName));
                    //Log.i("Firebase ==>", "camera image name is: " + ImgFileName);

                    if (cameraProductImgCount == cameraProductAmount) {
                        Message msg3 = new Message();
                        msg3.obj = messageLoad;
                        cameraProductImgCount = 0;
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
                    //Toast.makeText(CameraActivity.this, "DatabaseError, cameraImgName: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    };

    static class handler4 extends Handler {
        private WeakReference<CameraAdapter> weakRefCamera1Adapter, weakRefCamera2Adapter, weakRefCamera3Adapter;

        handler4(CameraAdapter hCamera1Adapter, CameraAdapter hCamera2Adapter, CameraAdapter hCamera3Adapter) {
            weakRefCamera1Adapter = new WeakReference<>(hCamera1Adapter);
            weakRefCamera2Adapter = new WeakReference<>(hCamera2Adapter);
            weakRefCamera3Adapter = new WeakReference<>(hCamera3Adapter);
        }

        @Override
        public void handleMessage(Message msg4) {
            super.handleMessage(msg4);

            String messageTransaction = (String)msg4.obj;
            //Log.i("Firebase thread  ==>", messageTransaction);
            if (messageTransaction.equals("Download Camera Image file Complete")) {
                cameraProductImgCount++;
                if (cameraProductImgCount == cameraProductAmount) {
                    RangeImgSequence();
                    threadComplete++;
                }
            }
            else {
                threadComplete++;
            }
            if (threadComplete == 4) {
                if (Reload == 0) {
                    for (int i = 0; i < cameraProductAmount; i++) {
                        if (i < camera1ProductAmount) {
                            CameraData1.add(new ProductItem(picListImg.get(i), picListName.get(i), picListPrice.get(i), picListIntro.get(i)));
                        } else if (i < (camera1ProductAmount + camera2ProductAmount) && (i >= camera1ProductAmount)) {
                            CameraData2.add(new ProductItem(picListImg.get(i), picListName.get(i), picListPrice.get(i), picListIntro.get(i)));
                        } else if (i >= (camera1ProductAmount + camera2ProductAmount)) {
                            CameraData3.add(new ProductItem(picListImg.get(i), picListName.get(i), picListPrice.get(i), picListIntro.get(i)));
                        }
                    }
                    Reload = 1;
                }
                CameraAdapter hmCamera1Adapter = weakRefCamera1Adapter.get();
                CameraAdapter hmCamera2Adapter = weakRefCamera2Adapter.get();
                CameraAdapter hmCamera3Adapter = weakRefCamera3Adapter.get();
                if (hmCamera1Adapter != null) {
                    hmCamera1Adapter.notifyDataSetChanged();
                }
                if (hmCamera2Adapter != null) {
                    hmCamera2Adapter.notifyDataSetChanged();
                }
                if (hmCamera3Adapter != null) {
                    hmCamera3Adapter.notifyDataSetChanged();
                }
                dialog.dismiss();
            }
        }

        void RangeImgSequence() {
            ArrayList<Bitmap> picListImg1 = new ArrayList<> ();

            for (int i=0; i<cameraProductAmount; i++) {
                for (int j=0; j<cameraProductAmount; j++) {
                    if (i == picListIndex.get(j)) {
                        picListImg1.add(picListImg.get(j));
                    }
                }
            }
            picListImg.subList(0, cameraProductAmount).clear();
            for (int n=0; n<cameraProductAmount; n++) {
                picListImg.add(picListImg1.get(n));
            }

        }

    }

    static Runnable runnable4 = new Runnable() {
        private int indexImg = 0;

        @Override
        public void run() {
            // TODO: http request.
            for (indexImg=0; indexImg < cameraProductAmount; indexImg++) {

                imagesRef.get(indexImg).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    int indexLoad = indexImg;
                    String messageLoad = "Download Camera Image file Complete";
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Data for "images/island.jpg" is returns, use this as needed

                        if (bytes.length != 0) {
                            picListImg.add(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                            picListIndex.add(indexLoad);
                        }
                        //Log.i("Firebase ==>", "Download camera image file success.");
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
            cameraPrice.addChildEventListener(new ChildEventListener() {
                String messageLoad = "Download Camera Price Complete";
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    String cameraProductPrice = dataSnapshot.getValue(String.class);
                    picListPrice.add(cameraProductPrice);
                    cameraProductPriceCount++;
                    //Log.i("Firebase ==>", "camera price is: " + cameraProductPrice);
                    if (cameraProductPriceCount == cameraProductAmount) {
                        Message msg5_1 = new Message();
                        Message msg5_2 = new Message();
                        msg5_1.obj = messageLoad;
                        msg5_2.obj = messageLoad;
                        cameraProductPriceCount = 0;
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
                    //Toast.makeText(CameraActivity.this, "  DatabaseError, cameraPrice: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
            cameraIntro.addChildEventListener(new ChildEventListener() {
                String messageLoad = "Download Camera Introduction Complete";
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    String cameraIntro = dataSnapshot.getValue(String.class);
                    picListIntro.add(cameraIntro);
                    cameraProductIntroCount++;
                    //Log.i("Firebase ==>", "camera introduction is: " + cameraIntro);
                    if (cameraProductIntroCount == cameraProductAmount) {
                        Message msg6_1 = new Message();
                        Message msg6_2 = new Message();
                        msg6_1.obj = messageLoad;
                        msg6_2.obj = messageLoad;
                        cameraProductIntroCount = 0;
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
                    //Toast.makeText(CameraActivity.this, "DatabaseError, cameraIntro: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    };

    @Override
    public void onBackPressed() {
        Intent intent;

        DrawerLayout drawer = findViewById(R.id.drawer_layout_camera);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            intent = new Intent();
            intent.setClass(CameraActivity.this, MainActivity.class);
            startActivity(intent);
            CameraActivity.this.finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.camera, menu);
        if (InternetConnection.checkConnection(CameraActivity.this)) {
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
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (YouTubeFragment.YPlayer != null) {
            try {
                if (YouTubeFragment.YPlayer.isPlaying()) {
                    YouTubeFragment.YPlayer.pause();
                    //Toast.makeText(CameraActivity.this, "play pause", Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e) {
                Toast.makeText(CameraActivity.this, "YPayer have released: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        return super.onMenuOpened(featureId, menu);
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
                bundleItem.putString("Menu", "CAMERA");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(CameraActivity.this, PositionActivity.class);
                if (YouTubeFragment.YPlayer != null) {
                    try {
                        if (YouTubeFragment.YPlayer.isPlaying()) {
                            YouTubeFragment.YPlayer.pause();
                            //Toast.makeText(CakeActivity.this, "play pause", Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e) {
                        Toast.makeText(CameraActivity.this, "YPayer have released: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    YouTubeFragment.YPlayer.release();
                }
                startActivity(intentItem);
                CameraActivity.this.finish();
                //Toast.makeText(this.getBaseContext(),"The setting item", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_user_account:
                //  Toast.makeText(this.getBaseContext(),"The user account item", Toast.LENGTH_SHORT).show();
                intentItem = new Intent();
                bundleItem = new Bundle();
                bundleItem.putString("Menu", "CAMERA");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(CameraActivity.this,UserActivity.class);
                startActivity(intentItem);
                CameraActivity.this.finish();
                break;
            case R.id.action_login_status:
                if (InternetConnection.checkConnection(CameraActivity.this)) {
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser == null || currentUser.isAnonymous()) {
                        intentItem = new Intent();
                        bundleItem = new Bundle();
                        bundleItem.putString("Menu", "CAMERA");
                        intentItem.putExtras(bundleItem);
                        intentItem.setClass(CameraActivity.this, LoginActivity.class);
                        startActivity(intentItem);
                        CameraActivity.this.finish();
                    } else {
                        mAuth.signOut();
                        Toast.makeText(CameraActivity.this, "登出成功 ! ", Toast.LENGTH_SHORT).show();
                        setUserAccountText();
                        item.setTitle("使用者登入");
                    }
                } else {
                    Toast.makeText(CameraActivity.this, "請先開啟網路連線 ! ", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_memo:
                intentItem = new Intent();
                bundleItem = new Bundle();
                bundleItem.putString("Menu", "CAMERA");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(CameraActivity.this, MemoActivity.class);
                startActivity(intentItem);
                CameraActivity.this.finish();
                //Toast.makeText(this.getBaseContext(),"The setting item", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_camera_shopping_car:
                intentItem = new Intent();
                bundleItem = new Bundle();
                bundleItem.putString("Menu", "CAMERA");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(CameraActivity.this, OrderActivity.class);
                startActivity(intentItem);
                CameraActivity.this.finish();
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
            intentItem.setClass(CameraActivity.this, MainActivity.class);
            startActivity(intentItem);
            CameraActivity.this.finish();
        } else if (id == R.id.nav_cake) {
            Intent intentItem = new Intent();
            intentItem.setClass(CameraActivity.this, CakeActivity.class);
            if (YouTubeFragment.YPlayer != null) {
                YouTubeFragment.YPlayer.release();
            }
            startActivity(intentItem);
            CameraActivity.this.finish();
        } else if (id == R.id.nav_phone) {
            Intent intentItem = new Intent();
            intentItem.setClass(CameraActivity.this, PhoneActivity.class);
            startActivity(intentItem);
            CameraActivity.this.finish();
        } else if (id == R.id.nav_camera) {

        } else if (id == R.id.nav_book) {
            Intent intentItem = new Intent();
            intentItem.setClass(CameraActivity.this, BookActivity.class);
            startActivity(intentItem);
            CameraActivity.this.finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_camera);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
