package com.example.user.store3c;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ApplicationExitInfo;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
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
import androidx.viewpager2.widget.ViewPager2;

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
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.app.ActivityManager.getMyMemoryState;
import static android.app.ActivityManager.isLowMemoryKillReportSupported;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;

@Keep
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Slide1Fragment.OnFragmentInteractionListener,
        Slide2Fragment.OnFragmentInteractionListener, Slide3Fragment.OnFragmentInteractionListener,
        Slide4Fragment.OnFragmentInteractionListener, Slide5Fragment.OnFragmentInteractionListener,
        View.OnClickListener, ComponentCallbacks2 {

    private static final ArrayList<ProductItem> ProductData = new ArrayList<>();
    private static FirebaseDatabase db = null;
    private static final ArrayList<Integer> picShowIndex = new ArrayList<> ();
    private static int dishProductAmount = 0;
    private static int Reload = 0;
    private static DatabaseReference dishRef;
    private static StorageReference mStorageRef;
    private static final ArrayList<StorageReference> imagesRef = new ArrayList<> ();
    private static final ArrayList<String> picListName = new ArrayList<> ();
    private static final ArrayList<Bitmap> picListImg = new ArrayList<> ();
    private static final ArrayList<String> picListPrice = new ArrayList<> ();
    private static final ArrayList<String> picListIntro = new ArrayList<> ();
    private static final ArrayList<Integer> picListIndex = new ArrayList<> ();
    private static final long ONE_MEGABYTE = 1024 * 1024;
    private static final int DISH_SHOW_COUNT = 5;
    private static int dishProductImgCount = 0, dishProductPriceCount = 0;
    private static int dishProductNameCount = 0, dishProductIntroCount = 0, dishProductShowCount = 0;
    private static int threadComplete = 0;
    private static final Handler2 handlerDownload2 = new Handler2();
    private static final Handler3 handlerDownload3 = new Handler3();
    private static Handler4 handlerDownload4;
    private static final Handler5 handlerDownload5 = new Handler5();
    private static final Handler6 handlerDownload6 = new Handler6();
    private static final Handler7 handlerDownload7 = new Handler7();

    public static FirebaseAuth mAuth = null;
    public static ArrayList<Bitmap> picShowImg = new ArrayList<> ();
    public static volatile int adapterLayout = 0;
    public static boolean setFirebaseDbPersistence;
    public static Bitmap userImg = null;
    public static boolean isTab;
    public static int rotationScreenWidth = 700;  // phone rotation width > 700 , Samsung A8 Tab width size: 800
    public static int rotationTabScreenWidth = 1000;  // Tab rotation width > 1000
    public AlertDialog dialog;

    private DishAdapter dishAdapter;
    private ImageView logoImage;
    private NavigationView navigationView;
    private String dbUserEmail, dbUserPassword;
    private Context mContext;
    private int versionCode = 0;
    private final Handler1 handlerDownload1 = new Handler1();
    private AccountDbAdapter dbHelper = null;
    private PageAdapter mPagerAdapter;
    private ViewPager2 pager;
    private ImageView dot1, dot2, dot3, dot4, dot5;
    private String retainRecentTask = null;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Bitmap productImage;
    private List<Fragment> fragments;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;
    private boolean systemClearTask = false;

    public String messageType, messageName,  messagePrice, messageIntro, messageImageUrl;
    public ActivityManager.AppTask currentTask = null;
    public boolean combinedActivity = false;
    public volatile int returnApp = 0, appRntTimer = 0;
    public volatile int TimerThread = 0;
    public UserHandler userAdHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        byte[] dbUserPicture;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView dishRecyclerView;
        GridLayoutManager layoutManager;
        Toolbar toolbar;
        DrawerLayout drawer;
        ActionBarDrawerToggle toggle;
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        int index = 0, DbMainActivityTaskId = -1;

        if (bundle != null) {       // firebase notification load App from system tray.
            messageType = bundle.getString("messageType");
            retainRecentTask = bundle.getString("RetainRecentTask");
            if (retainRecentTask != null) {
                messageType = "NotFirebaseMessage";
            }
            else {
                if (messageType == null) {      //No data payload.
                    messageType = "No-data-payload";
                }
            }
        }
        else {      //regular load MainActivity
            messageType = "NotFirebaseMessage";
        }

        if (dbHelper == null) {
            dbHelper = new AccountDbAdapter(this);
        }
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> tasks;
        ActivityManager.AppTask eachTask;

        try {
            synchronized (tasks = am.getAppTasks()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    for (ActivityManager.AppTask task : tasks) {
                        if (task.getTaskInfo() != null && task.getTaskInfo().persistentId == getTaskId()) {
                            currentTask = task;
                            if (currentTask.getTaskInfo().numActivities > 1) {
                                combinedActivity = true;            // MainActivity is not the root activity so to create a new task
                                Log.i("Number of activity: ", "===> " + currentTask.getTaskInfo().numActivities);
                            }
                        }
                    }
                } else {
                    if (messageType.equals("FCM-console") || messageType.equals("No-data-payload")) {
                        for (ActivityManager.AppTask task : tasks) {
                            if (task.getTaskInfo() != null && task.getTaskInfo().persistentId == getTaskId()) {
                                currentTask = task;
                                combinedActivity = true;        // always to create a new task
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Catch taskId: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.i("Get task Id error: ", "==>" + e.getMessage());       // the previous task removed
        }

        switch (messageType) {
            case "FCM-console":
                toolbar = findViewById(R.id.toolbarMain);
                setSupportActionBar(toolbar);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(false);   // user to wait for some process to finish,
                builder.setView(R.layout.loading_dialog);
                dialog = builder.create();
                dialog.show();

                if (bundle != null) {
                    messageName = bundle.getString("messageText");
                    messagePrice = bundle.getString("messagePrice");
                    messageIntro = bundle.getString("messageIntro");
                    messageImageUrl = bundle.getString("imagePath");
                } else {
                    messageName = "雞肉飯";
                    messagePrice = "80元";
                    messageIntro = "香噴噴的雞肉飯，吃了以後讓人再三的回味!";
                    messageImageUrl = "http://apptech.website/store3c/image/dish/d13.jpg";
                }

                executor.execute(() -> {
                    if (InternetConnection.checkConnection(MainActivity.this)) {
                        try {
                            String imageUrl = messageImageUrl;
                            URL url = new URL(imageUrl);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setDoInput(true);
                            connection.setConnectTimeout(60000);
                            connection.connect();
                            InputStream input = connection.getInputStream();
                            productImage = BitmapFactory.decodeStream(input);
                        } catch (Exception e) {
                            e.printStackTrace();
                            productImage = null;
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "網路未連線! ", Toast.LENGTH_SHORT).show();
                        productImage = null;
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (productImage == null) {
                                productImage = BitmapFactory.decodeResource(getResources(), R.drawable.store_icon);
                            }
                            Bundle bundleProduct = new Bundle();
                            Intent intentProduct = new Intent(MainActivity.this , ProductActivity.class);
                            if (productImage != null) {
                                bundleProduct.putByteArray("Pic", Bitmap2Bytes(productImage));
                            }
                            bundleProduct.putString("Name", messageName);
                            bundleProduct.putString("Price", messagePrice);
                            bundleProduct.putString("Intro", messageIntro);
                            bundleProduct.putString("Menu", "DISH");
                            bundleProduct.putString("Notification", "UPPER_APP");
                            bundleProduct.putString("Firebase", "DATA_PAYLOAD");
                            intentProduct.setClass(MainActivity.this , ProductActivity.class);
                            if (combinedActivity) {
                                bundleProduct.putString("RetainRecentTask", "RECENT_ACTIVITY");
                                intentProduct.putExtras(bundleProduct);
                                intentProduct.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                                dialog.dismiss();
                                startActivity(intentProduct);
                                currentTask.finishAndRemoveTask();
                            }
                            else {
                                intentProduct.putExtras(bundleProduct);
                                dialog.dismiss();
                                startActivity(intentProduct);
                                MainActivity.this.finish();
                            }
                        }
                    });
                });
                break;

            case "No-data-payload":
                if (combinedActivity) {
                    Intent reloadIntent = Intent.makeMainActivity (new ComponentName(getApplicationContext(), MainActivity.class));
                    Bundle reloadBundle = new Bundle();
                    reloadBundle.putString("RetainRecentTask", "RECENT_TASK");
                    reloadIntent.putExtras(reloadBundle);
                    reloadIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS);
                    startActivity(reloadIntent);
                    try {
                        if (currentTask != null) {
                            currentTask.finishAndRemoveTask();
                        } else {
                            this.finishAndRemoveTask();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                retainRecentTask = "RECENT_TASK";

            case "NotFirebaseMessage":
                try {
                    boolean isDbTaskIdListEmpty = dbHelper.IsDbTaskIdEmpty();
                    if (!isDbTaskIdListEmpty) {
                        try {
                            Cursor cursor = dbHelper.getTaskIdList();
                            index = cursor.getInt(0);
                            DbMainActivityTaskId = cursor.getInt(2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        synchronized (tasks = am.getAppTasks()) {
                            if (tasks.size() > 1) {
                                //Toast.makeText(MainActivity.this, "recentTaskId: " + recentTaskId, Toast.LENGTH_LONG).show();
                                for (int i = 0; i < tasks.size(); i++) {
                                    eachTask = tasks.get(i);
                                    if ((eachTask.getTaskInfo() != null) && (eachTask.getTaskInfo().persistentId == DbMainActivityTaskId) &&
                                            (eachTask.getTaskInfo().persistentId != getTaskId())) {
                                        if (!isDbTaskIdListEmpty) {
                                            if (dbHelper.updateRecentTaskId(index, -1) == 0) {
                                                Log.i("update recent task id: ", "no data change!");
                                            }
                                            if (dbHelper.updateMainActivityTaskId(index, -1) == 0) {
                                                Log.i("mainActivityTaskId: ", "update result, no data change!");
                                            }
                                        }
                                        eachTask.finishAndRemoveTask();
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Catch taskId: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.i("Get preTask Id error: ", "==>" + e.getMessage());
                    }
                    if (!isDbTaskIdListEmpty) {
                        if (dbHelper.updateMainActivityTaskId(index, getTaskId()) == 0) {
                            Log.i("mainActivityTaskId: ", "update result, no data change!");
                        }
                    }
                    else {
                        if (dbHelper.createTaskId(-1, getTaskId(), -1) == -1) {
                            Log.i("mainActivityTaskId: ", "create result, fail !");
                        }
                    }
                    toolbar = findViewById(R.id.toolbarMain);
                    setSupportActionBar(toolbar);
                    isTab = (getApplicationContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
                    if (isTab) {
                        rotationTabScreenWidth = Math.min(Resources.getSystem().getDisplayMetrics().widthPixels, Resources.getSystem().getDisplayMetrics().heightPixels);
                        //Toast.makeText(MainActivity.this, "screen size: " + Resources.getSystem().getDisplayMetrics().widthPixels, Toast.LENGTH_LONG).show();
                    }
                    else {
                        rotationScreenWidth = Math.min(Resources.getSystem().getDisplayMetrics().widthPixels, Resources.getSystem().getDisplayMetrics().heightPixels);
                    }

                    drawer = findViewById(R.id.drawer_layout_main);
                    toggle = new ActionBarDrawerToggle(
                            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
                    drawer.addDrawerListener(toggle);
                    toggle.syncState();

                    returnApp = 0;
                    appRntTimer = 0;
                    navigationView = findViewById(R.id.nav_view_main);
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
                                                //Toast.makeText(MainActivity.this, "picture size: " + totalLength, Toast.LENGTH_LONG).show();
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
                        adapterLayout = 0;
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
                        setUserAccountText();
                    }
                    //RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
                    //dishRecyclerView.addItemDecoration(itemDecoration);

                    logoImage.setOnClickListener(this);
                } catch (Throwable e) {
                    Toast.makeText(MainActivity.this, "NotFirebaseMessage error message:  " + e.getClass().getName(), Toast.LENGTH_LONG).show();
                }
                break;
        }

    }

    @SuppressLint("NotifyDataSetChanged")
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
            Toast.makeText(MainActivity.this, "Reload data.", Toast.LENGTH_SHORT).show();
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
                                                        Toast.makeText(MainActivity.this, "Open web browser fail." + e.getClass().toString(), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(MainActivity.this, "CheckVersion error message:  " + e.getClass().getName(), Toast.LENGTH_LONG).show();
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
        handlerDownload4 = new Handler4(MainActivity.this, dishAdapter, getLifecycle());
        new Thread(runnable1).start();
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);   // user to wait for some process to finish,
        builder.setView(R.layout.loading_dialog);
        dialog = builder.create();
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseMessaging.getInstance().subscribeToTopic("store3c").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String msg = "Subscribe success";
                if (!task.isSuccessful()) {
                    msg = "Subscribe Topic failed";
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
                Log.i("Subscribe Topic: ", msg);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onTrimMemory(int level) {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> tasks = am.getAppTasks();
        ActivityManager.AppTask currentTask, eachTask;
        currentTask = tasks.get(0);

        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                // Toast.makeText(this, "MainActivity: UI_HIDDEN !", Toast.LENGTH_SHORT).show();
                ActivityManager.RunningAppProcessInfo appInfo = new ActivityManager.RunningAppProcessInfo();
                ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();

                getMyMemoryState(appInfo);
         //       Toast.makeText(this, "MainActivity TrimLevel: " + appInfo.lastTrimLevel, Toast.LENGTH_SHORT).show();
                am.getMemoryInfo(outInfo);
                if (outInfo.lowMemory) {
                    systemClearTask = true;
                    Toast.makeText(this, "MainActivity: in lowMemory ", Toast.LENGTH_SHORT).show();
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    if (tasks.size() > 3 || systemClearTask) {      // Samsung Android 5.1 max task number is 3
                        try {
                            //      Toast.makeText(this, "MainActivity: system clear recentTaskList !", Toast.LENGTH_SHORT).show();
                            Thread.sleep(1000);
                            currentTask.moveToFront();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    if (systemClearTask) {
                        try {
                            //      Toast.makeText(this, "MainActivity: system clear recentTaskList !", Toast.LENGTH_SHORT).show();
                            Thread.sleep(1000);
                            currentTask.moveToFront();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE ->
                    Toast.makeText(this, "MainActivity: RUNNING_MODERATE !", Toast.LENGTH_SHORT).show();
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW ->
                    Toast.makeText(this, "MainActivity: RUNNING_LOW !", Toast.LENGTH_SHORT).show();
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL ->
                    Toast.makeText(this, "MainActivity: RUNNING_CRITICAL !", Toast.LENGTH_SHORT).show();
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND ->
                    Toast.makeText(this, "MainActivity: BACKGROUND !", Toast.LENGTH_SHORT).show();
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE -> {
                systemClearTask = true;
                    Toast.makeText(this, "MainActivity: MODERATE !", Toast.LENGTH_SHORT).show();
            }
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                systemClearTask = true;
                Toast.makeText(this, "MainActivity: COMPLETE !", Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    for (int i = 0; i < tasks.size(); i++) {
                        eachTask = tasks.get(i);
                        if (eachTask.getTaskInfo().taskId != getTaskId()) {
                            eachTask.finishAndRemoveTask();
                        }
                    }
                } else {
                    for (int i = 1; i < tasks.size(); i++) {
                        eachTask = tasks.get(i);
                        eachTask.finishAndRemoveTask();
                    }
                }
            }
            //Toast.makeText(this, "MainActivity: Memory is extremely low, free recent task !", Toast.LENGTH_SHORT).show();
            default -> Toast.makeText(this, "MainActivity: default !", Toast.LENGTH_SHORT).show();
        }

        super.onTrimMemory(level);
    }

    @Override
    public void onLowMemory() {
        systemClearTask = true;
        Toast.makeText(this, "MainActivity: LowMemory !", Toast.LENGTH_SHORT).show();
        super.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        TimerThread = 0;
        returnApp = 0;
        appRntTimer = 0;

        if (userAdHandler != null) {
            userAdHandler.removeCallbacksAndMessages(null);
        }
        if (fragments != null && fragments.size() != 0) {
            fragments.clear();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
        if (pager != null && pageChangeCallback != null) {
            pager.unregisterOnPageChangeCallback(pageChangeCallback);
        }
        executor.shutdown();
        super.onDestroy();
    }

    static class Handler1 extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg1) {
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
        public void handleMessage(@NonNull Message msg2) {
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
                final String messageLoad = "Download Product Name Complete";
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
        public void handleMessage(@NonNull Message msg3) {
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
                final String messageLoad = "Download Image Name Complete";
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
        private final WeakReference<MainActivity> weakRefActivity;
        private final WeakReference<DishAdapter> weakRefDishAdapter;
        private final WeakReference<Lifecycle> weakRefLifecycle;

        Handler4(MainActivity hActivity, DishAdapter hDishAdapter, Lifecycle hLifecycle) {
            weakRefActivity = new WeakReference<>(hActivity);
            weakRefDishAdapter = new WeakReference<>(hDishAdapter);
            weakRefLifecycle = new WeakReference<>(hLifecycle);
        }

        @Override
        public void handleMessage(@NonNull Message msg4) {
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
                    if (ProductData.size() > DISH_SHOW_COUNT) {
                        for (int j = 0; j < DISH_SHOW_COUNT; j++) {
                            picShowImg.add(ProductData.get(picShowIndex.get(j)).getImg());
                        }
                    }
                    Reload = 1;
                }
                MainActivity hmActivity = weakRefActivity.get();
                Lifecycle hmLifecycle = weakRefLifecycle.get();
                if (hmActivity != null) {
                    hmActivity.iniUpperPage(hmActivity, hmLifecycle, null);
                }
                DishAdapter hmDishAdapter = weakRefDishAdapter.get();
                if (hmDishAdapter != null) {
                    hmDishAdapter.notifyDataSetChanged();
                }
                if (hmActivity != null) {
                    hmActivity.dialog.dismiss();
                }
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
        final String messageLoad = "Download Image file Complete";
        int indexImg = 0;
        @Override
        public void run() {
            // TODO: http request.

            for (indexImg=0; indexImg < dishProductAmount; indexImg++) {

                imagesRef.get(indexImg).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    int indexLoad = indexImg;
                    @Override
                    public void onSuccess(@NonNull byte[] bytes) {
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
        public void handleMessage(@NonNull Message msg5) {
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
                final String messageLoad = "Download Product Price Complete";
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
        public void handleMessage(@NonNull Message msg6) {
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
                final String messageLoad = "Download Product Introduction Complete";
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
        public void handleMessage(@NonNull Message msg7) {
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
                final String messageLoad = "Download Showing Product index Complete";
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

    public void iniUpperPage(MainActivity activity, Lifecycle lifecycle, View view) {
        if (fragments != null && fragments.size() > 0) {
            fragments.clear();
        }
        fragments = new Vector<>();
        if (picShowImg.size() == DISH_SHOW_COUNT) {
            fragments.add(Slide1Fragment.newInstance(Bitmap2Bytes(picShowImg.get(0)), "frame1"));
            fragments.add(Slide2Fragment.newInstance(Bitmap2Bytes(picShowImg.get(1)), "frame2"));
            fragments.add(Slide3Fragment.newInstance(Bitmap2Bytes(picShowImg.get(2)), "frame3"));
            fragments.add(Slide4Fragment.newInstance(Bitmap2Bytes(picShowImg.get(3)), "frame4"));
            fragments.add(Slide5Fragment.newInstance(Bitmap2Bytes(picShowImg.get(4)), "frame5"));
        } else {
            Toast.makeText(MainActivity.this, "No DishShow fragment ", Toast.LENGTH_SHORT).show();
            Bitmap picture = BitmapFactory.decodeResource(getResources(), R.drawable.store_icon);
            fragments.add(Slide1Fragment.newInstance(Bitmap2Bytes(picture), "frame1"));
            fragments.add(Slide2Fragment.newInstance(Bitmap2Bytes(picture), "frame2"));
            fragments.add(Slide3Fragment.newInstance(Bitmap2Bytes(picture), "frame3"));
            fragments.add(Slide4Fragment.newInstance(Bitmap2Bytes(picture), "frame4"));
            fragments.add(Slide5Fragment.newInstance(Bitmap2Bytes(picture), "frame5"));
        }

        if (adapterLayout == 0) {
            pager = findViewById(R.id.viewPager_id);
            dot1 = findViewById(R.id.imgIcon1_id);
            dot2 = findViewById(R.id.imgIcon2_id);
            dot3 = findViewById(R.id.imgIcon3_id);
            dot4 = findViewById(R.id.imgIcon4_id);
            dot5 = findViewById(R.id.imgIcon5_id);
        } else {
            pager = view.findViewById(R.id.viewPager_id);
            dot1 = view.findViewById(R.id.imgIcon1_id);
            dot2 = view.findViewById(R.id.imgIcon2_id);
            dot3 = view.findViewById(R.id.imgIcon3_id);
            dot4 = view.findViewById(R.id.imgIcon4_id);
            dot5 = view.findViewById(R.id.imgIcon5_id);
        }

        mPagerAdapter = new PageAdapter(activity.getSupportFragmentManager(), fragments, lifecycle);
        pager.setAdapter(mPagerAdapter);
        userAdHandler = new UserHandler(pager, activity);
        UserTimerThread adTimerThread = new UserTimerThread(activity);
        TimerThread = 1;
        adTimerThread.start();

        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
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
        };
        pager.registerOnPageChangeCallback(pageChangeCallback);

    }

    public static class UserHandler extends Handler {
        private final WeakReference<ViewPager2> weakRefPager;
        private final WeakReference<MainActivity> weakRefActivity;

        UserHandler (ViewPager2 hPager, MainActivity activity) {
            weakRefPager = new WeakReference<>(hPager);
            weakRefActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity hActivity = weakRefActivity.get();
            ViewPager2 hmPager = weakRefPager.get();

            hActivity.appRntTimer = msg.what;
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
                if (hActivity.appRntTimer == (hActivity.returnApp % 5)) {
                    hActivity.returnApp = 0;
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

    public static byte [] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos =  new  ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,  100 , baos);
        return  baos.toByteArray();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_main);
        int index = 0;

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            if (isTaskRoot() && returnApp == 0) {
                returnApp = appRntTimer + 1;
                Toast.makeText(this.getBaseContext(), "再按一次, 可退出3C生活百貨! ", Toast.LENGTH_LONG).show();
            } else {
                TimerThread = 0;
                returnApp = 0;
                appRntTimer = 0;
                ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.AppTask> tasks = am.getAppTasks();
                ActivityManager.AppTask eachTask;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    for (int i = 0; i < tasks.size(); i++) {
                        eachTask = tasks.get(i);
                        if (eachTask.getTaskInfo().taskId != getTaskId()) {
                            eachTask.finishAndRemoveTask();
                        }
                        else {
                            Log.i("Activity number", ": "+ eachTask.getTaskInfo().numActivities );
                            Log.i("BaseActivity", "===>"+ eachTask.getTaskInfo().baseActivity );
                        }
                    }
                } else {
                    for (int i = 1; i < tasks.size(); i++) {
                        eachTask = tasks.get(i);
                        eachTask.finishAndRemoveTask();
                    }
                }
                boolean isDbTaskIdListEmpty = dbHelper.IsDbTaskIdEmpty();
                if (!isDbTaskIdListEmpty) {
                    try {
                        Cursor cursor = dbHelper.getTaskIdList();
                        index = cursor.getInt(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (retainRecentTask != null) {
                    if (retainRecentTask.equals("RECENT_TASK")) {
                        if (!isDbTaskIdListEmpty) {
                            if (dbHelper.updateRecentTaskId(index, getTaskId()) == 0) {
                                Log.i("update recent task id: ", "no data change!");
                            }
                        }
                        else {
                            if (dbHelper.createTaskId(getTaskId(), -1, -1) == -1) {
                                Log.i("create recent task id: ", "fail !");
                            }
                        }
                    }
                }
                if (!isDbTaskIdListEmpty) {
                    if (dbHelper.updateMainActivityTaskId(index, getTaskId()) == 0) {
                        Log.i("mainActivityTaskId: ", "update result, no data change!");
                    }
                    if (dbHelper.updateOrderActivityTaskId(index, -1) == 0) {
                        Log.i("orderActivityTaskId: ", "update result, no data change!");
                    }
                }
                else {
                    if (dbHelper.createTaskId(-1, getTaskId(), -1) == -1) {
                        Log.i("mainActivityTaskId: ", "create result, fail !");
                    }
                }
                MainActivity.this.finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        if (InternetConnection.checkConnection(MainActivity.this)) {
            mAuth = FirebaseAuth.getInstance();
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

        final MenuItem searchMenuItem = menu.findItem(R.id.action_main_search);
        final MenuItem shoppingCarMenuItem = menu.findItem(R.id.action_main_shopping_car);
        FrameLayout rootViewSearch = (FrameLayout) searchMenuItem.getActionView();
        FrameLayout rootViewShoppingCar = (FrameLayout) shoppingCarMenuItem.getActionView();

        ImageView searchIcon = rootViewSearch.findViewById(R.id.search_icon_id);
        ImageView shoppingCarIcon = rootViewShoppingCar.findViewById(R.id.shopping_car_icon_id);

        rootViewSearch.setOnClickListener(new View.OnClickListener() {
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
            case R.id.action_main_shopping_car:
                intentItem = new Intent();
                bundleItem = new Bundle();
                bundleItem.putString("Menu", "DISH");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(MainActivity.this, OrderActivity.class);
                startActivity(intentItem);
                MainActivity.this.finish();
                break;
            case R.id.action_main_search:
                intentItem = new Intent();
                bundleItem = new Bundle();
                bundleItem.putString("Menu", "DISH");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(MainActivity.this, SearchActivity.class);
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

        DrawerLayout drawer = findViewById(R.id.drawer_layout_main);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}

