package com.example.user.store3c;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.example.user.store3c.MainActivity.mAuth;

@Keep
public class PromotionActivity extends AppCompatActivity implements View.OnClickListener{
    private String menu_item = "DISH", up_menu_item = "";
    private String totalPrice;
    private int orderTableSize = 0;
    private ArrayList<Integer> orderSet = new ArrayList<>();
    private boolean recentTaskPromotion = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion);

        Button ret_b;
        Bundle bundle = getIntent().getExtras();
        String retainRecentTask;
        if (bundle != null) {
            totalPrice = bundle.getString("totalPrice");
            orderTableSize = bundle.getInt("orderTableSize");
            orderSet = bundle.getIntegerArrayList("orderSet");
            if (bundle.getString("Menu") != null) {
                menu_item = bundle.getString("Menu");
            }
            if (bundle.getString("upMenu") != null) {
                up_menu_item = bundle.getString("upMenu");
            }
            retainRecentTask = bundle.getString("RetainRecentTask");
            if (retainRecentTask != null) {
                if (retainRecentTask.equals("RECENT_ACTIVITY")) {       // recent task created by newDocument flag
                    recentTaskPromotion = true;
                }
            }
        }

        new UploadOrderToFirebaseTask(orderTableSize, orderSet, getApplicationContext()).execute(totalPrice);

        ret_b = findViewById(R.id.promotionRtn_id);
        ret_b.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.promotionRtn_id) {
            onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intentItem = new Intent();
        intentItem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        Bundle bundle, retainRecentTaskBundle;

        switch (menu_item) {
            case "DISH":
                if (recentTaskPromotion) {
                    intentItem = Intent.makeMainActivity (new ComponentName(getApplicationContext(), MainActivity.class));
                    intentItem.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS);
                    retainRecentTaskBundle = new Bundle();
                    retainRecentTaskBundle.putString("RetainRecentTask", "RECENT_TASK");
                    intentItem.putExtras(retainRecentTaskBundle);
                }
                else {
                    intentItem.setClass(PromotionActivity.this, MainActivity.class);
                    intentItem.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
                break;
            case "CAKE":
                intentItem.setClass(PromotionActivity.this, CakeActivity.class);
                break;
            case "PHONE":
                intentItem.setClass(PromotionActivity.this, PhoneActivity.class);
                break;
            case "CAMERA":
                intentItem.setClass(PromotionActivity.this, CameraActivity.class);
                break;
            case "BOOK":
                intentItem.setClass(PromotionActivity.this, BookActivity.class);
                break;
            case "MEMO":
                bundle = new Bundle();
                if (!up_menu_item.equals("")) {
                    bundle.putString("Menu", up_menu_item);
                    intentItem.putExtras(bundle);
                }
                intentItem.setClass(PromotionActivity.this, MemoActivity.class);
                break;
            case "USER":
                bundle = new Bundle();
                if (!up_menu_item.equals("")) {
                    bundle.putString("Menu", up_menu_item);
                    intentItem.putExtras(bundle);
                }
                intentItem.setClass(PromotionActivity.this, UserActivity.class);
                break;
            case "POSITION":
                bundle = new Bundle();
                if (!up_menu_item.equals("")) {
                    bundle.putString("Menu", up_menu_item);
                    intentItem.putExtras(bundle);
                }
                intentItem.setClass(PromotionActivity.this, PositionActivity.class);
                break;
            case "PRODUCT":
                bundle = new Bundle();
                if (!up_menu_item.equals("")) {
                    bundle.putString("Menu", up_menu_item);
                    intentItem.putExtras(bundle);
                }
                intentItem.setClass(PromotionActivity.this, ProductActivity.class);
                break;
            case "MAP":
                bundle = new Bundle();
                if (!up_menu_item.equals("")) {
                    bundle.putString("Menu", up_menu_item);
                    intentItem.putExtras(bundle);
                }
                intentItem.setClass(PromotionActivity.this, MapsActivity.class);
                break;
            case "LOGIN":
                bundle = new Bundle();
                if (!up_menu_item.equals("")) {
                    bundle.putString("Menu", up_menu_item);
                    intentItem.putExtras(bundle);
                }
                intentItem.setClass(PromotionActivity.this, LoginActivity.class);
                break;
            case "PAGE":
                bundle = new Bundle();
                if (!up_menu_item.equals("")) {
                    bundle.putString("Menu", up_menu_item);
                    intentItem.putExtras(bundle);
                }
                intentItem.setClass(PromotionActivity.this, PageActivity.class);
                break;
            case "ORDER_FORM":
                bundle = new Bundle();
                if (!up_menu_item.equals("")) {
                    bundle.putString("Menu", up_menu_item);
                    intentItem.putExtras(bundle);
                }
                intentItem.setClass(PromotionActivity.this, com.example.user.store3c.OrderFormActivity.class);
                break;
            default:
                //Toast.makeText(this.getBaseContext(), "Return to main menu ! ", Toast.LENGTH_SHORT).show();
                if (recentTaskPromotion) {
                    intentItem = Intent.makeMainActivity (new ComponentName(getApplicationContext(), MainActivity.class));
                    intentItem.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS);
                    retainRecentTaskBundle = new Bundle();
                    retainRecentTaskBundle.putString("RetainRecentTask", "RECENT_TASK");
                    intentItem.putExtras(retainRecentTaskBundle);
                }
                else {
                    intentItem.setClass(PromotionActivity.this, MainActivity.class);
                    intentItem.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
        }
        ActivityManager.AppTask currentTask = null;
        if (recentTaskPromotion) {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.AppTask> tasks;
            synchronized(tasks = am.getAppTasks()) {
                for (int i = 0; i < tasks.size(); i++) {
                    if (tasks.get(i).getTaskInfo().persistentId == getTaskId()) {
                        currentTask = tasks.get(i);
                    }
                }
            }
        }
        startActivity(intentItem);
        if (recentTaskPromotion && currentTask != null) {
            currentTask.finishAndRemoveTask();
        } else {
            PromotionActivity.this.finish();
        }
    }

}

class  UploadOrderToFirebaseTask extends AsyncTask<String, Void, Void> {
    private DatabaseReference promotionRef, amountRef, userUidRef, uidRef;
    private static int totalOrderAmount = 0;
    private String userId, userToken;
    private Map<String, Object> promotionValues;
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private AccountDbAdapter dbhelper;
    private final WeakReference<Context> weakRefContext;
    private final ArrayList<Integer> orderSet;
    private final int orderTableSize;

    UploadOrderToFirebaseTask(int orderTableSize, ArrayList<Integer> orderSet, Context activity) {
        this.orderTableSize = orderTableSize;
        this.orderSet = orderSet;
        weakRefContext = new WeakReference<>(activity);
    }

    protected Void doInBackground(String... params) {
        String totalPrice = params[0];
        Context contextActivity = weakRefContext.get();
        promotionRef = db.getReference("promotion");
        promotionRef.keepSynced(true);
        promotionRef.child("list").push();
        amountRef = promotionRef.child("promotionAmount").getRef();
        amountRef.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        dbhelper = new AccountDbAdapter(contextActivity);

        try{
            Thread.sleep(3000);
        }catch (Exception e) {
            e.printStackTrace();
        }
        amountRef.runTransaction(new Transaction.Handler() {
            @Override
            public @NonNull Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Integer counter = mutableData.getValue(Integer.class);
                if (counter == null) {
                    mutableData.setValue(1);
                    totalOrderAmount = 1;
                }
                else {
                    totalOrderAmount = counter + 1;
                    mutableData.setValue(totalOrderAmount);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.i("runTransaction===>", "postTransaction:onComplete: " + databaseError);
                if (databaseError != null) {
                    Log.i("runTransaction saved: ", "fail !" + databaseError.getMessage());
                    Toast.makeText(contextActivity, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.i("runTransaction saved: ", "successfully !");
                    //Toast.makeText(PromotionActivity.this, "Version: " + Build.VERSION.SDK_INT, Toast.LENGTH_SHORT).show();
                    final FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        userId = currentUser.getUid();
                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                userToken = task.getResult();
                                String userName = null, email = null;
                                if (!currentUser.isAnonymous()) {
                                    userName = currentUser.getDisplayName();
                                    email = currentUser.getEmail();
                                }
                                try {
                                    Date orderDate = new Date();
                                    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.TAIWAN);
                                    df.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                                    PromotionList newPromotion = new PromotionList(email, df.format(orderDate), OrderActivity.promotionListItem, totalPrice, userId, userName, userToken);
                                    promotionValues = newPromotion.toMap();
                                    String key = promotionRef.child("list").push().getKey();
                                    Map<String, Object> promotionChildUpdates = new HashMap<>();
                                    promotionChildUpdates.put("/list/" + key, promotionValues);
                                    promotionRef.updateChildren(promotionChildUpdates, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            if (databaseError != null) {
                                                Toast.makeText(contextActivity, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                Log.i("updateChildren saved: ", "fail !" + databaseError.getMessage());
                                            } else {
                                                Log.i("updateChildren saved: ", "successfully !");
                                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                                if (currentUser != null) {
                                                    String currentUserUid = currentUser.getUid();
                                                    userUidRef = db.getReference("/user/Uid/" + currentUserUid);
                                                    userUidRef.keepSynced(true);
                                                    userUidRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            String key = "uid";
                                                            if (mAuth.getCurrentUser() != null) {
                                                                key = mAuth.getCurrentUser().getUid();
                                                            }
                                                            String fbUid = dataSnapshot.getKey();
                                                            Log.i("Firebase ==>", "Firebase Uid is: " + fbUid);
                                                            uidRef = dataSnapshot.getRef();
                                                            String listKey = uidRef.child("orderList").push().getKey();
                                                            Map<String, Object> UidChildUpdates = new HashMap<>();

                                                            if (fbUid != null) {
                                                                if (fbUid.equals(key)) {
                                                                    UidChildUpdates.put("/orderList/" + listKey, promotionValues);
                                                                    uidRef.updateChildren(UidChildUpdates, new DatabaseReference.CompletionListener() {
                                                                        @Override
                                                                        public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                            if (databaseError != null) {
                                                                                Toast.makeText(contextActivity, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                Log.i("updateChildren saved: ", "fail !" + databaseError.getMessage());
                                                                            } else {
                                                                                Log.i("updateChildren saved: ", "successfully !");
                                                                                if (orderSet.size() > 0) {
                                                                                    if (dbhelper.deletePartOrder(orderTableSize, orderSet) == 0) {
                                                                                        Log.i("delete Order: ", "no data change!");
                                                                                    } else {
                                                                                        Toast.makeText(contextActivity, "Server會發送推播簡訊和E-mail訂單通知 !", Toast.LENGTH_LONG).show();
                                                                                    }
                                                                                }
                                                                                else {
                                                                                    Log.i("Order selected set : ", "no selected error !");
                                                                                }
                                                                                dbhelper.close();
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            // Failed to read value
                                                            Log.i("Firebase ==>", "Failed to read user data.", error.toException());
                                                            Toast.makeText(contextActivity, "DatabaseError, userRef, Uid: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    Toast.makeText(contextActivity, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }
                }
            }
        });
        return null;
    }

}
