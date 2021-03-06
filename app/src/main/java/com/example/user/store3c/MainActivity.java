package com.example.user.store3c;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.GridLayoutManager;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Slide1Fragment.OnFragmentInteractionListener,
        Slide2Fragment.OnFragmentInteractionListener, Slide3Fragment.OnFragmentInteractionListener,
        Slide4Fragment.OnFragmentInteractionListener, Slide5Fragment.OnFragmentInteractionListener,
        View.OnClickListener{

    private static ArrayList<ProductItem> ProductData = new ArrayList<>();
    private static FirebaseDatabase db = null;
    private static ArrayList<Integer> picShowIndex = new ArrayList<> ();
    private static ProgressDialog dialog;
    private static int dishProductAmount = 0;
    private static int Reload = 0;
    private static DatabaseReference dishRef;
    private static StorageReference mStorageRef;
    private static ArrayList<StorageReference> imagesRef = new ArrayList<> ();
    private static ArrayList<String> picListName = new ArrayList<> ();
    private static ArrayList<Bitmap> picListImg = new ArrayList<> ();
    private static ArrayList<String> picListPrice = new ArrayList<> ();
    private static ArrayList<String> picListIntro = new ArrayList<> ();
    private static ArrayList<Integer> picListIndex = new ArrayList<> ();
    private static final long ONE_MEGABYTE = 1024 * 1024;
    private static final int DISH_SHOW_COUNT = 5;
    private static int dishProductImgCount = 0, dishProductPriceCount = 0;
    private static int dishProductNameCount = 0, dishProductIntroCount = 0, dishProductShowCount = 0;
    private static int threadComplete = 0;
    private static Handler2 handlerDownload2 = new Handler2();
    private static Handler3 handlerDownload3 = new Handler3();
    private static Handler4 handlerDownload4;
    private static Handler5 handlerDownload5 = new Handler5();
    private static Handler6 handlerDownload6 = new Handler6();
    private static Handler7 handlerDownload7 = new Handler7();

    private DishAdapter dishAdapter = null;
    private ImageView logoImage;
    private NavigationView navigationView;
    private String dbUserEmail, dbUserPassword;
    private byte[] dbUserPicture;
    private Context mContext;
    //private String versionName;
    private int versionCode = 0;
    private Handler1 handlerDownload1 = new Handler1();
    private AccountDbAdapter dbHelper = null;

    public static FirebaseAuth mAuth = null;
    public static ArrayList<Bitmap> picShowImg = new ArrayList<> ();
    public static volatile int adapterLayout = 0;
    public static volatile int returnApp = 0, appRntTimer = 0;
    public static volatile int TimerThread = 1;
    public static List<Fragment> fragments;
    public static boolean setFirebaseDbPersistence;
    public static UserHandler userAdHandler;

    public static Bitmap userImg = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RecyclerView dishRecyclerView;
        GridLayoutManager layoutManager;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            returnApp = 0;
            appRntTimer = 0;
            navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            navigationView.setItemIconTintList(null);
            //navigationView.setItemBackgroundResource(R.drawable.menu_icon_img);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setLogo(R.drawable.store_logo);
                //getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

            //label.font = [UIFont fontWithName:@"Arial" size:50];
            if (navigationView.getHeaderCount() > 0) {
                View header = navigationView.getHeaderView(0);
                logoImage = header.findViewById(R.id.logoImage_id);
                dbHelper = new AccountDbAdapter(this);
                try {
                    if (!dbHelper.IsDbUserEmpty()) {
                        try {
                            if (userImg == null) {
                                ArrayList<Cursor> cursor = dbHelper.getFullUserData();
                                if (cursor.size() == 1) {
                                    dbUserPicture = cursor.get(0).getBlob(4);
                                }
                                else {
                                    int cursorSize = cursor.size(), num, length = 0, totalLength = 0;
                                    int remainLength = cursor.get(cursorSize - 2).getBlob(0).length;
                                    byte[] picture = new byte[1000000 * (cursorSize - 2) + remainLength];
                                    for (num = 0; num < cursorSize - 2; num++) {
                                        totalLength = totalLength + cursor.get(num).getBlob(0).length;
                                        System.arraycopy(cursor.get(num).getBlob(0), 0, picture, length, cursor.get(num).getBlob(0).length);
                                        length = length + cursor.get(num).getBlob(0).length;
                                    }
                                    totalLength = totalLength + cursor.get(num).getBlob(0).length;
                                    System.arraycopy(cursor.get(num).getBlob(0), 0, picture, length, cursor.get(num).getBlob(0).length);
                                    //picture[picture.length - 1] = '\0';
                                    if (totalLength == picture.length) {
                                        Toast.makeText(MainActivity.this, "picture size: " + totalLength, Toast.LENGTH_LONG).show();
                                    }
                                    dbUserPicture = picture;
                                    //Toast.makeText(MainActivity.this, "picture size: " + dbUserPicture.length, Toast.LENGTH_LONG).show();
                                }
                                userImg = BitmapFactory.decodeByteArray(dbUserPicture, 0, dbUserPicture.length);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "db get error", Toast.LENGTH_SHORT).show();
                        }
                        if (userImg != null) {
                            try {
                                //logoImage.setLayoutParams(new LinearLayout.LayoutParams(80, 80));
                                //userImg = BitmapFactory.decodeByteArray(dbUserPicture, 0, dbUserPicture.length);
                                logoImage.setImageBitmap(userImg);
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, "decodeByteArray error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "image get error", Toast.LENGTH_SHORT).show();
                }
            }

            if (ProductData.size() == 0) {
                setFirebaseDbPersistence = false;
                dishRecyclerView = findViewById(R.id.dishRecyclerView_id);
                dishAdapter = new DishAdapter(ProductData, this, "DISH");
                layoutManager = new GridLayoutManager(this, 2);
                layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return position == 0 ? 2 : 1;
                    }
                });
                dishRecyclerView.setLayoutManager(layoutManager);
                dishRecyclerView.setAdapter(dishAdapter);
                if (InternetConnection.checkConnection(MainActivity.this)) {
                    mAuth = FirebaseAuth.getInstance();
                    final FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser == null) {
                        mAuth.signInAnonymously()
                                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.i("Anonymously===>", "signInAnonymously:success");
                                            Log.i("UserId===> ", "current User Id: "+mAuth.getCurrentUser().getUid());
                                            setUserAccountText();
                                            checkVersion();
                                            startDownload();
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.i("Anonymously===>", "signInAnonymously:failure", task.getException());
                                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        if (dbHelper == null) {
                            dbHelper = new AccountDbAdapter(this);
                        }
                        if (!dbHelper.IsDbUserEmpty()) {
                            try {
                                Cursor cursor = dbHelper.getSimpleUserData();
                                //index = cursor.getInt(0));
                                //dbUserName = cursor.getString(1);
                                dbUserEmail = cursor.getString(2);
                                dbUserPassword = cursor.getString(3);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            mAuth.createUserWithEmailAndPassword(dbUserEmail, dbUserPassword)
                                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                Log.i("CreateUserAccount===>", "Create User Account: success");
                                                final FirebaseUser currentUser = mAuth.getCurrentUser();
                                                currentUser.delete()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.i("User Account===>", "User account: delete");
                                                                    //Toast.makeText(UserActivity.this, "資料已刪除 !", Toast.LENGTH_LONG).show();
                                                                    mAuth.signInAnonymously()
                                                                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        // Sign in success, update UI with the signed-in user's information
                                                                                        Log.i("Anonymously===>", "signInAnonymously:success");
                                                                                        setUserAccountText();
                                                                                        checkVersion();
                                                                                        startDownload();
                                                                                    } else {
                                                                                        // If sign in fails, display a message to the user.
                                                                                        Log.i("Anonymously===>", "signInAnonymously:failure", task.getException());
                                                                                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                                                                                Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });

                                                                } else {
                                                                    //Toast.makeText(UserActivity.this, "登入已久, 需重新登入, 再執行刪除.", Toast.LENGTH_LONG).show();
                                                                    AuthCredential credential = EmailAuthProvider.getCredential(dbUserEmail, dbUserPassword);

                                                                    // Prompt the user to re-provide their sign-in credentials
                                                                    currentUser.reauthenticate(credential)
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        Log.i("Re-authenticated===>", "User re-authenticated: Success");
                                                                                        //Toast.makeText(UserActivity.this, "已自動重新登入成功, 可再執行刪除 !", Toast.LENGTH_LONG).show();
                                                                                        currentUser.delete()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if (task.isSuccessful()) {
                                                                                                            Log.i("User Account===>", "User account: delete");
                                                                                                            //Toast.makeText(UserActivity.this, "資料已刪除 !", Toast.LENGTH_LONG).show();
                                                                                                            mAuth.signInAnonymously()
                                                                                                                    .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                                                                                                        @Override
                                                                                                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                                                                                                            if (task.isSuccessful()) {
                                                                                                                                // Sign in success, update UI with the signed-in user's information
                                                                                                                                Log.i("Anonymously===>", "signInAnonymously:success");
                                                                                                                                setUserAccountText();
                                                                                                                                checkVersion();
                                                                                                                                startDownload();
                                                                                                                            } else {
                                                                                                                                // If sign in fails, display a message to the user.
                                                                                                                                Log.i("Anonymously===>", "signInAnonymously:failure", task.getException());
                                                                                                                                Toast.makeText(MainActivity.this, "Authentication failed.",
                                                                                                                                        Toast.LENGTH_SHORT).show();
                                                                                                                            }
                                                                                                                        }
                                                                                                                    });
                                                                                                        } else {
                                                                                                            Log.i("check User Account===>", "User account: delete fail");
                                                                                                            Toast.makeText(MainActivity.this, "測試資料未刪除 !", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                    else {
                                                                                        Toast.makeText(MainActivity.this, "User re-authenticated fail.", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });

                                            } else {
                                                Log.i("CreateUserAccount===>", "Create User Account: failure", task.getException());
                                                //Toast.makeText(UserActivity.this, "Create User Account: failure !", Toast.LENGTH_SHORT).show();
                                                setUserAccountText();
                                                checkVersion();
                                                startDownload();
                                            }
                                        }
                                    });
                        }
                        else {
                            setUserAccountText();
                            checkVersion();
                            startDownload();
                        }

                    }
                } else {
                    Toast.makeText(MainActivity.this, "網路未連線! ", Toast.LENGTH_SHORT).show();
                }

            } else {
                adapterLayout = 1;
                setUserAccountText();

                dishRecyclerView = findViewById(R.id.dishRecyclerView_id);
                dishAdapter = new DishAdapter(ProductData, this, "DISH");
                layoutManager = new GridLayoutManager(this, 2);
                layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return position == 0 ? 2 : 1;
                    }
                });
                dishRecyclerView.setLayoutManager(layoutManager);
                dishRecyclerView.setAdapter(dishAdapter);
            }
            //RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
            //dishRecyclerView.addItemDecoration(itemDecoration);

            logoImage.setOnClickListener(this);
        } catch (Throwable e) {
            Toast.makeText(MainActivity.this, "error message:  " + e.getClass().getName(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig){

        DishAdapter.screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        dishAdapter.notifyDataSetChanged();
/*
        if((newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
                newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) ){
            //Toast.makeText(MainActivity.this, "The orientation changed to vertical!", Toast.LENGTH_SHORT).show();
        }
        else {
            //Toast.makeText(MainActivity.this, "The orientation changed to horizontal!", Toast.LENGTH_SHORT).show();
        }
   */
        Log.v("===>","ORIENTATION");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.logoImage_id) {
            Intent intentItem = new Intent();
            Bundle bundleItem = new Bundle();
            bundleItem.putString("Menu", "DISH");
            intentItem.putExtras(bundleItem);
            intentItem.setClass(MainActivity.this,UserActivity.class);
            TimerThread = 0;
            startActivity(intentItem);
            MainActivity.this.finish();
        }
    }

    public void checkVersion() {
        mContext = MainActivity.this;
        DatabaseReference versionCodeRef;
        final DatabaseReference versionNameRef;

        if (db == null) {
            db = FirebaseDatabase.getInstance();
        }
        try {
            if (!setFirebaseDbPersistence) {
                setFirebaseDbPersistence = true;
                db.setPersistenceEnabled(true);
            }
        }
        catch (Exception e) {
            Toast.makeText(MainActivity.this, "Pick image timeout, reload data.", Toast.LENGTH_SHORT).show();
            Log.i("Pick image timeout: " , "reload data.");
        }
        versionCodeRef = db.getReference("versionNumber");
        versionNameRef = db.getReference("versionName");
        versionCodeRef.keepSynced(true);
        versionNameRef.keepSynced(true);
        // Read from the database
        versionCodeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Long versionValue = dataSnapshot.getValue(Long.class);
                Log.i("Firebase ==>", "VersionNumber is: " + versionValue);
                try {
                    PackageManager pm = mContext.getPackageManager();
                    PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);
                    //versionName = pi.versionName;
                    versionCode = pi.versionCode;
                    if (versionValue != null) {
                        if (versionValue.intValue() > versionCode) {
                            versionNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    // This method is called once with the initial value and again
                                    // whenever data at this location is updated.
                                    String versionName = dataSnapshot.getValue(String.class);
                                    Log.i("Firebase ==>", "VersionName is: " + versionName);
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("版本更新")
                                            .setIcon(R.drawable.refresh)
                                            .setMessage("可上網下載新版本: " + versionName)
                                            .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    try {
                                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://apptech.website"));
                                                        startActivity(browserIntent);
                                                    } catch (Throwable e) {
                                                        Toast.makeText(MainActivity.this, "Open webbrowser fail." + e.getClass().toString(), Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            })
                                            .setNegativeButton("稍後下載", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                }
                                            })
                                            .show();
                                    //Toast.makeText(MainActivity.this, "Has new version on website: " + versionName, Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    // Failed to read value
                                    Log.i("Firebase ==>", "Failed to read version.", error.toException());
                                    Toast.makeText(MainActivity.this, "DatabaseError, versionNameRef: " + error.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });

                        }
                    }
                } catch (Throwable e) {
                    Toast.makeText(MainActivity.this, "error message:  " + e.getClass().getName(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.i("Firebase ==>", "Failed to read version.", error.toException());
                Toast.makeText(MainActivity.this, "DatabaseError, versionCodeRef: " + error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void startDownload() {
        if (db == null) {
            db = FirebaseDatabase.getInstance();
        }
        try {
            if (!setFirebaseDbPersistence) {
                setFirebaseDbPersistence = true;
                db.setPersistenceEnabled(true);
            }
        }
        catch (Exception e) {
            Toast.makeText(MainActivity.this, "Pick image timeout, reload data.", Toast.LENGTH_SHORT).show();
            Log.i("Pick image timeout: " , "reload data.");
        }
        dishRef = db.getReference("dish");
        dishRef.keepSynced(true);
        handlerDownload4 = new Handler4(MainActivity.this, dishAdapter);
        new Thread(runnable1).start();
        dialog = new ProgressDialog(MainActivity.this);
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
        String user, usrAccount = "User Account";

        if (InternetConnection.checkConnection(MainActivity.this)) {
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
                    userName.setText(usrAccount);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*try {
            if (InternetConnection.checkConnection(MainActivity.this)) {
                // Check if user is signed in (non-null) and update UI accordingly.
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
            Toast.makeText(MainActivity.this, "currentUser: " +
                            (currentUser.isAnonymous() ? "Anonymous" : currentUser.getEmail()),
                            Toast.LENGTH_LONG).show();
                }
            }
        } catch (Throwable e) {
            Toast.makeText(MainActivity.this, "error message:  " + e.getClass().getName(), Toast.LENGTH_LONG).show();
        }*/
        FirebaseMessaging.getInstance().subscribeToTopic("store3c");
    }

    @Override
    protected void onDestroy() {
        TimerThread = 0;
        if (userAdHandler != null) {
            userAdHandler.removeCallbacksAndMessages(null);
        }
        if (DishAdapter.userAdAdapterHandler != null) {
            DishAdapter.userAdAdapterHandler.removeCallbacksAndMessages(null);
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }

    static class Handler1 extends Handler {
        @Override
        public void handleMessage(Message msg1) {
            super.handleMessage(msg1);

            Long messageTransaction = (Long)msg1.obj;
            dishProductAmount = messageTransaction.intValue();
            Log.i("Firebase ==>", "Download Amount Complete: " + messageTransaction);
            new Thread(runnable2).start();
            new Thread(runnable3).start();
            new Thread(runnable5).start();
            new Thread(runnable6).start();
            new Thread(runnable7).start();
        }

    }

    Runnable runnable1 = new Runnable() {
        @Override
        public void run() {
            DatabaseReference dishAmount;
            dishAmount = db.getReference("dishAmount");
            dishAmount.keepSynced(true);

            // TODO: http request.
            // Read from the database
            dishAmount.addValueEventListener(new ValueEventListener() {
                //String messageLoad = "Download Amount Complete";
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    Long value = dataSnapshot.getValue(Long.class);

                    //Log.i("Firebase ==>", "Amount is: " + value);
                    Message msg1 = new Message();
                    msg1.obj = value;
                    handlerDownload1.sendMessage(msg1);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Failed to read value
                    Log.i("Firebase ==>", "Failed to read amount.", error.toException());
                    Toast.makeText(MainActivity.this, "DatabaseError, dishAmount: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    };

    static class Handler2 extends Handler {
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
            DatabaseReference dishName;
            dishName = dishRef.child("dishName").getRef();
            dishName.keepSynced(true);
            dishName.addChildEventListener(new ChildEventListener() {
                String messageLoad = "Download Product Name Complete";
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    String dishProductName = dataSnapshot.getValue(String.class);
                    picListName.add(dishProductName);
                    dishProductNameCount++;
                    //Log.i("Firebase ==>", "dish name is: " + dishProductName);
                    if (dishProductNameCount == dishProductAmount) {
                        Message msg2_1 = new Message();
                        Message msg2_2 = new Message();
                        msg2_1.obj = messageLoad;
                        msg2_2.obj = messageLoad;
                        dishProductNameCount = 0;
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
                    //Toast.makeText(staticMainActivity, "DatabaseError, dishName: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    };

    static class Handler3 extends Handler {
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
            mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://store3c-137123.appspot.com");
            DatabaseReference dishImgName;
            dishImgName = dishRef.child("dishImgName").getRef();
            dishImgName.keepSynced(true);
            dishImgName.addChildEventListener(new ChildEventListener() {
                String messageLoad = "Download Image Name Complete";
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    String ImgFileName = dataSnapshot.getValue(String.class);
                    dishProductImgCount++;
                    String dishImageName = "dish/" + ImgFileName;
                    imagesRef.add(mStorageRef.child(dishImageName));
                    //Log.i("Firebase ==>", "dish image name is: " + ImgFileName);

                    if (dishProductImgCount == dishProductAmount) {
                        Message msg3 = new Message();
                        msg3.obj = messageLoad;
                        dishProductImgCount = 0;
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
                    //Toast.makeText(staticMainActivity, "DatabaseError, dishImgName: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    };

    static class Handler4 extends Handler {
        private WeakReference<MainActivity> weakRefActivity;
        private WeakReference<DishAdapter> weakRefDishAdapter;

        Handler4(MainActivity hActivity, DishAdapter hDishAdapter) {
            weakRefActivity = new WeakReference<>(hActivity);
            weakRefDishAdapter = new WeakReference<>(hDishAdapter);
        }

        @Override
        public void handleMessage(Message msg4) {
            super.handleMessage(msg4);

            String messageTransaction = (String)msg4.obj;
            //Log.i("Firebase thread  ==>", messageTransaction);
            if (messageTransaction.equals("Download Image file Complete")) {
                dishProductImgCount++;
                if (dishProductImgCount == dishProductAmount) {
                    RangeImgSequence();
                    threadComplete++;
                }
            }
            else {
                threadComplete++;
            }
            if (threadComplete == 5) {
                if (Reload == 0) {
                    for (int i = 0; i < dishProductAmount; i++) {
                        ProductData.add(new ProductItem(picListImg.get(i), picListName.get(i), picListPrice.get(i), picListIntro.get(i)));
                    }
                    for (int j = 0; j < DISH_SHOW_COUNT; j++) {
                        picShowImg.add(ProductData.get(picShowIndex.get(j)).getImg());
                    }
                    Reload = 1;
                }
                MainActivity hmActivity = weakRefActivity.get();
                if (hmActivity != null) {
                    iniUpperPage(hmActivity);
                }
                DishAdapter hmDishAdapter = weakRefDishAdapter.get();
                if (hmDishAdapter != null) {
                    hmDishAdapter.notifyDataSetChanged();
                }
                dialog.dismiss();
            }
        }

        void RangeImgSequence() {
            ArrayList<Bitmap> picListImg1 = new ArrayList<> ();

            for (int i=0; i<dishProductAmount; i++) {
                for (int j=0; j<dishProductAmount; j++) {
                    if (i == picListIndex.get(j)) {
                        picListImg1.add(picListImg.get(j));
                    }
                }
            }
            picListImg.subList(0, dishProductAmount).clear();
            for (int n=0; n<dishProductAmount; n++) {
                picListImg.add(picListImg1.get(n));
            }

        }

    }

    static Runnable runnable4 = new Runnable() {
        String messageLoad = "Download Image file Complete";
        int indexImg = 0;
        @Override
        public void run() {
            // TODO: http request.

            for (indexImg=0; indexImg < dishProductAmount; indexImg++) {

                imagesRef.get(indexImg).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    int indexLoad = indexImg;
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Data for "images/island.jpg" is returns, use this as needed

                        if (bytes.length != 0) {
                            picListImg.add(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                            picListIndex.add(indexLoad);
                        }
                        //Log.i("Firebase ==>", "Download image file success.");
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

    static class Handler5 extends Handler {
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
            DatabaseReference dishPrice;
            dishPrice = dishRef.child("dishPrice").getRef();
            dishPrice.keepSynced(true);
            dishPrice.addChildEventListener(new ChildEventListener() {
                String messageLoad = "Download Product Price Complete";
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    String dishPrice = dataSnapshot.getValue(String.class);
                    picListPrice.add(dishPrice);
                    dishProductPriceCount++;
                    //Log.i("Firebase ==>", "dish price is: " + dishPrice);
                    if (dishProductPriceCount == dishProductAmount) {
                        Message msg5_1 = new Message();
                        Message msg5_2 = new Message();
                        msg5_1.obj = messageLoad;
                        msg5_2.obj = messageLoad;
                        dishProductPriceCount = 0;
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
                    //Toast.makeText(staticMainActivity, "DatabaseError, dishPrice: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    };

    static class Handler6 extends Handler {
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
            DatabaseReference dishIntro;
            dishIntro = dishRef.child("dishIntro").getRef();
            dishIntro.keepSynced(true);
            dishIntro.addChildEventListener(new ChildEventListener() {
                String messageLoad = "Download Product Introduction Complete";
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    String dishIntro = dataSnapshot.getValue(String.class);
                    picListIntro.add(dishIntro);
                    dishProductIntroCount++;
                    //Log.i("Firebase ==>", "dish introduction is: " + dishIntro);
                    if (dishProductIntroCount == dishProductAmount) {
                        Message msg6_1 = new Message();
                        Message msg6_2 = new Message();
                        msg6_1.obj = messageLoad;
                        msg6_2.obj = messageLoad;
                        dishProductIntroCount = 0;
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
                    //Toast.makeText(staticMainActivity, "DatabaseError, dishIntro: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    };

    static class Handler7 extends Handler {
        @Override
        public void handleMessage(Message msg7) {
            super.handleMessage(msg7);

            String messageTransaction = (String)msg7.obj;
            Log.i("Firebase ==>", messageTransaction);

        }
    }

    static Runnable runnable7 = new Runnable() {

        @Override
        public void run() {
            // TODO: http request.
            DatabaseReference dishShowId;
            dishShowId = dishRef.child("dishShowId").getRef();
            dishShowId.keepSynced(true);
            dishShowId.addChildEventListener(new ChildEventListener() {
                String messageLoad = "Download Showing Product index Complete";
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    Long showIndex = dataSnapshot.getValue(Long.class);
                    if (showIndex != null) {
                        picShowIndex.add(showIndex.intValue());
                    }
                    dishProductShowCount++;
                    //Log.i("Firebase ==>", "show dish index is: " + showIndex);
                    if (dishProductShowCount == DISH_SHOW_COUNT) {
                        Message msg7_1 = new Message();
                        Message msg7_2 = new Message();
                        msg7_1.obj = messageLoad;
                        msg7_2.obj = messageLoad;
                        dishProductShowCount = 0;
                        handlerDownload7.sendMessage(msg7_1);
                        handlerDownload4.sendMessage(msg7_2);
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
                    //Toast.makeText(staticMainActivity, "DatabaseError, dishShowId: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    };


    private static void iniUpperPage(MainActivity activity) {
        /*fragments = new Vector<>();
        fragments.add(Fragment.instantiate(this, Slide1Fragment.class.getName()));
        fragments.add(Fragment.instantiate(this, Slide2Fragment.class.getName()));
        fragments.add(Fragment.instantiate(this, Slide3Fragment.class.getName()));
        fragments.add(Fragment.instantiate(this, Slide4Fragment.class.getName()));
        fragments.add(Fragment.instantiate(this, Slide5Fragment.class.getName()));*/
        PagerAdapter mPagerAdapter;
        ViewPager pager;

        fragments = new Vector<>();
        fragments.add(Slide1Fragment.newInstance(Bitmap2Bytes(picShowImg.get(0)), "frame1"));
        fragments.add(Slide2Fragment.newInstance(Bitmap2Bytes(picShowImg.get(1)), "frame2"));
        fragments.add(Slide3Fragment.newInstance(Bitmap2Bytes(picShowImg.get(2)), "frame3"));
        fragments.add(Slide4Fragment.newInstance(Bitmap2Bytes(picShowImg.get(3)), "frame4"));
        fragments.add(Slide5Fragment.newInstance(Bitmap2Bytes(picShowImg.get(4)), "frame5"));

        pager = activity.findViewById(R.id.viewPager_id);
        mPagerAdapter = new PageAdapter(activity.getSupportFragmentManager(), fragments);
        pager.setAdapter(mPagerAdapter);
        userAdHandler = new UserHandler(pager);
        UserTimerThread adTimerThread = new UserTimerThread(activity);
        TimerThread = 1;
        adTimerThread.start();

        //pager.setCurrentItem(0);
        // pager.setOnClickListener(getActivity().this);
        final ImageView dot1, dot2, dot3, dot4, dot5;
        dot1 = activity.findViewById(R.id.imgIcon1_id);
        dot2 = activity.findViewById(R.id.imgIcon2_id);
        dot3 = activity.findViewById(R.id.imgIcon3_id);
        dot4 = activity.findViewById(R.id.imgIcon4_id);
        dot5 = activity.findViewById(R.id.imgIcon5_id);

        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                //invalidateOptionsMenu();
                switch (position) {
                    case 0:
                        dot1.setImageResource(R.drawable.dot1);
                        dot2.setImageResource(R.drawable.dot2);
                        dot3.setImageResource(R.drawable.dot2);
                        dot4.setImageResource(R.drawable.dot2);
                        dot5.setImageResource(R.drawable.dot2);
                        break;
                    case 1:
                        dot1.setImageResource(R.drawable.dot2);
                        dot2.setImageResource(R.drawable.dot1);
                        dot3.setImageResource(R.drawable.dot2);
                        dot4.setImageResource(R.drawable.dot2);
                        dot5.setImageResource(R.drawable.dot2);
                        break;
                    case 2:
                        dot1.setImageResource(R.drawable.dot2);
                        dot2.setImageResource(R.drawable.dot2);
                        dot3.setImageResource(R.drawable.dot1);
                        dot4.setImageResource(R.drawable.dot2);
                        dot5.setImageResource(R.drawable.dot2);
                        break;
                    case 3:
                        dot1.setImageResource(R.drawable.dot2);
                        dot2.setImageResource(R.drawable.dot2);
                        dot3.setImageResource(R.drawable.dot2);
                        dot4.setImageResource(R.drawable.dot1);
                        dot5.setImageResource(R.drawable.dot2);
                        break;
                    case 4:
                        dot1.setImageResource(R.drawable.dot2);
                        dot2.setImageResource(R.drawable.dot2);
                        dot3.setImageResource(R.drawable.dot2);
                        dot4.setImageResource(R.drawable.dot2);
                        dot5.setImageResource(R.drawable.dot1);
                        break;
                }
                //Toast.makeText(MainActivity.this, "The top fragment "+ position, Toast.LENGTH_SHORT).show();
            }

        });

    }

    public static class UserHandler extends Handler {
        private WeakReference<ViewPager> weakRefPager;

        UserHandler (ViewPager hPager) {
            weakRefPager = new WeakReference<>(hPager);
        }

        @Override
        public void handleMessage(Message msg) {
            appRntTimer = msg.what;
            ViewPager hmPager = weakRefPager.get();
            if (hmPager != null) {
                switch (msg.what) {
                    case 0:
                        //Toast.makeText(MainActivity.this, "The picture number is 1" , Toast.LENGTH_SHORT).show();
                        hmPager.setCurrentItem(0);
                        break;
                    case 1:
                        //Toast.makeText(MainActivity.this, "The picture number is 2" , Toast.LENGTH_SHORT).show();
                        hmPager.setCurrentItem(1);
                        break;
                    case 2:
                        //Toast.makeText(MainActivity.this, "The picture number is 3" , Toast.LENGTH_SHORT).show();
                        hmPager.setCurrentItem(2);
                        break;
                    case 3:
                        //Toast.makeText(MainActivity.this, "The picture number is 3" , Toast.LENGTH_SHORT).show();
                        hmPager.setCurrentItem(3);
                        break;
                    case 4:
                        //Toast.makeText(MainActivity.this, "The picture number is 3" , Toast.LENGTH_SHORT).show();
                        hmPager.setCurrentItem(4);
                        break;
                }
                if (appRntTimer == (returnApp % 5)) {
                    returnApp = 0;
                }
            }

            super.handleMessage(msg);
        }
    }

    public void onFragmentInteraction(int imgId) {
        Intent intentItem = new Intent();
        Bundle bundle = new Bundle();

        int position = picShowIndex.get(imgId-1);
        if ((position > -1) && (position < dishProductAmount)) {
            bundle.putString("Menu", "DISH");
            bundle.putByteArray("Pic", Bitmap2Bytes(ProductData.get(position).getImg()));
            bundle.putString("Name", ProductData.get(position).getName());
            bundle.putString("Price", ProductData.get(position).getPrice());
            bundle.putString("Intro", ProductData.get(position).getIntro());

            intentItem.putExtras(bundle);
            intentItem.setClass(MainActivity.this, ProductActivity.class);
            TimerThread = 0;
            startActivity(intentItem);
            MainActivity.this.finish();
        }
        else {
            Toast.makeText(MainActivity.this, "The fragment image is not correct: " + position, Toast.LENGTH_SHORT).show();
        }
    }

    private static byte [] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos =  new  ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,  100 , baos);
        return  baos.toByteArray();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            if (returnApp == 0) {
                returnApp = appRntTimer + 1;
                Toast.makeText(this.getBaseContext(), "再按一次, 可退出3C生活百貨! ", Toast.LENGTH_LONG).show();
            } else {
                TimerThread = 0;
                MainActivity.this.finish();
                //super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        if (InternetConnection.checkConnection(MainActivity.this)) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            menu.add(0, R.id.action_login_status,100, "使用者");
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        //menu.findItem(R.id.action_login_status).setIconTintList(Color.BLUE);
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
                bundleItem.putString("Menu", "DISH");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(MainActivity.this, PositionActivity.class);
                TimerThread = 0;
                startActivity(intentItem);
                MainActivity.this.finish();
                //Toast.makeText(this.getBaseContext(),"The setting item", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_user_account:
                intentItem = new Intent();
                bundleItem = new Bundle();
                bundleItem.putString("Menu", "DISH");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(MainActivity.this,UserActivity.class);
                TimerThread = 0;
                startActivity(intentItem);
                MainActivity.this.finish();
                //  Toast.makeText(this.getBaseContext(),"The user account item", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_login_status:
                if (InternetConnection.checkConnection(MainActivity.this)) {
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser == null || currentUser.isAnonymous()) {
                        intentItem = new Intent();
                        bundleItem = new Bundle();
                        bundleItem.putString("Menu", "DISH");
                        intentItem.putExtras(bundleItem);
                        intentItem.setClass(MainActivity.this, LoginActivity.class);
                        TimerThread = 0;
                        startActivity(intentItem);
                        MainActivity.this.finish();
                    } else {
                        mAuth.signOut();
                        Toast.makeText(MainActivity.this, "登出成功 ! ", Toast.LENGTH_SHORT).show();
                        setUserAccountText();
                        item.setTitle("使用者登入");
                    }
                } else {
                    Toast.makeText(MainActivity.this, "請先開啟網路連線 ! ", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_memo:
                intentItem = new Intent();
                bundleItem = new Bundle();
                bundleItem.putString("Menu", "DISH");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(MainActivity.this, MemoActivity.class);
                TimerThread = 0;
                startActivity(intentItem);
                MainActivity.this.finish();
                //Toast.makeText(this.getBaseContext(),"The setting item", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_shopping_car:
                intentItem = new Intent();
                bundleItem = new Bundle();
                bundleItem.putString("Menu", "DISH");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(MainActivity.this, OrderActivity.class);
                startActivity(intentItem);
                MainActivity.this.finish();
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
            // Handle the dish action
            //menuItem.findItem(R.id.nav_dish).setIconTintList(Color.BLUE);
        } else if (id == R.id.nav_cake) {
            Intent intentItem = new Intent();
            intentItem.setClass(MainActivity.this, CakeActivity.class);
            TimerThread = 0;
            startActivity(intentItem);
            MainActivity.this.finish();
        } else if (id == R.id.nav_phone) {
            Intent intentItem = new Intent();
            intentItem.setClass(MainActivity.this, PhoneActivity.class);
            TimerThread = 0;
            startActivity(intentItem);
            MainActivity.this.finish();
        } else if (id == R.id.nav_camera) {
            Intent intentItem = new Intent();
            intentItem.setClass(MainActivity.this, CameraActivity.class);
            TimerThread = 0;
            startActivity(intentItem);
            MainActivity.this.finish();
        } else if (id == R.id.nav_book) {
            Intent intentItem = new Intent();
            intentItem.setClass(MainActivity.this, BookActivity.class);
            TimerThread = 0;
            startActivity(intentItem);
            MainActivity.this.finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
