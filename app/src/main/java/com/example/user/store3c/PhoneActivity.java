package com.example.user.store3c;

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

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.adapter.FragmentStateAdapter;
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
import com.google.android.material.tabs.TabLayoutMediator;
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
import static com.example.user.store3c.MainActivity.isTab;
import static com.example.user.store3c.MainActivity.mAuth;
import static com.example.user.store3c.MainActivity.rotationScreenWidth;
import static com.example.user.store3c.MainActivity.rotationTabScreenWidth;
import static com.example.user.store3c.MainActivity.userImg;

public class PhoneActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener,
        TabFragment.OnTabFragmentInteractionListener{
    private ViewPager2 pager;
    private TabLayout tabs;
    private static TabFragment tabFragment1, tabFragment2, tabFragment3;
    private static int PhonePosition = 0;
    private static ArrayList<ProductItem> PhoneData = new ArrayList<>();
    private static ArrayList<ProductItem> Brand1Data = new ArrayList<>();
    private static ArrayList<ProductItem> Brand2Data = new ArrayList<>();
    private static ArrayList<ProductItem> Brand3Data = new ArrayList<>();
    private static ViewPagerAdapter phoneAdapter;
    private static DatabaseReference phoneAmount;
    private static DatabaseReference phoneName;
    private static DatabaseReference phoneImgName;
    private static DatabaseReference phonePrice;
    private static DatabaseReference phoneIntro;
    private static StorageReference mStorageRef;
    private static ArrayList<StorageReference> imagesRef = new ArrayList<> ();
    private static ArrayList<String> picListName = new ArrayList<> ();
    private static ArrayList<Bitmap> picListImg = new ArrayList<> ();
    private static ArrayList<String> picListPrice = new ArrayList<> ();
    private static ArrayList<String> picListIntro = new ArrayList<> ();
    private static ArrayList<Integer> picListIndex = new ArrayList<> ();
    private static final long ONE_MEGABYTE = 1024 * 1024;
    private static int phoneProductAmount = 0;
    private static int phoneProductImgCount = 0, phoneProductPriceCount = 0;
    private static int phoneProductNameCount = 0, phoneProductIntroCount = 0;
    private static ProgressDialog dialog;
    private static int threadComplete = 0;
    private static int Reload = 0;
    private static int download = 0;

    private ImageView logoImage;
    private byte[] dbUserPicture;
    private NavigationView navigationView;
    private handler1 handlerDownload1 = new handler1();
    private static handler2 handlerDownload2 = new handler2();
    private static handler3 handlerDownload3 = new handler3();
    private static handler4 handlerDownload4;
    private static handler5 handlerDownload5 = new handler5();
    private static handler6 handlerDownload6 = new handler6();
    private static FragmentManager staticFragmentManager;
    private static Lifecycle staticLifecycle;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        Toolbar toolbar = findViewById(R.id.toolbarPhone);
        setSupportActionBar(toolbar);
        AccountDbAdapter dbHelper;

        DrawerLayout drawer = findViewById(R.id.drawer_layout_phone);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        staticFragmentManager = getSupportFragmentManager();
        staticLifecycle = getLifecycle();
        navigationView = findViewById(R.id.nav_view_phone);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setItemIconTintList(null);
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
                            Toast.makeText(PhoneActivity.this, "picture size: " + dbUserPicture.length, Toast.LENGTH_LONG).show();
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

        pager = findViewById(R.id.viewPagerPhone_id);
        if (PhoneData.size() == 0) {
            if (InternetConnection.checkConnection(PhoneActivity.this)) {
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
                                        Toast.makeText(PhoneActivity.this, "Authentication failed.",
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
                Toast.makeText(PhoneActivity.this, "網路未連線! ", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            if (InternetConnection.checkConnection(PhoneActivity.this)) {
                mAuth = FirebaseAuth.getInstance();
            }
            else {
                Toast.makeText(PhoneActivity.this, "網路未連線! ", Toast.LENGTH_SHORT).show();
            }
            setUserAccountText();
            phoneAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
            pager.setAdapter(phoneAdapter);
            tabs = findViewById(R.id.phoneTabLayout_id);
            new TabLayoutMediator(tabs, pager,
                    new TabLayoutMediator.TabConfigurationStrategy() {
                        @Override public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                            if (position == 0) {
                                tab.setText("HTC");
                            }
                            if(position == 1) {
                                tab.setText("ASUS");
                            }
                            if(position == 2) {
                                tab.setText("SAMSUNG");
                            }
                        }
                    }).attach();

            tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    pager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            pager.setCurrentItem(PhonePosition);
            if (isTab) {
                if (Resources.getSystem().getDisplayMetrics().widthPixels > rotationTabScreenWidth) {
                    switch (PhonePosition) {
                        case 0:
                            if (tabFragment1 != null) {
                                tabFragment1.notificationPhoneTypeLayout();
                            }
                            break;
                        case 1:
                            if (tabFragment2 != null) {
                                tabFragment2.notificationPhoneTypeLayout();
                            }
                            break;
                        case 2:
                            if (tabFragment3 != null) {
                                tabFragment3.notificationPhoneTypeLayout();
                            }
                            break;
                        default:
                            Toast.makeText(PhoneActivity.this, "Fragment error:  " + PhonePosition, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
            else {
                if (Resources.getSystem().getDisplayMetrics().widthPixels > rotationScreenWidth) {
                    switch (PhonePosition) {
                        case 0:
                            if (tabFragment1 != null) {
                                tabFragment1.notificationPhoneTypeLayout();
                            }
                            break;
                        case 1:
                            if (tabFragment2 != null) {
                                tabFragment2.notificationPhoneTypeLayout();
                            }
                            break;
                        case 2:
                            if (tabFragment3 != null) {
                                tabFragment3.notificationPhoneTypeLayout();
                            }
                            break;
                        default:
                            Toast.makeText(PhoneActivity.this, "Fragment error:  " + PhonePosition, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        }

        logoImage.setOnClickListener(this);

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                PhonePosition = position;
                tabs.selectTab(tabs.getTabAt(position));

                switch (PhonePosition) {
                    case 0:
                        if (tabFragment1 != null) {
                            tabFragment1.notificationAdapter();
                        }
                        break;
                    case 1:
                        if (tabFragment2 != null) {
                            tabFragment2.notificationAdapter();
                        }
                        break;
                    case 2:
                        if (tabFragment3 != null) {
                            tabFragment3.notificationAdapter();
                        }
                        break;
                    default :
                        Toast.makeText(PhoneActivity.this, "Fragment error:  "+ PhonePosition, Toast.LENGTH_SHORT).show();
                        break;
                }
                //Toast.makeText(PhoneActivity.this, "The fragment "+ position, Toast.LENGTH_SHORT).show();
            }

        });

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                mAdView = findViewById(R.id.adView_id);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }
        });

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig){
        if (InternetConnection.checkConnection(PhoneActivity.this)) {
            if (download == 0) {
                phoneAdapter.notifyDataSetChanged();
                switch (PhonePosition) {
                    case 0:
                        tabFragment1.notificationAdapter();
                        break;
                    case 1:
                        tabFragment2.notificationAdapter();
                        break;
                    case 2:
                        tabFragment3.notificationAdapter();
                        break;
                    default :
                        Toast.makeText(PhoneActivity.this, "Fragment error:  "+ PhonePosition, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
        else {
            if (download == 0 && PhoneData.size() != 0) {
                phoneAdapter.notifyDataSetChanged();
                switch (PhonePosition) {
                    case 0:
                        tabFragment1.notificationAdapter();
                        break;
                    case 1:
                        tabFragment2.notificationAdapter();
                        break;
                    case 2:
                        tabFragment3.notificationAdapter();
                        break;
                    default :
                        Toast.makeText(PhoneActivity.this, "Fragment error:  "+ PhonePosition, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            Toast.makeText(PhoneActivity.this, "網路未連線! ", Toast.LENGTH_SHORT).show();
        }
        Log.v("===>","ORIENTATION");
        super.onConfigurationChanged(newConfig);
    }

    private void startDownload() {
        FirebaseDatabase dbPhone;
        DatabaseReference phoneRef;

        download = 1;
        dbPhone = FirebaseDatabase.getInstance();
        try {
            if (!MainActivity.setFirebaseDbPersistence) {
                dbPhone.setPersistenceEnabled(true);
                MainActivity.setFirebaseDbPersistence = true;
            }
        }
        catch (Exception e) {
            Toast.makeText(PhoneActivity.this, "Pick image timeout, reload data.", Toast.LENGTH_SHORT).show();
            Log.i("Pick image timeout: " , "reload data.");
        }
        phoneRef = dbPhone.getReference("phone");
        phoneRef.keepSynced(true);
        phoneAmount = dbPhone.getReference("phoneAmount");
        phoneAmount.keepSynced(true);
        phoneName = phoneRef.child("phoneName").getRef();
        phoneName.keepSynced(true);
        phoneImgName = phoneRef.child("phoneImgName").getRef();
        phoneImgName.keepSynced(true);
        phonePrice = phoneRef.child("phonePrice").getRef();
        phonePrice.keepSynced(true);
        phoneIntro = phoneRef.child("phoneIntro").getRef();
        phoneIntro.keepSynced(true);
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://store3c-137123.appspot.com");
        tabs = findViewById(R.id.phoneTabLayout_id);
        handlerDownload4 = new handler4(PhoneActivity.this, pager, tabs);
        new Thread(runnable1).start();
        dialog = new ProgressDialog(PhoneActivity.this);
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

        if (InternetConnection.checkConnection(PhoneActivity.this)) {
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

    static class handler1 extends Handler {
        @Override
        public void handleMessage(Message msg1) {
            super.handleMessage(msg1);

            Long messageTransaction = (Long)msg1.obj;
            phoneProductAmount = messageTransaction.intValue();
            Log.i("Firebase ==>", "Download Phone Amount Complete: " + messageTransaction);
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
            phoneAmount.addValueEventListener(new ValueEventListener() {
                //String messageLoad = "Download Phone Amount Complete";
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    Long value = dataSnapshot.getValue(Long.class);

                    //Log.i("Firebase ==>", "Phone amount is: " + value);
                    Message msg1 = new Message();
                    msg1.obj = value;
                    handlerDownload1.sendMessage(msg1);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Failed to read value
                    Log.i("Firebase ==>", "Failed to read amount.", error.toException());
                    Toast.makeText(PhoneActivity.this, "DatabaseError, phoneAmount: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
            phoneName.addChildEventListener(new ChildEventListener() {
                String messageLoad = "Download Phone Name Complete";
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    String phoneProductName = dataSnapshot.getValue(String.class);
                    picListName.add(phoneProductName);
                    phoneProductNameCount++;
                    //Log.i("Firebase ==>", "phone name is: " + phoneProductName);
                    if (phoneProductNameCount == phoneProductAmount) {
                        Message msg2_1 = new Message();
                        Message msg2_2 = new Message();
                        msg2_1.obj = messageLoad;
                        msg2_2.obj = messageLoad;
                        phoneProductNameCount = 0;
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
                    //Toast.makeText(PhoneActivity.this, "DatabaseError, phoneName: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
            phoneImgName.addChildEventListener(new ChildEventListener() {
                String messageLoad = "Download Phone Image Name Complete";
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    String ImgFileName = dataSnapshot.getValue(String.class);
                    phoneProductImgCount++;
                    String phoneImageName = "phone/" + ImgFileName;
                    imagesRef.add(mStorageRef.child(phoneImageName));
                    //Log.i("Firebase ==>", "phone image name is: " + ImgFileName);

                    if (phoneProductImgCount == phoneProductAmount) {
                        Message msg3 = new Message();
                        msg3.obj = messageLoad;
                        phoneProductImgCount = 0;
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
                    //Toast.makeText(PhoneActivity.this, "DatabaseError, phoneImgName: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    };

    static class handler4 extends Handler {
        private WeakReference<PhoneActivity> weakRefActivity;
        private WeakReference<ViewPager2> weakRefPager;
        private WeakReference<TabLayout> weakRefTab;

        handler4(PhoneActivity hActivity, ViewPager2 hPager, TabLayout hTab) {
            weakRefActivity = new WeakReference<>(hActivity);
            weakRefPager = new WeakReference<>(hPager);
            weakRefTab = new WeakReference<>(hTab);
        }

        @Override
        public void handleMessage(Message msg4) {
            super.handleMessage(msg4);

            String messageTransaction = (String)msg4.obj;
            PhoneActivity hmActivity = weakRefActivity.get();
            //Log.i("Firebase thread  ==>", messageTransaction);
            if (messageTransaction.equals("Download Phone Image file Complete")) {
                phoneProductImgCount++;
                if (phoneProductImgCount == phoneProductAmount) {
                    RangeImgSequence();
                    threadComplete++;
                }
            }
            else {
                threadComplete++;
            }
            if (threadComplete == 4) {
                if (Reload == 0) {
                    for (int i = 0; i < phoneProductAmount; i++) {
                        PhoneData.add(new ProductItem(picListImg.get(i), picListName.get(i), picListPrice.get(i), picListIntro.get(i)));
                    }
                    Reload = 1;
                    String[] brand;
                    for (int index=0; index< PhoneData.size();index++) {
                        ProductItem pItem = PhoneData.get(index);
                        brand = pItem.getName().split(" ");
                        switch (brand[0]) {
                            case "HTC":
                                Brand1Data.add(pItem);
                                break;
                            case "ZenFone":
                                Brand2Data.add(pItem);
                                break;
                            case "Galaxy":
                                Brand3Data.add(pItem);
                                break;
                            default:
                                Log.i("Brand error ==>", "Failed to parse the string: " + brand[0]);
                                if (hmActivity != null) {
                                    Toast.makeText(hmActivity, "The brand name " + brand[0] + " is error.", Toast.LENGTH_SHORT).show();
                                }
                        }
                    }
                }
                phoneAdapter = new ViewPagerAdapter(staticFragmentManager, staticLifecycle);
                ViewPager2 hmPager = weakRefPager.get();
                TabLayout hmTab = weakRefTab.get();

                if (hmPager != null) {
                    hmPager.setAdapter(phoneAdapter);
                }
                if (hmPager != null && hmTab != null) {
                    new TabLayoutMediator(hmTab, hmPager,
                            new TabLayoutMediator.TabConfigurationStrategy() {
                                @Override public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                                    if (position == 0) {
                                        tab.setText("HTC");
                                    }
                                    if(position == 1) {
                                        tab.setText("ASUS");
                                    }
                                    if(position == 2) {
                                        tab.setText("SAMSUNG");
                                    }
                                }
                            }).attach();

                    hmTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                        @Override
                        public void onTabSelected(TabLayout.Tab tab) {
                            hmPager.setCurrentItem(tab.getPosition());
                        }

                        @Override
                        public void onTabUnselected(TabLayout.Tab tab) {

                        }

                        @Override
                        public void onTabReselected(TabLayout.Tab tab) {

                        }
                    });

                    hmPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                        @Override
                        public void onPageSelected(int position) {
                            PhonePosition = position;
                            hmTab.selectTab(hmTab.getTabAt(position));

                            switch (PhonePosition) {
                                case 0:
                                    if (tabFragment1 != null) {
                                        tabFragment1.notificationAdapter();
                                    }
                                    break;
                                case 1:
                                    if (tabFragment2 != null) {
                                        tabFragment2.notificationAdapter();
                                    }
                                    break;
                                case 2:
                                    if (tabFragment3 != null) {
                                        tabFragment3.notificationAdapter();
                                    }
                                    break;
                                default :
                                    if (hmActivity != null) {
                                        Toast.makeText(hmActivity, "Fragment error:  " + PhonePosition, Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                            }
                            //Toast.makeText(PhoneActivity.this, "The fragment "+ position, Toast.LENGTH_SHORT).show();
                        }

                    });
                }

                if (hmPager != null) {
                    hmPager.setCurrentItem(PhonePosition);
                    if (isTab) {
                        if (Resources.getSystem().getDisplayMetrics().widthPixels > rotationTabScreenWidth) {
                            switch (PhonePosition) {
                                case 0:
                                     if (tabFragment1 != null) {
                                         tabFragment1.notificationPhoneTypeLayout();
                                     }
                                    break;
                                case 1:
                                    if (tabFragment2 != null) {
                                        tabFragment2.notificationPhoneTypeLayout();
                                    }
                                    break;
                                case 2:
                                    if (tabFragment3 != null) {
                                        tabFragment3.notificationPhoneTypeLayout();
                                    }
                                    break;
                                default:
                                    if (hmActivity != null) {
                                        Toast.makeText(hmActivity, "Fragment error:  " + PhonePosition, Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                            }
                        }
                    } else {
                        if (Resources.getSystem().getDisplayMetrics().widthPixels > rotationScreenWidth) {
                            switch (PhonePosition) {
                                case 0:
                                    if (tabFragment1 != null) {
                                        tabFragment1.notificationPhoneTypeLayout();
                                    }
                                    break;
                                case 1:
                                    if (tabFragment2 != null) {
                                        tabFragment2.notificationPhoneTypeLayout();
                                    }
                                    break;
                                case 2:
                                    if (tabFragment3 != null) {
                                        tabFragment3.notificationPhoneTypeLayout();
                                    }
                                    break;
                                default:
                                    if (hmActivity != null) {
                                        Toast.makeText(hmActivity, "Fragment error:  " + PhonePosition, Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                            }
                        }
                    }
                }

                download = 0;
                dialog.dismiss();
            }
        }

        void RangeImgSequence() {
            ArrayList<Bitmap> picListImg1 = new ArrayList<> ();

            for (int i=0; i<phoneProductAmount; i++) {
                for (int j=0; j<phoneProductAmount; j++) {
                    if (i == picListIndex.get(j)) {
                        picListImg1.add(picListImg.get(j));
                    }
                }
            }
            picListImg.subList(0, phoneProductAmount).clear();
            for (int n=0; n<phoneProductAmount; n++) {
                picListImg.add(picListImg1.get(n));
            }

        }
    }

    static Runnable runnable4 = new Runnable() {
        private int indexImg = 0;

        @Override
        public void run() {
            // TODO: http request.
            for (indexImg=0; indexImg < phoneProductAmount; indexImg++) {

                imagesRef.get(indexImg).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    int indexLoad = indexImg;
                    String messageLoad = "Download Phone Image file Complete";
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Data for "images/island.jpg" is returns, use this as needed

                        if (bytes.length != 0) {
                            picListImg.add(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                            picListIndex.add(indexLoad);
                        }
                        //Log.i("Firebase ==>", "Download phone image file success.");
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
            phonePrice.addChildEventListener(new ChildEventListener() {
                String messageLoad = "Download Phone Price Complete";
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    String phoneProductPrice = dataSnapshot.getValue(String.class);
                    picListPrice.add(phoneProductPrice);
                    phoneProductPriceCount++;
                    //Log.i("Firebase ==>", "phone price is: " + phoneProductPrice);
                    if (phoneProductPriceCount == phoneProductAmount) {
                        Message msg5_1 = new Message();
                        Message msg5_2 = new Message();
                        msg5_1.obj = messageLoad;
                        msg5_2.obj = messageLoad;
                        phoneProductPriceCount = 0;
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
                    //Toast.makeText(PhoneActivity.this, "DatabaseError, phonePrice: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
            phoneIntro.addChildEventListener(new ChildEventListener() {
                String messageLoad = "Download Phone Introduction Complete";
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    String phoneIntro = dataSnapshot.getValue(String.class);
                    picListIntro.add(phoneIntro);
                    phoneProductIntroCount++;
                    //Log.i("Firebase ==>", "phone introduction is: " + phoneIntro);
                    if (phoneProductIntroCount == phoneProductAmount) {
                        Message msg6_1 = new Message();
                        Message msg6_2 = new Message();
                        msg6_1.obj = messageLoad;
                        msg6_2.obj = messageLoad;
                        phoneProductIntroCount = 0;
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
                    //Toast.makeText(PhoneActivity.this, "DatabaseError, phoneIntro: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    };

    @Override
    public void onClick(View v) {
        Intent intentItem;
        Bundle bundleItem;
        if (v.getId() == R.id.logoImage_id) {
            intentItem = new Intent();
            bundleItem = new Bundle();
            bundleItem.putString("Menu", "PHONE");
            intentItem.putExtras(bundleItem);
            intentItem.setClass(PhoneActivity.this,UserActivity.class);
            startActivity(intentItem);
            PhoneActivity.this.finish();
        }

    }

    public static class ViewPagerAdapter extends FragmentStateAdapter{

        public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);

            if (tabFragment1 == null) {
                tabFragment1 = TabFragment.newInstance(0, Brand1Data);
            }
            if (tabFragment2 == null) {
                tabFragment2 = TabFragment.newInstance(1, Brand2Data);
            }
            if (tabFragment3 == null) {
                tabFragment3 = TabFragment.newInstance(2, Brand3Data);
            }
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {

            switch (position) {
                case 0:
                    return tabFragment1;
                case 1:
                    return tabFragment2;
                case 2:
                    return tabFragment3;
                default:
                    Log.i("Fragment getItem error:", "Failed to read the position value: " + position);
                    //Toast.makeText(PhoneActivity.this, "The fragment number " + position + " is error.", Toast.LENGTH_SHORT).show();
                    return TabFragment.newInstance(position, Brand1Data);
            }

        }

        @Override
        public int getItemCount() {
            return 3;       //the amount of brand
        }

    }

    public void onTabFragmentInteraction(int position) {

        //Toast.makeText(PhoneActivity.this, "The fragment is " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        Intent intent;
        DrawerLayout drawer = findViewById(R.id.drawer_layout_phone);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            intent = new Intent();
            intent.setClass(PhoneActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(intent);
            PhoneActivity.this.finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.phone, menu);
        if (InternetConnection.checkConnection(PhoneActivity.this)) {
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem searchMenuItem = menu.findItem(R.id.action_phone_search);
        final MenuItem shoppingCarMenuItem = menu.findItem(R.id.action_phone_shopping_car);
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
                bundleItem.putString("Menu", "PHONE");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(PhoneActivity.this, PositionActivity.class);
                startActivity(intentItem);
                PhoneActivity.this.finish();
                //Toast.makeText(this.getBaseContext(),"The setting item", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_user_account:
                //  Toast.makeText(this.getBaseContext(),"The user account item", Toast.LENGTH_SHORT).show();
                intentItem = new Intent();
                bundleItem = new Bundle();
                bundleItem.putString("Menu", "PHONE");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(PhoneActivity.this,UserActivity.class);
                startActivity(intentItem);
                PhoneActivity.this.finish();
                break;
            case R.id.action_login_status:
                if (InternetConnection.checkConnection(PhoneActivity.this)) {
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser == null || currentUser.isAnonymous()) {
                        intentItem = new Intent();
                        bundleItem = new Bundle();
                        bundleItem.putString("Menu", "PHONE");
                        intentItem.putExtras(bundleItem);
                        intentItem.setClass(PhoneActivity.this, LoginActivity.class);
                        startActivity(intentItem);
                        PhoneActivity.this.finish();
                    } else {
                        mAuth.signOut();
                        Toast.makeText(PhoneActivity.this, "登出成功 ! ", Toast.LENGTH_SHORT).show();
                        setUserAccountText();
                        item.setTitle("使用者登入");
                    }
                } else {
                    Toast.makeText(PhoneActivity.this, "請先開啟網路連線 ! ", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_memo:
                intentItem = new Intent();
                bundleItem = new Bundle();
                bundleItem.putString("Menu", "PHONE");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(PhoneActivity.this, MemoActivity.class);
                startActivity(intentItem);
                PhoneActivity.this.finish();
                //Toast.makeText(this.getBaseContext(),"The setting item", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_phone_shopping_car:
                intentItem = new Intent();
                bundleItem = new Bundle();
                bundleItem.putString("Menu", "PHONE");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(PhoneActivity.this, OrderActivity.class);
                startActivity(intentItem);
                PhoneActivity.this.finish();
                break;
            case R.id.action_phone_search:
                intentItem = new Intent();
                bundleItem = new Bundle();
                bundleItem.putString("Menu", "PHONE");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(PhoneActivity.this, SearchActivity.class);
                startActivity(intentItem);
                PhoneActivity.this.finish();
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
            intentItem.setClass(PhoneActivity.this, MainActivity.class);
            intentItem.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(intentItem);
            PhoneActivity.this.finish();
        } else if (id == R.id.nav_cake) {
            Intent intentItem = new Intent();
            intentItem.setClass(PhoneActivity.this, CakeActivity.class);
            startActivity(intentItem);
            PhoneActivity.this.finish();
        } else if (id == R.id.nav_phone) {

        } else if (id == R.id.nav_camera) {
            Intent intentItem = new Intent();
            intentItem.setClass(PhoneActivity.this, CameraActivity.class);
            startActivity(intentItem);
            PhoneActivity.this.finish();
        } else if (id == R.id.nav_book) {
            Intent intentItem = new Intent();
            intentItem.setClass(PhoneActivity.this, BookActivity.class);
            startActivity(intentItem);
            PhoneActivity.this.finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_phone);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
