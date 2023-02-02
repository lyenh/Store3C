package com.example.user.store3c;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import static com.example.user.store3c.MainActivity.mAuth;

/**
 * Created by user on 2018/2/12.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private AccountDbAdapter dbhelper = null;
    private String menu_item;
    private EditText LoginEmail, LoginPassword;
    private String user = "", email = "", password = "";
    private FirebaseAuth.AuthStateListener authListener;
    private int index=0;
    private String dbUserName, dbUserPassword, dbUserEmail;
    private byte[] userPicture;
    private Bitmap userImg;
    private FirebaseDatabase db = null;
    private DatabaseReference UidRef, uidRef, userTokenRef, userUidRef;
    private String userToken;
    private Boolean updateTokenData = false, updateUserData = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button LoginButton;
        Intent intentItem = getIntent();
        Bundle bundleItem = intentItem.getExtras();

        if (bundleItem != null) {
            menu_item = bundleItem.getString("Menu");
        }
        String edit_Title = "使用者登入";
        if (getSupportActionBar() != null) {
            this.getSupportActionBar().setTitle(edit_Title);
        }
        LoginEmail = findViewById(R.id.loginEmail_id);
        LoginPassword = findViewById(R.id.loginPassword_id);
        LoginButton = findViewById(R.id.login_id);
        LoginButton.setOnClickListener(this);

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };

    }

    void loginSuccess() {
        Toast.makeText(LoginActivity.this, "登入成功 ! ", Toast.LENGTH_SHORT).show();
        Intent intentItem = new Intent();
        switch (menu_item) {
            case "DISH":
                intentItem.setClass(LoginActivity.this, MainActivity.class);
                intentItem.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                break;
            case "CAKE":
                intentItem.setClass(LoginActivity.this, CakeActivity.class);
                break;
            case "PHONE":
                intentItem.setClass(LoginActivity.this, PhoneActivity.class);
                break;
            case "CAMERA":
                intentItem.setClass(LoginActivity.this, CameraActivity.class);
                break;
            case "BOOK":
                intentItem.setClass(LoginActivity.this, BookActivity.class);
                break;
            default:
                Toast.makeText(this.getBaseContext(), "Return to main menu ! ", Toast.LENGTH_SHORT).show();
                intentItem.setClass(LoginActivity.this, MainActivity.class);
                intentItem.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        startActivity(intentItem);
        LoginActivity.this.finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(authListener);
        dbhelper = new AccountDbAdapter(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(authListener);
        dbhelper.close();
    }

    @Override
    public void onClick(View v) {
        email = LoginEmail.getText().toString();
        password = LoginPassword.getText().toString();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null || currentUser.isAnonymous()) {
            if (!email.isEmpty() && !password.isEmpty()) {
                mAuth.signInWithEmailAndPassword(email, password).
                        addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.i("signInWithEmail: ", "success");

                                    final FirebaseUser currentUser = mAuth.getCurrentUser();

                                    if (currentUser != null && !currentUser.isAnonymous()) {
                                        db = FirebaseDatabase.getInstance();
                                        UidRef = db.getReference("/user/Uid");
                                        UidRef.keepSynced(true);
                                        userTokenRef = db.getReference("userToken");
                                        userTokenRef.keepSynced(true);
                                        user = currentUser.getDisplayName();

                                        if (!dbhelper.IsDbUserEmpty()) {
                                            try {
                                                Cursor cursor = dbhelper.getSimpleUserData();
                                                index = cursor.getInt(0);
                                                dbUserName = cursor.getString(1);
                                                dbUserEmail = cursor.getString(2);
                                                dbUserPassword = cursor.getString(3);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            if (!user.equals(dbUserName) || !password.equals(dbUserPassword) || !email.equals(dbUserEmail)) {
                                                if (dbhelper.updateUserSimpleData(index, user, email, password) == 0) {
                                                    Log.i("update User: ", "no data change!");
                                                }
                                            }
                                        }
                                        else {
                                            userImg = BitmapFactory.decodeResource(getResources(), R.drawable.user_icon);
                                            userPicture = Bitmap2Bytes(userImg);
                                            if (dbhelper.createUser(user, email, password, userPicture) == -1) {
                                                Log.i("create User: ", "fail !");
                                            }
                                        }

                                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                            @Override
                                            public void onComplete(@NonNull Task<String> task) {
                                                userToken = task.getResult();

                                                userTokenRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        DataSnapshot tokenSnapshot = dataSnapshot.child("token");
                                                        Iterable<DataSnapshot> tokenChildren = tokenSnapshot.getChildren();

                                                        for (DataSnapshot token : tokenChildren) {
                                                            String key = token.getKey();
                                                            UserItem c = token.getValue(UserItem.class);
                                                            //Toast.makeText(UserActivity.this, "token: " + c.getUserToken(), Toast.LENGTH_SHORT).show();
                                                            if (c != null) {
                                                                if (c.getUserToken() != null) {
                                                                    Log.d("user: ", c.getUserToken() + "  " + c.getUserEmail() + "  " + c.getUserName());
                                                                    if (c.getUserToken().equals(userToken) && (!c.getUserName().equals(user) || !c.getUserEmail().equals(email))) {
                                                                        //Toast.makeText(UserActivity.this, "update: ", Toast.LENGTH_SHORT).show();
                                                                        updateTokenData = true;
                                                                        UserItem newUser = new UserItem(Objects.requireNonNull(mAuth.getCurrentUser()).getUid(), userToken, email, user);
                                                                        Map<String, Object> userValues = newUser.toMap();
                                                                        Map<String, Object> userUpdates = new HashMap<>();
                                                                        userUpdates.put("/token/" + key, userValues);
                                                                        userTokenRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                                                            @Override
                                                                            public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                                if (databaseError != null) {
                                                                                    //Toast.makeText(LoginActivity.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                    Log.i("updateChildren saved: ", "fail !" + databaseError.getMessage());
                                                                                } else {
                                                                                    Log.i("updateChildren saved: ", "successfully !");
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        // Failed to read value
                                                        Log.i("Firebase ==>", "Failed to read user data.", error.toException());
                                                        Toast.makeText(LoginActivity.this, "DatabaseError, userRef, token: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                                String currentUserUid = currentUser.getUid();
                                                userUidRef = db.getReference("/user/Uid/" + currentUserUid);
                                                userUidRef.keepSynced(true);

                                                userUidRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        final boolean userAccountExist = true;
                                                        String key = currentUser.getUid();
                                                        Log.i("Firebase ==>", "Firebase current user Uid is: " + key);
                                                        UserInfo.UserDeviceInfo deviceInfo;
                                                        String fbUid = dataSnapshot.getKey();
                                                        Log.i("Firebase ==>", "Firebase Uid is: " + fbUid);
                                                        if (fbUid != null) {
                                                            if (fbUid.equals(key)) {
                                                                updateUserData = true;
                                                                String Manufacturer = Build.MANUFACTURER;       //samsung
                                                                String Model = android.os.Build.MODEL;            // SM-J700F
                                                                int SDKversion = Build.VERSION.SDK_INT;             // 22
                                                                uidRef = dataSnapshot.getRef();

                                                                deviceInfo = new UserInfo.UserDeviceInfo(Manufacturer, Model, SDKversion);
                                                                Date loginDate = new Date();
                                                                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.TAIWAN);
                                                                df.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                                                                UserInfo newUser = new UserInfo(df.format(loginDate), userToken, email, user, password, userAccountExist, deviceInfo);
                                                                Map<String, Object> userValues = newUser.toMap();
                                                                Map<String, Object> userUpdates = new HashMap<>();
                                                                userUpdates.put("/userInfo/", userValues);
                                                                uidRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                                                    @Override
                                                                    public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                        if (databaseError != null) {
                                                                            Toast.makeText(LoginActivity.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                                            Log.i("updateChildren saved: ", "fail !" + databaseError.getMessage());
                                                                        } else {
                                                                            Log.i("updateChildren saved: ", "successfully !");
                                                                        }
                                                                        loginSuccess();
                                                                    }
                                                                });

                                                            }
                                                        }
                                                        if (!updateTokenData) {
                                                            if (!updateUserData) {
                                                                loginSuccess();
                                                            }
                                                        }
                                                        else {
                                                            if (!updateUserData) {
                                                                loginSuccess();
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        // Failed to read value
                                                        Log.i("Firebase ==>", "Failed to read user data.", error.toException());
                                                        Toast.makeText(LoginActivity.this, "DatabaseError, userRef, Uid: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.i("signInWithEmail:", "failure", task.getException());
                                    if (task.getException() != null) {
                                        Toast.makeText(LoginActivity.this, "登入失敗: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        Toast.makeText(LoginActivity.this, "登入失敗: ", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
            }
            else {
                Toast.makeText(this, "請輸入資料 !", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "使用者已有登入 ! ", Toast.LENGTH_SHORT).show();
        }

    }

    private static byte [] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos =  new  ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,  100 , baos);
        return  baos.toByteArray();
    }

    @Override
    public void onBackPressed() {
        Intent intent;
        intent = new Intent();
        switch (menu_item) {
            case "DISH":
                intent.setClass(LoginActivity.this, MainActivity.class);
                intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                break;
            case "CAKE":
                intent.setClass(LoginActivity.this, CakeActivity.class);
                break;
            case "PHONE":
                intent.setClass(LoginActivity.this, PhoneActivity.class);
                break;
            case "CAMERA":
                intent.setClass(LoginActivity.this, CameraActivity.class);
                break;
            case "BOOK":
                intent.setClass(LoginActivity.this, BookActivity.class);
                break;
            default:
                Toast.makeText(this.getBaseContext(), "Return to main menu ! ", Toast.LENGTH_SHORT).show();
                intent.setClass(LoginActivity.this, MainActivity.class);
                intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        startActivity(intent);
        LoginActivity.this.finish();

    }



}
