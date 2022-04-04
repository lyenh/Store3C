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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import static com.example.user.store3c.MainActivity.userImg;

public class BookActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener{
    public static ArrayList<ProductItem> BookData = new ArrayList<>();

    private bookRecyclerAdapter bookAdapter = null;
    private static DatabaseReference bookAmount;
    private static DatabaseReference bookName;
    private static DatabaseReference bookImgName;
    private static DatabaseReference bookPrice;
    private static DatabaseReference bookIntro;
    private static StorageReference mStorageRef;
    private static ArrayList<StorageReference> imagesRef = new ArrayList<> ();
    private static ArrayList<String> picListName = new ArrayList<> ();
    private static ArrayList<Bitmap> picListImg = new ArrayList<> ();
    private static ArrayList<String> picListPrice = new ArrayList<> ();
    private static ArrayList<String> picListIntro = new ArrayList<> ();
    private static ArrayList<Integer> picListIndex = new ArrayList<> ();
    private static final long ONE_MEGABYTE = 1024 * 1024;
    private static int bookProductAmount = 0;
    private static int bookProductImgCount = 0, bookProductPriceCount = 0;
    private static int bookProductNameCount = 0, bookProductIntroCount = 0;
    private static ProgressDialog dialog;
    private static int threadComplete = 0;
    private static int Reload = 0;
    private ImageView logoImage;
    private byte[] dbUserPicture;
    private NavigationView navigationView;
    private handler1 handlerDownload1 = new handler1();
    private static handler2 handlerDownload2 = new handler2();
    private static handler3 handlerDownload3 = new handler3();
    private static handler4 handlerDownload4;
    private static handler5 handlerDownload5 = new handler5();
    private static handler6 handlerDownload6 = new handler6();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        RecyclerView bookRecyclerView;
        ImageView imageTitle;
        Toolbar toolbar = findViewById(R.id.toolbarBook);
        setSupportActionBar(toolbar);
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int imgHeight;
        AccountDbAdapter dbHelper;

        DrawerLayout drawer = findViewById(R.id.drawer_layout_book);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view_book);
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
                        }
                        else {
                            int cursorSize = cursor.size(), num, length = 0;
                            int remainLength = cursor.get(cursorSize - 2).getBlob(0).length;
                            byte[] picture = new byte[1000000 * (cursorSize - 2) + remainLength];
                            for (num = 0; num < cursorSize - 2; num++) {
                                System.arraycopy(cursor.get(num).getBlob(0), 0, picture, length, cursor.get(num).getBlob(0).length);
                                length = length + cursor.get(num).getBlob(0).length;
                            }
                            System.arraycopy(cursor.get(num).getBlob(0), 0, picture, length, cursor.get(num).getBlob(0).length);
                            dbUserPicture = picture;
                            Toast.makeText(BookActivity.this, "picture size: " + dbUserPicture.length, Toast.LENGTH_LONG).show();
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

        if (BookData.size() == 0) {
            bookRecyclerView = findViewById(R.id.bookRecyclerView_id);
            bookAdapter = new bookRecyclerAdapter(BookData, this, "BOOK");
            bookRecyclerView.setAdapter(bookAdapter);
            bookRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            if (InternetConnection.checkConnection(BookActivity.this)) {
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
                                        Toast.makeText(BookActivity.this, "Authentication failed.",
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
                Toast.makeText(BookActivity.this, "網路未連線! ", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            setUserAccountText();
            bookRecyclerView = findViewById(R.id.bookRecyclerView_id);
            bookAdapter = new bookRecyclerAdapter(BookData, this, "BOOK");
            bookRecyclerView.setAdapter(bookAdapter);
            bookRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        imageTitle = findViewById(R.id.titleImageView_id);
        if (isTab) {
            if (screenWidth > rotationScreenWidth) {
                imgHeight = 500;
                imageTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imgHeight));
            }
        }
        else {
            if (screenWidth > rotationScreenWidth) {
                imgHeight = 300;
                imageTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imgHeight));
            }
        }
        imageTitle.setImageResource(R.drawable.title);
        logoImage.setOnClickListener(this);

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig){
        bookRecyclerAdapter.screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        bookAdapter.notifyDataSetChanged();
        Log.v("===>","ORIENTATION");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.logoImage_id) {
            Intent intentItem = new Intent();
            Bundle bundleItem = new Bundle();
            bundleItem.putString("Menu", "BOOK");
            intentItem.putExtras(bundleItem);
            intentItem.setClass(BookActivity.this,UserActivity.class);
            startActivity(intentItem);
            BookActivity.this.finish();
        }
    }

    private void startDownload() {
        FirebaseDatabase dbBook;
        DatabaseReference bookRef;
        dbBook = FirebaseDatabase.getInstance();
        try {
            if (!MainActivity.setFirebaseDbPersistence) {
                dbBook.setPersistenceEnabled(true);
                MainActivity.setFirebaseDbPersistence = true;
            }
        }
        catch (Exception e) {
            Toast.makeText(BookActivity.this, "Pick image timeout, reload data.", Toast.LENGTH_SHORT).show();
            Log.i("Pick image timeout: " , "reload data.");
        }
        bookRef = dbBook.getReference("book");
        bookRef.keepSynced(true);
        bookAmount = dbBook.getReference("bookAmount");
        bookAmount.keepSynced(true);
        bookName = bookRef.child("bookName").getRef();
        bookName.keepSynced(true);
        bookImgName = bookRef.child("bookImgName").getRef();
        bookImgName.keepSynced(true);
        bookPrice = bookRef.child("bookPrice").getRef();
        bookPrice.keepSynced(true);
        bookIntro = bookRef.child("bookIntro").getRef();
        bookIntro.keepSynced(true);
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://store3c-137123.appspot.com");
        handlerDownload4  = new handler4(bookAdapter);
        new Thread(runnable1).start();
        dialog = new ProgressDialog(BookActivity.this);
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

        if (InternetConnection.checkConnection(BookActivity.this)) {
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
            bookProductAmount = messageTransaction.intValue();
            Log.i("Firebase ==>", "Download Book Amount Complete: " + messageTransaction);
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
            bookAmount.addValueEventListener(new ValueEventListener() {
                //String messageLoad = "Download Book Amount Complete";
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    Long value = dataSnapshot.getValue(Long.class);

                    //Log.i("Firebase ==>", "Book amount is: " + value);
                    Message msg1 = new Message();
                    msg1.obj = value;
                    handlerDownload1.sendMessage(msg1);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Failed to read value
                    Log.i("Firebase ==>", "Failed to read amount.", error.toException());
                    Toast.makeText(BookActivity.this, "DatabaseError, bookAmount: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
            bookName.addChildEventListener(new ChildEventListener() {
                String messageLoad = "Download Book Name Complete";
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    String bookProductName = dataSnapshot.getValue(String.class);
                    picListName.add(bookProductName);
                    bookProductNameCount++;
                    //Log.i("Firebase ==>", "book name is: " + bookProductName);
                    if (bookProductNameCount == bookProductAmount) {
                        Message msg2_1 = new Message();
                        Message msg2_2 = new Message();
                        msg2_1.obj = messageLoad;
                        msg2_2.obj = messageLoad;
                        bookProductNameCount = 0;
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
                    //Toast.makeText(BookActivity.this, "DatabaseError, bookName: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
            bookImgName.addChildEventListener(new ChildEventListener() {
                String messageLoad = "Download Book Image Name Complete";
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    String ImgFileName = dataSnapshot.getValue(String.class);
                    bookProductImgCount++;
                    String bookImageName = "book/" + ImgFileName;
                    imagesRef.add(mStorageRef.child(bookImageName));
                    //Log.i("Firebase ==>", "book image name is: " + ImgFileName);

                    if (bookProductImgCount == bookProductAmount) {
                        Message msg3 = new Message();
                        msg3.obj = messageLoad;
                        bookProductImgCount = 0;
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
                         //Toast.makeText(BookActivity.this, "DatabaseError, bookImgName: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    };

    static class handler4 extends Handler {
        private WeakReference<bookRecyclerAdapter> weakRefBookAdapter;

        handler4(bookRecyclerAdapter hBookAdapter) {
            weakRefBookAdapter = new WeakReference<>(hBookAdapter);
        }

        @Override
        public void handleMessage(Message msg4) {
            super.handleMessage(msg4);

            String messageTransaction = (String)msg4.obj;
            //Log.i("Firebase thread  ==>", messageTransaction);
            if (messageTransaction.equals("Download Book Image file Complete")) {
                bookProductImgCount++;
                if (bookProductImgCount == bookProductAmount) {
                    RangeImgSequence();
                    threadComplete++;
                }
            }
            else {
                threadComplete++;
            }
            if (threadComplete == 4) {
                if (Reload == 0) {
                    for (int i = 0; i < bookProductAmount; i++) {
                        BookData.add(new ProductItem(picListImg.get(i), picListName.get(i), picListPrice.get(i), picListIntro.get(i)));
                    }
                    Reload = 1;
                }
                bookRecyclerAdapter hmBookAdapter = weakRefBookAdapter.get();
                if (hmBookAdapter != null) {
                    hmBookAdapter.notifyDataSetChanged();
                }
                dialog.dismiss();
            }
        }

        void RangeImgSequence() {
            ArrayList<Bitmap> picListImg1 = new ArrayList<> ();

            for (int i=0; i<bookProductAmount; i++) {
                for (int j=0; j<bookProductAmount; j++) {
                    if (i == picListIndex.get(j)) {
                        picListImg1.add(picListImg.get(j));
                    }
                }
            }
            picListImg.subList(0, bookProductAmount).clear();
            for (int n=0; n<bookProductAmount; n++) {
                picListImg.add(picListImg1.get(n));
            }

        }
    }

    static Runnable runnable4 = new Runnable() {
        private int indexImg = 0;

        @Override
        public void run() {
            // TODO: http request.
            for (indexImg=0; indexImg < bookProductAmount; indexImg++) {

                imagesRef.get(indexImg).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    int indexLoad = indexImg;
                    String messageLoad = "Download Book Image file Complete";
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Data for "images/island.jpg" is returns, use this as needed

                        if (bytes.length != 0) {
                            picListImg.add(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                            picListIndex.add(indexLoad);
                        }
                        //Log.i("Firebase ==>", "Download book image file success.");
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
            bookPrice.addChildEventListener(new ChildEventListener() {
                String messageLoad = "Download Book Price Complete";
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    String bookProductPrice = dataSnapshot.getValue(String.class);
                    picListPrice.add(bookProductPrice);
                    bookProductPriceCount++;
                    //Log.i("Firebase ==>", "book price is: " + bookProductPrice);
                    if (bookProductPriceCount == bookProductAmount) {
                        Message msg5_1 = new Message();
                        Message msg5_2 = new Message();
                        msg5_1.obj = messageLoad;
                        msg5_2.obj = messageLoad;
                        bookProductPriceCount = 0;
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
                    //Toast.makeText(BookActivity.this, "DatabaseError, bookPrice: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
            bookIntro.addChildEventListener(new ChildEventListener() {
                String messageLoad = "Download Book Introduction Complete";
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    String bookIntro = dataSnapshot.getValue(String.class);
                    picListIntro.add(bookIntro);
                    bookProductIntroCount++;
                    //Log.i("Firebase ==>", "book introduction is: " + bookIntro);
                    if (bookProductIntroCount == bookProductAmount) {
                        Message msg6_1 = new Message();
                        Message msg6_2 = new Message();
                        msg6_1.obj = messageLoad;
                        msg6_2.obj = messageLoad;
                        bookProductIntroCount = 0;
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
                    //Toast.makeText(BookActivity.this, "DatabaseError, bookIntro: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    };


    @Override
    public void onBackPressed() {
        Intent intent;
        DrawerLayout drawer = findViewById(R.id.drawer_layout_book);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            intent = new Intent();
            intent.setClass(BookActivity.this, MainActivity.class);
            startActivity(intent);
            BookActivity.this.finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.book, menu);
        if (InternetConnection.checkConnection(BookActivity.this)) {
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
        final MenuItem searchMenuItem = menu.findItem(R.id.action_book_search);
        final MenuItem shoppingCarMenuItem = menu.findItem(R.id.action_book_shopping_car);
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
                bundleItem.putString("Menu", "BOOK");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(BookActivity.this, PositionActivity.class);
                startActivity(intentItem);
                BookActivity.this.finish();
                //Toast.makeText(this.getBaseContext(),"The setting item", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_user_account:
                //  Toast.makeText(this.getBaseContext(),"The user account item", Toast.LENGTH_SHORT).show();
                intentItem = new Intent();
                bundleItem = new Bundle();
                bundleItem.putString("Menu", "BOOK");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(BookActivity.this,UserActivity.class);
                startActivity(intentItem);
                BookActivity.this.finish();
                break;
            case R.id.action_login_status:
                if (InternetConnection.checkConnection(BookActivity.this)) {
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser == null || currentUser.isAnonymous()) {
                        intentItem = new Intent();
                        bundleItem = new Bundle();
                        bundleItem.putString("Menu", "BOOK");
                        intentItem.putExtras(bundleItem);
                        intentItem.setClass(BookActivity.this, LoginActivity.class);
                        startActivity(intentItem);
                        BookActivity.this.finish();
                    } else {
                        mAuth.signOut();
                        Toast.makeText(BookActivity.this, "登出成功 ! ", Toast.LENGTH_SHORT).show();
                        setUserAccountText();
                        item.setTitle("使用者登入");
                    }
                } else {
                    Toast.makeText(BookActivity.this, "請先開啟網路連線 ! ", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_memo:
                intentItem = new Intent();
                bundleItem = new Bundle();
                bundleItem.putString("Menu", "BOOK");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(BookActivity.this, MemoActivity.class);
                startActivity(intentItem);
                BookActivity.this.finish();
                //Toast.makeText(this.getBaseContext(),"The setting item", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_book_shopping_car:
                intentItem = new Intent();
                bundleItem = new Bundle();
                bundleItem.putString("Menu", "BOOK");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(BookActivity.this, OrderActivity.class);
                startActivity(intentItem);
                BookActivity.this.finish();
                break;
            case R.id.action_book_search:
                intentItem = new Intent();
                bundleItem = new Bundle();
                bundleItem.putString("Menu", "BOOK");
                intentItem.putExtras(bundleItem);
                intentItem.setClass(BookActivity.this, SearchActivity.class);
                startActivity(intentItem);
                BookActivity.this.finish();
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
            intentItem.setClass(BookActivity.this, MainActivity.class);
            startActivity(intentItem);
            BookActivity.this.finish();
        } else if (id == R.id.nav_cake) {
            Intent intentItem = new Intent();
            intentItem.setClass(BookActivity.this, CakeActivity.class);
            startActivity(intentItem);
            BookActivity.this.finish();
        } else if (id == R.id.nav_phone) {
            Intent intentItem = new Intent();
            intentItem.setClass(BookActivity.this, PhoneActivity.class);
            startActivity(intentItem);
            BookActivity.this.finish();
        } else if (id == R.id.nav_camera) {
            Intent intentItem = new Intent();
            intentItem.setClass(BookActivity.this, CameraActivity.class);
            startActivity(intentItem);
            BookActivity.this.finish();
        } else if (id == R.id.nav_book) {

        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout_book);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
