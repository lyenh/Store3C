package com.example.user.store3c;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.example.user.store3c.MainActivity.mAuth;

public class PromotionActivity extends AppCompatActivity implements View.OnClickListener{

    private DatabaseReference promotionRef, userUidRef, uidRef;
    private static int totalOrderAmount = 0;
    private String totalPrice, userToken;
    private Map<String, Object> promotionValues;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private AccountDbAdapter dbhelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion);

        dbhelper = new AccountDbAdapter(this);
        Button ret_b;
        DatabaseReference amountRef;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            totalPrice = bundle.getString("totalPrice");
        }
        promotionRef = db.getReference("promotion");
        promotionRef.keepSynced(true);
        promotionRef.child("list").push();
        amountRef = promotionRef.child("promotionAmount").getRef();
        amountRef.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();

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
                    Toast.makeText(PromotionActivity.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.i("runTransaction saved: ", "successfully !");
                    //Toast.makeText(PromotionActivity.this, "Version: " + Build.VERSION.SDK_INT, Toast.LENGTH_SHORT).show();
                    final FirebaseUser currentUser = mAuth.getCurrentUser();
                    FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( new OnSuccessListener<InstanceIdResult>() {
                        @Override
                        public void onSuccess(InstanceIdResult instanceIdResult) {
                            userToken = instanceIdResult.getToken();
                            String userName = null, email = null;
                            if (currentUser != null && !currentUser.isAnonymous()) {
                                userName = currentUser.getDisplayName();
                                email = currentUser.getEmail();
                            }
                            try {
                                Date orderDate = new Date();
                                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.TAIWAN);
                                df.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                                PromotionList newPromotion = new PromotionList(email, userName, df.format(orderDate), totalPrice, userToken, OrderActivity.promotionListItem);
                                promotionValues = newPromotion.toMap();
                                String key = promotionRef.child("list").push().getKey();
                                Map<String, Object> promotionChildUpdates = new HashMap<>();
                                promotionChildUpdates.put("/list/" + key, promotionValues);
                                promotionRef.updateChildren(promotionChildUpdates, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        if (databaseError != null) {
                                            Toast.makeText(PromotionActivity.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                            Log.i("updateChildren saved: ", "fail !" + databaseError.getMessage());
                                        } else {
                                            Log.i("updateChildren saved: ", "successfully !");
                                            FirebaseUser currentUser = mAuth.getCurrentUser();
                                            if (currentUser != null ) {
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
                                                                            Toast.makeText(PromotionActivity.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                                            Log.i("updateChildren saved: ", "fail !" + databaseError.getMessage());
                                                                        } else {
                                                                            Log.i("updateChildren saved: ", "successfully !");
                                                                            if (!dbhelper.deleteAllOrder()) {
                                                                                Log.i("deleteAllOrder===>", "fail !");
                                                                            } else {
                                                                                Toast.makeText(PromotionActivity.this, "Server會發送推播簡訊和E-mail訂單通知 !", Toast.LENGTH_LONG).show();
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
                                                        Toast.makeText(PromotionActivity.this, "DatabaseError, userRef, Uid: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                Toast.makeText(PromotionActivity.this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        ret_b = findViewById(R.id.promotionRtn_id);
        ret_b.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Intent intentItem = new Intent();
        switch (v.getId()) {
            case R.id.promotionRtn_id:
                intentItem.setClass(PromotionActivity.this, MainActivity.class);
                startActivity(intentItem);
                PromotionActivity.this.finish();
                break;

        }
    }

    @Override
    public void onBackPressed() {
        Intent intentItem = new Intent();
        intentItem.setClass(PromotionActivity.this, MainActivity.class);
        startActivity(intentItem);
        PromotionActivity.this.finish();

    }

}
