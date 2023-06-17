package com.example.user.store3c;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static androidx.core.content.FileProvider.getUriForFile;
import static com.example.user.store3c.MainActivity.mAuth;
import static com.example.user.store3c.MainActivity.userImg;

public class UserActivity extends AppCompatActivity implements View.OnClickListener {
    private AccountDbAdapter dbHelper = null;
    private EditText editName = null;
    private EditText editPassword = null;
    private EditText editEmail = null;
    private Button btnMode = null;
    private ImageView userPicture = null;
    private Intent intent = null;
    private String userName, dbUserName;
    private String userPassword, dbUserPassword;
    private String userEmail, dbUserEmail;
    private String menu_item;
    private int index=0;
    private FirebaseDatabase db = null;
    private DatabaseReference userRef, userTokenRef;
    private boolean existUser = false;
    private int totalUidAmount = 0;
    private String userToken;
    private boolean relogin = false;
    private boolean userAccountExist = true;
    private String key = "uid";
    private final int MaxByte = 512*1024;
    private UserInfo.UserDeviceInfo deviceInfo;
    private Bitmap selectedUserImg;
    private Uri imageUri = null, fileUri = null;
    private File filePath, file;
    private ActivityResultLauncher<Intent> loadImgResultLauncher, cropImgResultLauncher;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<CropImageContractOptions> cropImage =
            registerForActivityResult(new CropImageContract(),
                    result -> {
                        if (result.isSuccessful()) {
                            if (Objects.equals(Objects.requireNonNull(result.getUriContent()).getPath(), fileUri.getPath())) {
                                Log.i("Crop  ==>", "output file is the same !");
                            }
                            else {
                                Log.i("Crop  ==>", "output file is different !");
                            }
                            cropImgSave(result.getUriFilePath(UserActivity.this, false), result.getUriContent());
                        } else if (result.equals(CropImage.CancelledResult.INSTANCE)) {
                            Toast.makeText(UserActivity.this, "You haven't picked Image !", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UserActivity.this, "Cropping image failed !", Toast.LENGTH_SHORT).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        String osVersion, osName, Device, Product, Hardware, Manufacturer, Model;
        int SDKversion;
        Button btnName, btnEmail, btnPassword, btnPicture;
        Intent intentItem = getIntent();
        Bundle bundleItem = intentItem.getExtras();

        if (bundleItem != null) {
            menu_item = bundleItem.getString("Menu");
        }
        dbHelper = new AccountDbAdapter(this);

        editName = findViewById(R.id.name_id);
        editPassword = findViewById(R.id.password_id);
        editEmail = findViewById(R.id.email_id);
        btnName = findViewById(R.id.nameBtn_id);
        btnEmail = findViewById(R.id.emailBtn_id);
        btnPassword = findViewById(R.id.passwordBtn_id);
        btnMode = findViewById(R.id.userModeBtn_id);
        btnPicture = findViewById((R.id.pictureBtn_id));
        userPicture = findViewById(R.id.userPicture_id);
        btnName.setOnClickListener(this);
        btnEmail.setOnClickListener(this);
        btnPassword.setOnClickListener(this);
        btnMode.setOnClickListener(this);
        btnPicture.setOnClickListener(this);
        selectedUserImg = BitmapFactory.decodeResource(getResources(), R.drawable.user_icon);
       // String edit_Title = "使用者資料";
    //    this.getSupportActionBar().setTitle(edit_Title);

        osVersion = System.getProperty("os.version");      // 3.10.61-1150266
        osName = System.getProperty("os.name");           //Linux
        Device  = android.os.Build.DEVICE;            // j7elte
        Product = android.os.Build.PRODUCT;          // j7eltezt
        Hardware = Build.HARDWARE;                  //samsungexynos7580

        Manufacturer = Build.MANUFACTURER;       //samsung
        Model = android.os.Build.MODEL;            // SM-J700F
        SDKversion = Build.VERSION.SDK_INT;             // 22
        deviceInfo = new UserInfo.UserDeviceInfo(Manufacturer, Model, SDKversion);

        String info = "osVersion: " + osVersion+ ", osName: " + osName+ ", SDKversion: "
                + SDKversion+ ", Device: " + Device+ ", Model: " + Model+ ", Product: " +
                Product+ ", Hardware: " + Hardware+ ", Manufacturer: " + Manufacturer;
        Log.i("DeviceInfo ==>", "DeviceInfo: " + info);

        if (InternetConnection.checkConnection(UserActivity.this)) {
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (!dbHelper.IsDbUserEmpty()) {
                try {
                    Cursor cursor = dbHelper.getSimpleUserData();
                    index = cursor.getInt(0);
                    dbUserName = cursor.getString(1);
                    dbUserEmail = cursor.getString(2);
                    dbUserPassword = cursor.getString(3);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(UserActivity.this, "get picture error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                if (userImg != null) {
                    selectedUserImg = userImg;
                }
                db = FirebaseDatabase.getInstance();
                userRef = db.getReference("user");
                userRef.keepSynced(true);

                if (currentUser != null) {
                    if (currentUser.isAnonymous()) {
                        AuthCredential credential = EmailAuthProvider.getCredential(dbUserEmail, dbUserPassword);
                        currentUser.linkWithCredential(credential)
                                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Log.i("Credential===>", "linkWithCredential:success");
                                            final FirebaseUser currentUser = mAuth.getCurrentUser();
                                            if (currentUser != null) {
                                                currentUser.delete()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.i("User Account===>", "User account: delete");
                                                                    //Toast.makeText(UserActivity.this, "資料已刪除 !", Toast.LENGTH_LONG).show();
                                                                    setNonUserAccountText();
                                                                } else {
                                                                    //Toast.makeText(UserActivity.this, "登入已久, 需重新登入, 再執行刪除.", Toast.LENGTH_LONG).show();
                                                                    AuthCredential credential = EmailAuthProvider.getCredential(userEmail, userPassword);
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
                                                                                                            setNonUserAccountText();
                                                                                                        } else {
                                                                                                            Log.i("check User Account===>", "User account: delete fail");
                                                                                                            Toast.makeText(UserActivity.this, "測試資料未刪除 !", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        } else {
                                            Log.i("Credential===>", "linkWithCredential:failure", task.getException());
                                            //Toast.makeText(UserActivity.this, "Authentication failed: need logout, firstly !", Toast.LENGTH_SHORT).show();
                                            setExistUserText();
                                        }

                                    }
                                });
                    }
                    else {
                        mAuth.createUserWithEmailAndPassword(dbUserEmail, dbUserPassword)
                                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Log.i("CreateUserAccount===>", "Create User Account: success");
                                            final FirebaseUser currentUser = mAuth.getCurrentUser();
                                            if (currentUser != null ) {
                                                currentUser.delete()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.i("User Account===>", "User account: delete");
                                                                    //Toast.makeText(UserActivity.this, "資料已刪除 !", Toast.LENGTH_LONG).show();
                                                                    setNonUserAccountText();
                                                                } else {
                                                                    //Toast.makeText(UserActivity.this, "登入已久, 需重新登入, 再執行刪除.", Toast.LENGTH_LONG).show();
                                                                    AuthCredential credential = EmailAuthProvider.getCredential(userEmail, userPassword);

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
                                                                                                            setNonUserAccountText();
                                                                                                        } else {
                                                                                                            Log.i("check User Account===>", "User account: delete fail");
                                                                                                            Toast.makeText(UserActivity.this, "測試資料未刪除 !", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        } else {
                                            Log.i("CreateUserAccount===>", "Create User Account: failure", task.getException());
                                            //Toast.makeText(UserActivity.this, "Create User Account: failure !", Toast.LENGTH_SHORT).show();
                                            setExistUserText();
                                        }
                                    }
                                });
                    }
                } else {
                    mAuth.createUserWithEmailAndPassword(dbUserEmail, dbUserPassword)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Log.i("CreateUserAccount===>", "Create User Account: success");
                                        final FirebaseUser currentUser = mAuth.getCurrentUser();
                                        if (currentUser != null) {
                                            currentUser.delete()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Log.i("User Account===>", "User account: delete");
                                                                //Toast.makeText(UserActivity.this, "資料已刪除 !", Toast.LENGTH_LONG).show();
                                                                setNonUserAccountText();
                                                            } else {
                                                                //Toast.makeText(UserActivity.this, "登入已久, 需重新登入, 再執行刪除.", Toast.LENGTH_LONG).show();
                                                                AuthCredential credential = EmailAuthProvider.getCredential(userEmail, userPassword);

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
                                                                                                        setNonUserAccountText();
                                                                                                    } else {
                                                                                                        Log.i("check User Account===>", "User account: delete fail");
                                                                                                        Toast.makeText(UserActivity.this, "測試資料未刪除 !", Toast.LENGTH_SHORT).show();
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                        }
                                    } else {
                                        Log.i("CreateUserAccount===>", "Create User Account: failure", task.getException());
                                        //Toast.makeText(UserActivity.this, "Create User Account: failure !", Toast.LENGTH_SHORT).show();
                                        setExistUserText();
                                    }
                                }
                            });
                }
            } else {
                existUser = false;
                btnMode.setText("新增");
                Toast.makeText(UserActivity.this, "請建立使用者資料! ", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(UserActivity.this, "網路未連線! ", Toast.LENGTH_SHORT).show();
        }

        loadImgResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            if (data != null){
                                imageUri = data.getData();
                            }
                            if (imageUri != null) {
                                Log.i("===> Image Uri: ", imageUri.toString());
                                //Toast.makeText(UserActivity.this, "Uri: " + imageUri.toString(), Toast.LENGTH_LONG).show();
                                try {
                                    file = new File(getExternalFilesDir(null), "image");
                                    //file = new File(filePath, "faceImg");
                                    Log.i("===> File Path: ", file.getPath());
                                    Log.i("===> File Uri: ", Uri.fromFile(file).toString());

                                    if (file.exists()) {
                                        if (file.delete()) {
                                            if (file.createNewFile()) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                    // Android 7.0 : file access per mission
                                                    fileUri = getUriForFile(UserActivity.this,
                                                            "com.example.user.store3c.fileProvider", file);
                                                    Log.i("===> Manifest FileUri:", fileUri.toString());
                                                } else {
                                                    fileUri = Uri.fromFile(file);
                                                }
                                            }
                                        }
                                    }
                                    else {
                                        if (file.createNewFile()) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                // Android 7.0 : file access permission
                                                fileUri = getUriForFile(UserActivity.this,
                                                        "com.example.user.store3c.fileProvider", file);
                                                Log.i("===> Manifest FileUri:", fileUri.toString());
                                            } else {
                                                fileUri = Uri.fromFile(file);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(UserActivity.this, "file I/O error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }

                                try {
                                    Intent cropIntent = new Intent("com.android.camera.action.CROP");
                                    cropIntent.setType("image/*");
                                    List<ResolveInfo> list = getPackageManager().queryIntentActivities(cropIntent, PackageManager.MATCH_DEFAULT_ONLY);
                                    int size = list.size();
                                    //Toast.makeText(this, "There are " + size + " image crop app", Toast.LENGTH_LONG).show();
                                    if (size == 0 || imageUri.toString().startsWith("content://com.google.android.apps.photos.content")) {
                                        try {
                                            CropImageContractOptions options = new CropImageContractOptions(imageUri, new CropImageOptions())
                                                    .setScaleType(CropImageView.ScaleType.FIT_CENTER)
                                                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                                                    .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                                                    .setAutoZoomEnabled(true)
                                                    .setRequestedSize(360, 360)
                                                    .setAllowRotation(true)
                                                    .setAllowFlipping(true)
                                                    .setNoOutputImage(false)
                                                    .setOutputUri(fileUri);
                                            cropImage.launch(options);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Toast.makeText(UserActivity.this, "decodeFile error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                        //Toast.makeText(this, "There are no default image crop app, using cropImage api", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (fileUri != null) {
                                            cropIntent.setData(imageUri);
                                            cropIntent.putExtra("crop", "true");
                                            cropIntent.putExtra("circleCrop", "true");
                                            cropIntent.putExtra("outputX", 360);
                                            cropIntent.putExtra("outputY", 360);
                                            cropIntent.putExtra("aspectX", 1);
                                            cropIntent.putExtra("aspectY", 1);
                                            cropIntent.putExtra("scale", "true");
                                            cropIntent.putExtra("return-data", "false");
                                            cropIntent.putExtra("output", fileUri);
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                                        | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                            }
                                            //if (size > 1) {
                                            //Toast.makeText(this, "There are more than one image crop app: " + size, Toast.LENGTH_LONG).show();
                                            //}
                                            Intent picIntent = new Intent(cropIntent);
                                            ResolveInfo res = list.get(0);
                                            String packageName = res.activityInfo.packageName;
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                UserActivity.this.grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                UserActivity.this.grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                picIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                            }
                                            picIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                                            cropImgResultLauncher.launch(picIntent);
                                        }
                                    }
                                }
                                catch (ActivityNotFoundException e) {
                                    String errorMessage = "your device doesn't support the crop action!";
                                    Toast toast = Toast.makeText(UserActivity.this, errorMessage, Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }
                        }
                    }
                });

        cropImgResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            if (data != null && fileUri != null){
                                try {
                                    ParcelFileDescriptor parcelFileDescriptor =
                                            getContentResolver().openFileDescriptor(fileUri, "r");
                                    if (parcelFileDescriptor != null) {
                                        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                                        selectedUserImg = BitmapFactory.decodeFileDescriptor(fileDescriptor,null, null);
                                        parcelFileDescriptor.close();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(UserActivity.this, "decodeFile error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                            selectedUserImg = getRoundedCroppedBitmap(selectedUserImg);
                            userPicture.setImageBitmap(selectedUserImg);

                            byte[] img = Bitmap2Bytes(selectedUserImg);
                            if (img.length > MaxByte) {
                                try {
                                    ParcelFileDescriptor parcelFileDescriptor =
                                            getContentResolver().openFileDescriptor(fileUri, "r");

                                    executor.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            Bitmap image;
                                            try {
                                                int targetW = 200;
                                                int targetH = 200;

                                                // Get the dimensions of the bitmap
                                                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                                bmOptions.inJustDecodeBounds = true;

                                                if (parcelFileDescriptor != null) {
                                                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                                                    BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmOptions);

                                                    int photoW = bmOptions.outWidth;
                                                    int photoH = bmOptions.outHeight;

                                                    // Determine how much to scale down the image
                                                    int scaleFactor = 1;

                                                    if (photoH > targetH || photoW > targetW) {
                                                        final int halfHeight = photoH / 2;
                                                        final int halfWidth = photoW / 2;

                                                        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                                                        // height and width larger than the requested height and width.
                                                        while ((halfHeight / scaleFactor) >= targetH
                                                                && (halfWidth / scaleFactor) >= targetW) {
                                                            scaleFactor *= 2;
                                                        }
                                                    }

                                                    // Decode the image file into a Bitmap sized to fill the View
                                                    bmOptions.inJustDecodeBounds = false;
                                                    bmOptions.inSampleSize = scaleFactor;
                                                    image = BitmapFactory.decodeFileDescriptor(fileDescriptor,null, bmOptions);

                                                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                                    image.compress(Bitmap.CompressFormat.JPEG, 100 , bos);  //ignored for PNG
                                                    byte[] bitmapdata = bos.toByteArray();
                                                    FileOutputStream fos = new FileOutputStream(file);
                                                    fos.write(bitmapdata);
                                                    fos.flush();
                                                    fos.close();
                                                    parcelFileDescriptor.close();
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                Toast.makeText(UserActivity.this, "decodeFile error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            }

                                            handler.post(() -> ResizeCropImgSave(file));
                                        }
                                    });
                                    Log.i("Resize ==>", "file resize !");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(UserActivity.this, "decodeFile error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                            else {
                                //Toast.makeText(UserActivity.this, "Image size: " + img.length, Toast.LENGTH_LONG).show();
                                if (dbUserName == null || dbUserEmail == null || dbUserPassword == null) {
                                    Toast.makeText(UserActivity.this, "檔案已載入, 請新增使用者資料", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    if (dbHelper.updateUser(index, dbUserName, dbUserEmail, dbUserPassword, img) == 0) {
                                        Toast.makeText(UserActivity.this, "db update error", Toast.LENGTH_SHORT).show();
                                        Log.i("update User: ", "no data change!");
                                    }
                                    else {
                                        MainActivity.userImg = selectedUserImg;
                                        if (file.exists()) {
                                            if (file.delete()) {
                                                Log.i("===> Deleted File Path:", file.getPath());
                                            }
                                        }
                                    }
                                    //Toast.makeText(UserActivity.this, "image size: " + img.length/1024 + "k", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        else if (result.getResultCode() == RESULT_CANCELED && result.getData() == null) {
                            try {
                                CropImageContractOptions options = new CropImageContractOptions(imageUri, new CropImageOptions())
                                        .setScaleType(CropImageView.ScaleType.FIT_CENTER)
                                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                                        .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                                        .setAutoZoomEnabled(true)
                                        .setRequestedSize(360, 360)
                                        .setAllowRotation(true)
                                        .setAllowFlipping(true)
                                        .setNoOutputImage(false)
                                        .setOutputUri(fileUri);
                                cropImage.launch(options);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(UserActivity.this, "Crop image error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                Log.i("CropImgKt error ===>", Objects.requireNonNull(e.getMessage()));
                            }
                            //Toast.makeText(this, "There are no useful default image crop app", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public void setExistUserText() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && !currentUser.isAnonymous()) {
            if (dbUserName.equals(currentUser.getDisplayName())) {
                editName.setText(dbUserName);
            } else {
                Log.i("Get user data===>", "User name not matched");
                editName.setText(currentUser.getDisplayName());
                if (dbHelper.updateUser(index, currentUser.getDisplayName(), currentUser.getEmail(), dbUserPassword, Bitmap2Bytes(selectedUserImg)) == 0) {
                    Log.i("update User: ", "no data change!");
                }
            }
            if (dbUserEmail.equals(currentUser.getEmail())) {
                editEmail.setText(dbUserEmail);
            } else {
                Log.i("Get user data===>", "User Email not matched");
                editEmail.setText(currentUser.getEmail());
                if (dbHelper.updateUser(index, currentUser.getDisplayName(), currentUser.getEmail(), dbUserPassword, Bitmap2Bytes(selectedUserImg)) == 0) {
                    Log.i("update User: ", "no data change!");
                }
            }
            editPassword.setText(dbUserPassword);
        } else {
            editName.setText(dbUserName);
            editEmail.setText(dbUserEmail);
            editPassword.setText(dbUserPassword);
        }
        //userPicture.setLayoutParams(new LinearLayout.LayoutParams(230, 260));
        userPicture.setImageBitmap(selectedUserImg);
        existUser = true;
        btnMode.setText("刪除");

    }

    private byte [] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos =  new  ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,  100 , baos);
        return  baos.toByteArray();
    }

    public void setNonUserAccountText() {
        editName.setText(dbUserName);
        editEmail.setText(dbUserEmail);
        editPassword.setText(dbUserPassword);
        existUser = false;
        btnMode.setText("新增");
    }

    private void addFbUidData() {
        final DatabaseReference totalUidRef = userRef.child("registerUidAmount").getRef();
        userRef = db.getReference("user");
        userRef.keepSynced(true);
        totalUidRef.keepSynced(true);
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                userToken = task.getResult();
                totalUidRef.runTransaction(new Transaction.Handler() {
                    @Override
                    public @NonNull Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        Integer counter = mutableData.getValue(Integer.class);
                        boolean findUid = false;
                        String key = "uid";

                        if (counter == null) {
                            Log.i("Firebase ==>", "Total Uid Amount is: null");
                        }
                        else {
                            totalUidAmount = counter;
                            Log.i("Firebase ==>", "Total Uid Amount is: " + totalUidAmount);
                            if (totalUidAmount == 0) {
                                userRef.child("Uid").push();
                                if (mAuth.getCurrentUser() != null) {
                                    key = mAuth.getCurrentUser().getUid();
                                    userRef.child("Uid").child(key).child("userInfo").push();
                                }
                                //userRef.child("token").child(key).setValue(new UserInfo(refreshedToken, "defaultEmail", "defaultName"));
                                Date loginDate = new Date();
                                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.TAIWAN);
                                df.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                                UserInfo newUser = new UserInfo(df.format(loginDate), userToken, userEmail, userName, userPassword, userAccountExist, deviceInfo);
                                Map<String, Object> userValues = newUser.toMap();
                                Map<String, Object> userUpdates = new HashMap<>();
                                userUpdates.put("/Uid/" + key + "/userInfo/", userValues);
                                userRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        if (databaseError != null) {
                                            Toast.makeText(UserActivity.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                            Log.i("updateChildren saved: ", "fail !" + databaseError.getMessage());
                                        } else {
                                            Log.i("updateChildren saved: ", "successfully !");
                                        }
                                    }
                                });
                                mutableData.setValue(++totalUidAmount);
                            }
                            else {
                                userRef.child("Uid").push();
                                if (mAuth.getCurrentUser() != null) {
                                    key = mAuth.getCurrentUser().getUid();
                                    userRef.child("Uid").child(key).child("userInfo").push();
                                }
                                MutableData uidSnapshot = mutableData.child("Uid");
                                Iterable<MutableData> uidChildren = uidSnapshot.getChildren();
                                for (MutableData uid : uidChildren) {
                                    String fbUid = uid.getKey();
                                    Log.i("Firebase ==>", "Firebase Uid is: " + fbUid);
                                    if (fbUid != null) {
                                        if (fbUid.equals(key)) {
                                            findUid = true;
                                        }
                                    }
                                }
                                if (!findUid) {
                                    totalUidAmount++;

                                    //userRef.child("token").child(key).setValue(new UserItem(refreshedToken, "defaultEmail", "defaultName"));
                                    Date loginDate = new Date();
                                    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.TAIWAN);
                                    df.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                                    UserInfo newUser = new UserInfo(df.format(loginDate), userToken, userEmail, userName, userPassword, userAccountExist, deviceInfo);
                                    Map<String, Object> userValues = newUser.toMap();
                                    Map<String, Object> userUpdates = new HashMap<>();
                                    userUpdates.put("/Uid/" + key + "/userInfo/", userValues);
                                    userRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            if (databaseError != null) {
                                                Toast.makeText(UserActivity.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                Log.i("updateChildren saved: ", "fail !" + databaseError.getMessage());
                                            } else {
                                                Log.i("updateChildren saved: ", "successfully !");
                                            }
                                        }
                                    });
                                    mutableData.setValue(totalUidAmount);
                                }
                            }
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
                            Toast.makeText(UserActivity.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            Log.i("runTransaction saved: ", "successfully !");
                            //Toast.makeText(PromotionActivity.this, "Version: " + Build.VERSION.SDK_INT, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

    public void addNewUserAccount() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        userTokenRef = db.getReference("userToken");
        userTokenRef.keepSynced(true);
        if (currentUser != null) {
            if (currentUser.isAnonymous()) {
                AuthCredential credential = EmailAuthProvider.getCredential(userEmail, userPassword);
                currentUser.linkWithCredential(credential)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.i("Credential===>", "linkWithCredential:success");
                                    FirebaseUser user = task.getResult().getUser();
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(userName).build();
                                    if (user != null) {
                                        user.updateProfile(profileUpdates)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.i("Profile===>", "User profile updated.");
                                                            //Toast.makeText(UserActivity.this, "profile updated. ", Toast.LENGTH_SHORT).show();
                                                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<String> task) {
                                                                    final String refreshedToken = task.getResult();

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
                                                                                        //Toast.makeText(UserActivity.this, "refreshed: " + refreshedToken, Toast.LENGTH_SHORT).show();
                                                                                        if (c.getUserToken().equals(refreshedToken)) {
                                                                                            //Toast.makeText(UserActivity.this, "update: ", Toast.LENGTH_SHORT).show();
                                                                                            UserItem newUser = new UserItem(Objects.requireNonNull(mAuth.getCurrentUser()).getUid(), refreshedToken, userEmail, userName);
                                                                                            Map<String, Object> userValues = newUser.toMap();
                                                                                            Map<String, Object> userUpdates = new HashMap<>();
                                                                                            userUpdates.put("/token/" + key, userValues);
                                                                                            userTokenRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                                                                                @Override
                                                                                                public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                                                    if (databaseError != null) {
                                                                                                        Toast.makeText(UserActivity.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
                                                                            Toast.makeText(UserActivity.this, "DatabaseError, userRef, token: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                }
                                                            });

                                                            addFbUidData();

                                                            if (dbHelper.createUser(userName, userEmail, userPassword, Bitmap2Bytes(selectedUserImg)) == -1) {
                                                                Log.i("create User: ", "fail !");
                                                            }
                                                            Toast.makeText(UserActivity.this, "資料已建立", Toast.LENGTH_SHORT).show();
                                                            intent = getIntent();
                                                            finish();
                                                            startActivity(intent);
                                                        } else {
                                                            Log.i("Profile===>", "User profile update fail.");
                                                        }
                                                    }
                                                });
                                    }
                                } else {
                                    Log.i("Credential===>", "linkWithCredential:failure", task.getException());
                                    try {
                                        if (task.getException() != null) {
                                            String errMessage = task.getException().getMessage();
                                            if (errMessage != null) {
                                                Toast.makeText(UserActivity.this, "新增錯誤: " + errMessage, Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(UserActivity.this, "無法新增 !", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(UserActivity.this, "無法新增 !", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            }
                        });
            }
        } else {
            mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.i("CreateUserAccount===>", "Create User Account: success");
                                FirebaseUser user = task.getResult().getUser();
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(userName).build();
                                if (user != null) {
                                    user.updateProfile(profileUpdates)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.i("Profile===>", "User profile update success.");
                                                        //Toast.makeText(UserActivity.this, "profile updated. ", Toast.LENGTH_SHORT).show();
                                                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<String> task) {
                                                                final String refreshedToken = task.getResult();

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
                                                                                    //Toast.makeText(UserActivity.this, "refreshed: " + refreshedToken, Toast.LENGTH_SHORT).show();
                                                                                    if (c.getUserToken().equals(refreshedToken)) {
                                                                                        //Toast.makeText(UserActivity.this, "update: ", Toast.LENGTH_SHORT).show();
                                                                                        UserItem newUser = new UserItem(Objects.requireNonNull(mAuth.getCurrentUser()).getUid(), refreshedToken, userEmail, userName);
                                                                                        Map<String, Object> userValues = newUser.toMap();
                                                                                        Map<String, Object> userUpdates = new HashMap<>();
                                                                                        userUpdates.put("/token/" + key, userValues);
                                                                                        userTokenRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                                                                            @Override
                                                                                            public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                                                if (databaseError != null) {
                                                                                                    Toast.makeText(UserActivity.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
                                                                        Toast.makeText(UserActivity.this, "DatabaseError, userRef, token: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            }
                                                        });

                                                        addFbUidData();

                                                        if (dbHelper.createUser(userName, userEmail, userPassword, Bitmap2Bytes(selectedUserImg)) == -1) {
                                                            Log.i("create User: ", "fail !");
                                                        }
                                                        Toast.makeText(UserActivity.this, "資料已建立", Toast.LENGTH_SHORT).show();
                                                        intent = getIntent();
                                                        finish();
                                                        startActivity(intent);
                                                    } else {
                                                        Log.i("Profile===>", "User profile update fail.");
                                                    }
                                                }
                                            });
                                }
                            } else {
                                Log.i("CreateUserAccount===>", "Create User Account: failure", task.getException());
                                try {
                                    if (task.getException() != null) {
                                        String errMessage = task.getException().getMessage();
                                        if (errMessage != null) {
                                            Toast.makeText(UserActivity.this, "新增錯誤: " + errMessage, Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(UserActivity.this, "無法新增 !", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(UserActivity.this, "無法新增 !", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        }

    }

    public void checkAndSetFbUserAccount() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        userTokenRef = db.getReference("userToken");
        userTokenRef.keepSynced(true);
        if (currentUser != null) {
            if (currentUser.isAnonymous()) {
                AuthCredential credential = EmailAuthProvider.getCredential(dbUserEmail, dbUserPassword);
                currentUser.linkWithCredential(credential)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.i("Credential===>", "linkWithCredential:success");
                                    if (dbUserEmail.equals(userEmail) && dbUserPassword.equals(userPassword) && dbUserName.equals(userName)) {
                                        FirebaseUser user = task.getResult().getUser();
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(userName).build();
                                        if (user != null) {
                                            user.updateProfile(profileUpdates)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Log.i("Profile===>", "User profile updated.");
                                                                //Toast.makeText(UserActivity.this, "profile updated. ", Toast.LENGTH_SHORT).show();
                                                                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<String> task) {
                                                                        final String refreshedToken = task.getResult();

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
                                                                                            //Toast.makeText(UserActivity.this, "refreshed: " + refreshedToken, Toast.LENGTH_SHORT).show();
                                                                                            if (c.getUserToken().equals(refreshedToken)) {
                                                                                                //Toast.makeText(UserActivity.this, "update: ", Toast.LENGTH_SHORT).show();
                                                                                                UserItem newUser = new UserItem(Objects.requireNonNull(mAuth.getCurrentUser()).getUid(), refreshedToken, userEmail, userName);
                                                                                                Map<String, Object> userValues = newUser.toMap();
                                                                                                Map<String, Object> userUpdates = new HashMap<>();
                                                                                                userUpdates.put("/token/" + key, userValues);
                                                                                                userTokenRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                                                                                    @Override
                                                                                                    public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                                                        if (databaseError != null) {
                                                                                                            Toast.makeText(UserActivity.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
                                                                                Toast.makeText(UserActivity.this, "DatabaseError, userRef, token: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                                    }
                                                                });

                                                                addFbUidData();

                                                                if (dbHelper.deleteUser(index) == 0) {
                                                                    Log.i("delete User: ", "no data change!");
                                                                }
                                                                if (dbHelper.createUser(userName, userEmail, userPassword, Bitmap2Bytes(selectedUserImg)) == -1) {
                                                                    Log.i("create User: ", "fail !");
                                                                }
                                                                Toast.makeText(UserActivity.this, "原資料已由另一端刪除, " + "\n" + "重建使用者資料成功 !", Toast.LENGTH_LONG).show();
                                                                intent = getIntent();
                                                                finish();
                                                                startActivity(intent);
                                                            } else {
                                                                Log.i("Profile===>", "User profile update fail.");
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                    else {
                                        final FirebaseUser currentUser = mAuth.getCurrentUser();
                                        if (currentUser != null) {
                                            currentUser.delete()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Log.i("User Account===>", "User account: delete");
                                                                if (dbHelper.deleteUser(index) == 0) {
                                                                    Log.i("delete User: ", "no data change!");
                                                                }
                                                                //Toast.makeText(UserActivity.this, "資料已刪除 !", Toast.LENGTH_LONG).show();
                                                                addNewUserAccount();
                                                            } else {
                                                                //Toast.makeText(UserActivity.this, "登入已久, 需重新登入, 再執行刪除.", Toast.LENGTH_LONG).show();
                                                                AuthCredential credential = EmailAuthProvider.getCredential(userEmail, userPassword);

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
                                                                                                        if (dbHelper.deleteUser(index) == 0) {
                                                                                                            Log.i("delete User: ", "no data change!");
                                                                                                        }
                                                                                                        //Toast.makeText(UserActivity.this, "資料已刪除 !", Toast.LENGTH_LONG).show();
                                                                                                        addNewUserAccount();
                                                                                                    } else {
                                                                                                        Log.i("check User Account===>", "User account: delete fail");
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                } else {
                                    Log.i("Credential===>", "linkWithCredential:failure", task.getException());
                                    //Toast.makeText(UserActivity.this, "Authentication failed: need logout, firstly !", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(UserActivity.this, "只可設定一位使用者資料 !", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            }
            else {
                Toast.makeText(UserActivity.this, "只可設定一位使用者資料 !", Toast.LENGTH_SHORT).show();
            }
        } else {
            mAuth.createUserWithEmailAndPassword(dbUserEmail, dbUserPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.i("CreateUserAccount===>", "Create User Account: success");
                                if (dbUserEmail.equals(userEmail) && dbUserPassword.equals(userPassword) && dbUserName.equals(userName)) {
                                    FirebaseUser user = task.getResult().getUser();
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(userName).build();
                                    if (user != null) {
                                        user.updateProfile(profileUpdates)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.i("Profile===>", "User profile update success.");
                                                            //Toast.makeText(UserActivity.this, "profile updated. ", Toast.LENGTH_SHORT).show();
                                                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<String> task) {
                                                                    final String refreshedToken = task.getResult();

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
                                                                                        //Toast.makeText(UserActivity.this, "refreshed: " + refreshedToken, Toast.LENGTH_SHORT).show();
                                                                                        if (c.getUserToken().equals(refreshedToken)) {
                                                                                            //Toast.makeText(UserActivity.this, "update: ", Toast.LENGTH_SHORT).show();
                                                                                            UserItem newUser = new UserItem(Objects.requireNonNull(mAuth.getCurrentUser()).getUid(), refreshedToken, userEmail, userName);
                                                                                            Map<String, Object> userValues = newUser.toMap();
                                                                                            Map<String, Object> userUpdates = new HashMap<>();
                                                                                            userUpdates.put("/token/" + key, userValues);
                                                                                            userTokenRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                                                                                @Override
                                                                                                public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                                                    if (databaseError != null) {
                                                                                                        Toast.makeText(UserActivity.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
                                                                            Toast.makeText(UserActivity.this, "DatabaseError, userRef, token: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                }
                                                            });

                                                            addFbUidData();

                                                            if (dbHelper.deleteUser(index) == 0) {
                                                                Log.i("delete User: ", "no data change!");
                                                            }
                                                            if (dbHelper.createUser(userName, userEmail, userPassword, Bitmap2Bytes(selectedUserImg)) == -1) {
                                                                Log.i("create User: ", "fail !");
                                                            }
                                                            Toast.makeText(UserActivity.this, "原資料已由另一端刪除, " + "\n" + "重建使用者資料成功 !", Toast.LENGTH_LONG).show();
                                                            intent = getIntent();
                                                            finish();
                                                            startActivity(intent);
                                                        } else {
                                                            Log.i("Profile===>", "User profile update fail.");
                                                        }
                                                    }
                                                });
                                    }
                                }
                                else {
                                    final FirebaseUser currentUser = mAuth.getCurrentUser();
                                    if (currentUser != null) {
                                        currentUser.delete()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.i("User Account===>", "User account: delete");
                                                            if (dbHelper.deleteUser(index) == 0) {
                                                                Log.i("delete User: ", "no data change!");
                                                            }
                                                            //Toast.makeText(UserActivity.this, "資料已刪除 !", Toast.LENGTH_LONG).show();
                                                            addNewUserAccount();
                                                        } else {
                                                            //Toast.makeText(UserActivity.this, "登入已久, 需重新登入, 再執行刪除.", Toast.LENGTH_LONG).show();
                                                            AuthCredential credential = EmailAuthProvider.getCredential(userEmail, userPassword);

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
                                                                                                    if (dbHelper.deleteUser(index) == 0) {
                                                                                                        Log.i("delete User: ", "no data change!");
                                                                                                    }
                                                                                                    //Toast.makeText(UserActivity.this, "資料已刪除 !", Toast.LENGTH_LONG).show();
                                                                                                    addNewUserAccount();
                                                                                                } else {
                                                                                                    Log.i("check User Account===>", "User account: delete fail");
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                    }
                                }
                            } else {
                                Log.i("CreateUserAccount===>", "Create User Account: failure", task.getException());
                                //Toast.makeText(UserActivity.this, "Create User Account: failure !", Toast.LENGTH_SHORT).show();
                                Toast.makeText(UserActivity.this, "只可設定一位使用者資料 !", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    void cropImgSave (String filePath, Uri fileUri) {
        selectedUserImg = BitmapFactory.decodeFile(filePath);
        selectedUserImg = getRoundedCroppedBitmap(selectedUserImg);
        userPicture.setImageBitmap(selectedUserImg);

        byte[] img = Bitmap2Bytes(selectedUserImg);
        if (img.length > MaxByte) {
            try {
                ParcelFileDescriptor parcelFileDescriptor =
                        getContentResolver().openFileDescriptor(fileUri, "r");

                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap image;
                        try {
                            int targetW = 200;
                            int targetH = 200;

                            // Get the dimensions of the bitmap
                            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                            bmOptions.inJustDecodeBounds = true;

                            if (parcelFileDescriptor != null) {
                                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                                BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmOptions);

                                int photoW = bmOptions.outWidth;
                                int photoH = bmOptions.outHeight;

                                // Determine how much to scale down the image
                                int scaleFactor = 1;

                                if (photoH > targetH || photoW > targetW) {
                                    final int halfHeight = photoH / 2;
                                    final int halfWidth = photoW / 2;

                                    // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                                    // height and width larger than the requested height and width.
                                    while ((halfHeight / scaleFactor) >= targetH
                                            && (halfWidth / scaleFactor) >= targetW) {
                                        scaleFactor *= 2;
                                    }
                                }

                                // Decode the image file into a Bitmap sized to fill the View
                                bmOptions.inJustDecodeBounds = false;
                                bmOptions.inSampleSize = scaleFactor;
                                image = BitmapFactory.decodeFileDescriptor(fileDescriptor,null, bmOptions);

                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                image.compress(Bitmap.CompressFormat.JPEG, 100 , bos);  //ignored for PNG
                                byte[] bitmapdata = bos.toByteArray();
                                FileOutputStream fos = new FileOutputStream(file);
                                fos.write(bitmapdata);
                                fos.flush();
                                fos.close();
                                parcelFileDescriptor.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(UserActivity.this, "decodeFile error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        handler.post(() -> ResizeCropImgSave(file));
                    }
                });

                Log.i("Resize ==>", "file resize !");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(UserActivity.this, "decodeFile error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        else {
            //Toast.makeText(UserActivity.this, "Image_crop size: " + img.length, Toast.LENGTH_LONG).show();
            if (dbUserName == null || dbUserEmail == null || dbUserPassword == null) {
                Toast.makeText(UserActivity.this, "檔案已載入, 請新增使用者資料", Toast.LENGTH_SHORT).show();
            }
            else {
                if (dbHelper.updateUser(index, dbUserName, dbUserEmail, dbUserPassword, img) == 0) {
                    Toast.makeText(UserActivity.this, "db update error", Toast.LENGTH_SHORT).show();
                    Log.i("update User: ", "no data change!");
                }
                else {
                    MainActivity.userImg = selectedUserImg;
                    if (file.exists()) {
                        if (file.delete()) {
                            Log.i("===> Deleted File Path:", file.getPath());
                        }
                    }
                }
                //Toast.makeText(UserActivity.this, "image size: " + img.length/1024 + "k", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void ResizeCropImgSave (File cropFile) {
        selectedUserImg = BitmapFactory.decodeFile(cropFile.getPath());
        selectedUserImg = getRoundedCroppedBitmap(selectedUserImg);
        userPicture.setImageBitmap(selectedUserImg);
        byte[] img = Bitmap2Bytes(selectedUserImg);

        if (dbUserName == null || dbUserEmail == null || dbUserPassword == null) {
            Toast.makeText(UserActivity.this, "檔案已載入, 請新增使用者資料", Toast.LENGTH_SHORT).show();
        }
        else {
            if (dbHelper.updateUser(index, dbUserName, dbUserEmail, dbUserPassword, img) == 0) {
                Toast.makeText(UserActivity.this, "db update error", Toast.LENGTH_SHORT).show();
                Log.i("update User: ", "no data change!");
            }
            else {
                MainActivity.userImg = selectedUserImg;
                if (cropFile.exists()) {
                    if (cropFile.delete()) {
                        Log.i("===> Deleted File Path:", cropFile.getPath());
                    }
                }
            }
            //Toast.makeText(UserActivity.this, "image size: " + img.length/1024 + "k", Toast.LENGTH_SHORT).show();
        }

    }

    private Bitmap getRoundedCroppedBitmap(Bitmap bitmap) {
        int widthLight = bitmap.getWidth();
        int heightLight = bitmap.getHeight();

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        Paint paintColor = new Paint();
        paintColor.setFlags(Paint.ANTI_ALIAS_FLAG);

        RectF rectF = new RectF(new Rect(0, 0, widthLight, heightLight));

        canvas.drawRoundRect(rectF, (float)(widthLight/2) ,(float)(heightLight/2),paintColor);

        Paint paintImage = new Paint();
        paintImage.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(bitmap, 0, 0, paintImage);

        return output;
    }

    @Override
    public void onClick(View view) {
        db = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userRef = db.getReference("user");
        userRef.keepSynced(true);
        userTokenRef = db.getReference("userToken");
        userTokenRef.keepSynced(true);
        userName = editName.getText().toString();
        userPassword = editPassword.getText().toString();
        userEmail = editEmail.getText().toString();
        final FirebaseUser currentUser = mAuth.getCurrentUser();

        if (InternetConnection.checkConnection(UserActivity.this)) {
            switch (view.getId()) {
                case R.id.userModeBtn_id:
                    if (existUser) {
                        if (mAuth.getCurrentUser() != null) {
                            key = mAuth.getCurrentUser().getUid();
                        }
                        if (currentUser != null && !currentUser.isAnonymous()) {
                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    userToken = task.getResult();

                                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            DataSnapshot uidSnapshot = dataSnapshot.child("Uid");
                                            Iterable<DataSnapshot> uidChildren = uidSnapshot.getChildren();
                                            for (DataSnapshot uid : uidChildren) {
                                                String fbUid = uid.getKey();
                                                Log.i("Firebase ==>", "Firebase Uid is: " + fbUid);
                                                if (fbUid != null) {
                                                    if (fbUid.equals(key)) {
                                                        final boolean userAccountEffective = false;
                                                        Date loginDate = new Date();
                                                        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.TAIWAN);
                                                        df.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                                                        UserInfo newUser = new UserInfo(df.format(loginDate), userToken, dbUserEmail, dbUserName, dbUserPassword, userAccountEffective, deviceInfo);
                                                        Map<String, Object> userValues = newUser.toMap();
                                                        Map<String, Object> userUpdates = new HashMap<>();
                                                        userUpdates.put("/Uid/" + key + "/userInfo/", userValues);
                                                        userRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                                            @Override
                                                            public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                if (databaseError != null) {
                                                                    Toast.makeText(UserActivity.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            // Failed to read value
                                            Log.i("Firebase ==>", "Failed to read user data.", error.toException());
                                            Toast.makeText(UserActivity.this, "DatabaseError, userRef, Uid: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    currentUser.delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.i("User Account===>", "User account: delete");
                                                        if (dbHelper.deleteUser(index) == 0) {
                                                            Log.i("delete User: ", "no data change!");
                                                        }
                                                        Toast.makeText(UserActivity.this, "資料已刪除 !", Toast.LENGTH_SHORT).show();
                                                        intent = new Intent();
                                                        intent.setClass(UserActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                        UserActivity.this.finish();
                                                    } else {
                                                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                DataSnapshot uidSnapshot = dataSnapshot.child("Uid");
                                                                Iterable<DataSnapshot> uidChildren = uidSnapshot.getChildren();
                                                                for (DataSnapshot uid : uidChildren) {
                                                                    String fbUid = uid.getKey();
                                                                    Log.i("Firebase ==>", "Firebase Uid is: " + fbUid);
                                                                    if (fbUid != null) {
                                                                        if (fbUid.equals(key)) {
                                                                            userAccountExist = true;
                                                                            Date loginDate = new Date();
                                                                            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.TAIWAN);
                                                                            df.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                                                                            UserInfo newUser = new UserInfo(df.format(loginDate), userToken, dbUserEmail, dbUserName, dbUserPassword, userAccountExist, deviceInfo);
                                                                            Map<String, Object> userValues = newUser.toMap();
                                                                            Map<String, Object> userUpdates = new HashMap<>();
                                                                            userUpdates.put("/Uid/" + key + "/userInfo/", userValues);
                                                                            userRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                                                                @Override
                                                                                public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                                    if (databaseError != null) {
                                                                                        Toast.makeText(UserActivity.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {
                                                                // Failed to read value
                                                                Log.i("Firebase ==>", "Failed to read user data.", error.toException());
                                                                Toast.makeText(UserActivity.this, "DatabaseError, userRef, Uid: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });

                                                        Toast.makeText(UserActivity.this, "登入已久, 需重新登入, 再執行刪除.", Toast.LENGTH_SHORT).show();
                                                        AuthCredential credential = EmailAuthProvider.getCredential(userEmail, userPassword);
                                                        FirebaseUser currentUser = mAuth.getCurrentUser();
                                                        // Prompt the user to re-provide their sign-in credentials
                                                        try {
                                                            currentUser.reauthenticate(credential)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                Log.i("Re-authenticated===>", "User re-authenticated: Success");
                                                                                Toast.makeText(UserActivity.this, "已自動重新登入成功, 可再執行刪除 !", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                        } catch (Exception e) {
                                                            Log.i("Re-authenticated===>", "User re-authenticated: " + e.getMessage());
                                                            Toast.makeText(UserActivity.this, "刪除失敗: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                }
                                            });

                                }
                            });

                        } else {
                            Toast.makeText(UserActivity.this, "請先登入, 再執行刪除 !", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        if (!userEmail.isEmpty() && !userPassword.isEmpty() && !userName.isEmpty()) {
                            if (dbHelper.IsDbUserEmpty()) {
                                addNewUserAccount();
                            }
                            else {
                                checkAndSetFbUserAccount();
                            }
                        } else {
                            Toast.makeText(UserActivity.this, "請輸入資料 !", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;

                case R.id.nameBtn_id:
                    if (currentUser != null && !currentUser.isAnonymous()) {
                        if (!userName.isEmpty()) {
                            if (!userName.equals(dbUserName)) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(userName).build();
                                currentUser.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.i("Update===>", "User name update success.");
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
                                                                                //Toast.makeText(UserActivity.this, "refreshed: " + refreshedToken, Toast.LENGTH_SHORT).show();
                                                                                if (c.getUserToken().equals(userToken)) {
                                                                                    //Toast.makeText(UserActivity.this, "update: ", Toast.LENGTH_SHORT).show();
                                                                                    UserItem newUser = new UserItem(Objects.requireNonNull(mAuth.getCurrentUser()).getUid(), userToken, dbUserEmail, userName);
                                                                                    Map<String, Object> userValues = newUser.toMap();
                                                                                    Map<String, Object> userUpdates = new HashMap<>();
                                                                                    userUpdates.put("/token/" + key, userValues);
                                                                                    userTokenRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                                                                        @Override
                                                                                        public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                                            if (databaseError != null) {
                                                                                                Toast.makeText(UserActivity.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
                                                                    Toast.makeText(UserActivity.this, "DatabaseError, userRef, token: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });

                                                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    String key = "uid";
                                                                    if (mAuth.getCurrentUser() != null) {
                                                                        key = mAuth.getCurrentUser().getUid();
                                                                    }
                                                                    DataSnapshot uidSnapshot = dataSnapshot.child("Uid");
                                                                    Iterable<DataSnapshot> uidChildren = uidSnapshot.getChildren();
                                                                    for (DataSnapshot uid : uidChildren) {
                                                                        String fbUid = uid.getKey();
                                                                        Log.i("Firebase ==>", "Firebase Uid is: " + fbUid);
                                                                        if (fbUid != null) {
                                                                            if (fbUid.equals(key)) {
                                                                                Date loginDate = new Date();
                                                                                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.TAIWAN);
                                                                                df.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                                                                                UserInfo newUser = new UserInfo(df.format(loginDate), userToken, dbUserEmail, userName, dbUserPassword, userAccountExist, deviceInfo);
                                                                                Map<String, Object> userValues = newUser.toMap();
                                                                                Map<String, Object> userUpdates = new HashMap<>();
                                                                                userUpdates.put("/Uid/" + key + "/userInfo/", userValues);
                                                                                userRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                                                                    @Override
                                                                                    public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                                        if (databaseError != null) {
                                                                                            Toast.makeText(UserActivity.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {
                                                                    // Failed to read value
                                                                    Log.i("Firebase ==>", "Failed to read user data.", error.toException());
                                                                    Toast.makeText(UserActivity.this, "DatabaseError, userRef, Uid: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                    });

                                                    if (dbHelper.updateUser(index, userName, dbUserEmail, dbUserPassword, Bitmap2Bytes(selectedUserImg)) == 0) {
                                                        Log.i("update User: ", "no data change!");
                                                    }
                                                    dbUserName = userName;
                                                    Toast.makeText(UserActivity.this, "姓名已更新", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Log.i("Update===>", "User name update fail.");
                                                    Toast.makeText(UserActivity.this, "無法更新", Toast.LENGTH_SHORT).show();
                                                    editName.setText(dbUserName);
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(UserActivity.this, "請輸入資料 !", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(UserActivity.this, "請先登入, 才可以更改 !", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.emailBtn_id:
                    if (currentUser != null && !currentUser.isAnonymous()) {
                        if (!userEmail.isEmpty()) {
                            if (!userEmail.equals(dbUserEmail)) {
                                currentUser.updateEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.i("Update===>", "User email address update success.");
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
                                                                        //Toast.makeText(UserActivity.this, "refreshed: " + refreshedToken, Toast.LENGTH_SHORT).show();
                                                                        if (c.getUserToken().equals(userToken)) {
                                                                            //Toast.makeText(UserActivity.this, "update: ", Toast.LENGTH_SHORT).show();
                                                                            UserItem newUser = new UserItem(Objects.requireNonNull(mAuth.getCurrentUser()).getUid(), userToken, userEmail, dbUserName);
                                                                            Map<String, Object> userValues = newUser.toMap();
                                                                            Map<String, Object> userUpdates = new HashMap<>();
                                                                            userUpdates.put("/token/" + key, userValues);
                                                                            userTokenRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                                                                @Override
                                                                                public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                                    if (databaseError != null) {
                                                                                        Toast.makeText(UserActivity.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
                                                            Toast.makeText(UserActivity.this, "DatabaseError, userRef, token: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            String key = "uid";
                                                            if (mAuth.getCurrentUser() != null) {
                                                                key = mAuth.getCurrentUser().getUid();
                                                            }
                                                            DataSnapshot uidSnapshot = dataSnapshot.child("Uid");
                                                            Iterable<DataSnapshot> uidChildren = uidSnapshot.getChildren();
                                                            for (DataSnapshot uid : uidChildren) {
                                                                String fbUid = uid.getKey();
                                                                Log.i("Firebase ==>", "Firebase Uid is: " + fbUid);
                                                                if (fbUid != null) {
                                                                    if (fbUid.equals(key)) {
                                                                        Date loginDate = new Date();
                                                                        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.TAIWAN);
                                                                        df.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                                                                        UserInfo newUser = new UserInfo(df.format(loginDate), userToken, userEmail, dbUserName, dbUserPassword, userAccountExist, deviceInfo);
                                                                        Map<String, Object> userValues = newUser.toMap();
                                                                        Map<String, Object> userUpdates = new HashMap<>();
                                                                        userUpdates.put("/Uid/" + key + "/userInfo/", userValues);
                                                                        userRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                                                            @Override
                                                                            public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                                if (databaseError != null) {
                                                                                    Toast.makeText(UserActivity.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            // Failed to read value
                                                            Log.i("Firebase ==>", "Failed to read user data.", error.toException());
                                                            Toast.makeText(UserActivity.this, "DatabaseError, userRef, Uid: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                                }
                                            });

                                            if (dbHelper.updateUser(index, dbUserName, userEmail, dbUserPassword, Bitmap2Bytes(selectedUserImg)) == 0) {
                                                Log.i("update User: ", "no data change!");
                                            }
                                            dbUserEmail = userEmail;
                                            Toast.makeText(UserActivity.this, "Email 已更新", Toast.LENGTH_SHORT).show();
                                            FirebaseUser currentUser = mAuth.getCurrentUser();
                                            if (currentUser != null) {
                                                currentUser.sendEmailVerification()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d("Verification mail===>", "Email updated, mail sent success.");
                                                                    Toast.makeText(UserActivity.this, "通知信已寄送.", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Log.d("Verification mail===>", "Email updated, mail sent fail.");
                                                                }
                                                            }
                                                        });
                                            }
                                        } else {
                                            if (!relogin) {
                                                Log.i("Update===>", "User Email update fail.");
                                                Toast.makeText(UserActivity.this, "登入已久, 需重新登入, 再執行更改.", Toast.LENGTH_SHORT).show();
                                                AuthCredential credential = EmailAuthProvider
                                                        .getCredential(dbUserEmail, dbUserPassword);
                                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                                // Prompt the user to re-provide their sign-in credentials
                                                if (currentUser != null) {
                                                    currentUser.reauthenticate(credential)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Log.i("Re-authenticated===>", "User re-authenticated: Success");
                                                                        relogin = true;
                                                                        Toast.makeText(UserActivity.this, "已自動重新登入成功, 可再執行更改 !", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                            else {
                                                try {
                                                    if (task.getException() != null) {
                                                        String errMessage = task.getException().getMessage();
                                                        if (errMessage != null) {
                                                            Toast.makeText(UserActivity.this, "更改失敗: " + errMessage, Toast.LENGTH_LONG).show();
                                                        } else {
                                                            Toast.makeText(UserActivity.this, "無法更改 !", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    Toast.makeText(UserActivity.this, "無法更改: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                        }
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(UserActivity.this, "請輸入資料 !", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(UserActivity.this, "請先登入, 才可以更改 !", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.passwordBtn_id:
                    if (currentUser != null && !currentUser.isAnonymous()) {
                        if (!userPassword.isEmpty()) {
                            if (!userPassword.equals(dbUserPassword)) {
                                AuthCredential credential = EmailAuthProvider
                                        .getCredential(dbUserEmail, dbUserPassword);
                                // Prompt the user to re-provide their sign-in credentials
                                currentUser.reauthenticate(credential)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.i("Re-authenticated===>", "User re-authenticated: Success");
                                                    //Toast.makeText(UserActivity.this, "已自動重新登入成功, 可再執行更改 !", Toast.LENGTH_SHORT).show();
                                                    FirebaseUser currentUser = mAuth.getCurrentUser();
                                                    if (currentUser != null) {
                                                        currentUser.updatePassword(userPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.i("Update===>", "User password update success.");
                                                                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<String> task) {
                                                                            userToken = task.getResult();

                                                                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                    String key = "uid";
                                                                                    if (mAuth.getCurrentUser() != null) {
                                                                                        key = mAuth.getCurrentUser().getUid();
                                                                                    }
                                                                                    DataSnapshot uidSnapshot = dataSnapshot.child("Uid");
                                                                                    Iterable<DataSnapshot> uidChildren = uidSnapshot.getChildren();
                                                                                    for (DataSnapshot uid : uidChildren) {
                                                                                        String fbUid = uid.getKey();
                                                                                        Log.i("Firebase ==>", "Firebase Uid is: " + fbUid);
                                                                                        if (fbUid != null) {
                                                                                            if (fbUid.equals(key)) {
                                                                                                Date loginDate = new Date();
                                                                                                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.TAIWAN);
                                                                                                df.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                                                                                                UserInfo newUser = new UserInfo(df.format(loginDate), userToken, dbUserEmail, dbUserName, userPassword, userAccountExist, deviceInfo);
                                                                                                Map<String, Object> userValues = newUser.toMap();
                                                                                                Map<String, Object> userUpdates = new HashMap<>();
                                                                                                userUpdates.put("/Uid/" + key + "/userInfo/", userValues);
                                                                                                userRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                                                                                    @Override
                                                                                                    public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                                                        if (databaseError != null) {
                                                                                                            Toast.makeText(UserActivity.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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

                                                                                @Override
                                                                                public void onCancelled(@NonNull DatabaseError error) {
                                                                                    // Failed to read value
                                                                                    Log.i("Firebase ==>", "Failed to read user data.", error.toException());
                                                                                    Toast.makeText(UserActivity.this, "DatabaseError, userRef, Uid: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                        }
                                                                    });

                                                                    if (dbHelper.updateUser(index, dbUserName, dbUserEmail, userPassword, Bitmap2Bytes(selectedUserImg)) == 0) {
                                                                        Log.i("update User: ", "no data change!");
                                                                    }
                                                                    dbUserPassword = userPassword;
                                                                    Toast.makeText(UserActivity.this, "密碼已更新", Toast.LENGTH_SHORT).show();
                                                                    mAuth.sendPasswordResetEmail(dbUserEmail)
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        Log.d("Verification mail===>", "Password updated, mail sent success.");
                                                                                        Toast.makeText(UserActivity.this, "通知信已寄送.", Toast.LENGTH_SHORT).show();
                                                                                    } else {
                                                                                        Log.d("Verification mail===>", "Password updated, mail sent fail.");
                                                                                    }
                                                                                }
                                                                            });
                                                                } else {
                                                                    Log.i("Update===>", "User password update fail: " + task.getException());
                                                                    try {
                                                                        if (task.getException() != null) {
                                                                            String errMessage = task.getException().getMessage();
                                                                            if (errMessage != null) {
                                                                                Toast.makeText(UserActivity.this, "更改錯誤: " + errMessage, Toast.LENGTH_LONG).show();
                                                                            } else {
                                                                                Toast.makeText(UserActivity.this, "無法更改 !", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                        Toast.makeText(UserActivity.this, "無法更改 !", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                                else {
                                                    try {
                                                        if (task.getException() != null) {
                                                            String errMessage = task.getException().getMessage();
                                                            if (errMessage != null) {
                                                                Toast.makeText(UserActivity.this, "更改失敗: " + errMessage, Toast.LENGTH_LONG).show();
                                                            } else {
                                                                Toast.makeText(UserActivity.this, "無法更改 !", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        Toast.makeText(UserActivity.this, "無法更改: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }

                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(UserActivity.this, "請輸入資料 !", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(UserActivity.this, "請先登入, 才可以更改 !", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.pictureBtn_id:
                    Intent photoPickerIntent;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        photoPickerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        photoPickerIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                                | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } else {
                        photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    }
                    photoPickerIntent.setType("image/*");
                    //photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    loadImgResultLauncher.launch(photoPickerIntent);
                    break;
                default:
                    Log.i("Submit===>", "Undefined case.");
                    break;
            }
        }
        else {
            Toast.makeText(UserActivity.this, "請先開啟網路連線 ! ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        executor.shutdown();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        intent = new Intent();
        switch (menu_item) {
            case "DISH":
                intent.setClass(UserActivity.this, MainActivity.class);
                break;
            case "CAKE":
                intent.setClass(UserActivity.this, CakeActivity.class);
                break;
            case "PHONE":
                intent.setClass(UserActivity.this, PhoneActivity.class);
                break;
            case "CAMERA":
                intent.setClass(UserActivity.this, CameraActivity.class);
                break;
            case "BOOK":
                intent.setClass(UserActivity.this, BookActivity.class);
                break;
            default:
                Toast.makeText(this.getBaseContext(), "Return to main menu ! ", Toast.LENGTH_SHORT).show();
                intent.setClass(UserActivity.this, MainActivity.class);
        }

        startActivity(intent);
        UserActivity.this.finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user, menu);
        if (InternetConnection.checkConnection(UserActivity.this)) {
            if (menu.findItem(R.id.action_login_status).isVisible()) {
                menu.findItem(R.id.action_login_status).setVisible(false);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Bundle bundle;
        int id = item.getItemId();

        switch (id) {
            case R.id.action_user_shopping_car:
                intent = new Intent();
                bundle = new Bundle();
                bundle.putString("Menu", menu_item);
                intent.putExtras(bundle);
                intent.setClass(UserActivity.this, OrderActivity.class);
                startActivity(intent);
                UserActivity.this.finish();
                break;

            case R.id.action_return:
                intent = new Intent();
                switch (menu_item) {
                    case "DISH":
                        intent.setClass(UserActivity.this, MainActivity.class);
                        break;
                    case "CAKE":
                        intent.setClass(UserActivity.this, CakeActivity.class);
                        break;
                    case "PHONE":
                        intent.setClass(UserActivity.this, PhoneActivity.class);
                        break;
                    case "CAMERA":
                        intent.setClass(UserActivity.this, CameraActivity.class);
                        break;
                    case "BOOK":
                        intent.setClass(UserActivity.this, BookActivity.class);
                        break;
                    default:
                        Toast.makeText(this.getBaseContext(), "Return to main menu ! ", Toast.LENGTH_SHORT).show();
                        intent.setClass(UserActivity.this, MainActivity.class);
                }
                startActivity(intent);
                UserActivity.this.finish();
                break;

            case R.id.action_login_status:
                Toast.makeText(UserActivity.this, "請先開啟網路連線 ! ", Toast.LENGTH_SHORT).show();
                break;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

