package com.example.user.store3c;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static com.example.user.store3c.MainActivity.setFirebaseDbPersistence;

public class PromotionFirebaseMessagingService extends FirebaseMessagingService {
    private DatabaseReference userTokenRef;
    private static int totalUserAmount = 0;
    private AccountDbAdapter dbhelper = null;
    private String dbUserName, dbUserEmail;
    public String instanceId;
    public String refreshedToken;

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.i("Messaging===> ", "from: "+remoteMessage.getFrom());
        String title, messageType, messageText, subText, message, imageUrl; // "http://appserver.000webhostapp.com/store3c/image/dish/d16.jpg"
        Bitmap picture = BitmapFactory.decodeResource(getResources(), R.drawable.store_icon);
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.i("Messaging===> ", "Message data payload:  "+remoteMessage.getData());
            Map<String, String> data = remoteMessage.getData();
            title = data.get("titleText");
            messageType = data.get("messageType");
            if (messageType != null) {
                if (messageType.equals("promotion")) {
                    messageText = data.get("messageText");
                    subText = data.get("subText");
                    Intent intent = new Intent(PromotionFirebaseMessagingService.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(PromotionFirebaseMessagingService.this, 0 /* Request code */, intent,
                            PendingIntent.FLAG_ONE_SHOT);
                    String channelId = getString(R.string.default_notification_channel_id);
                    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.store_icon);
                    NotificationCompat.Builder notificationBuilder =
                            new NotificationCompat.Builder(PromotionFirebaseMessagingService.this, channelId)
                                    .setSmallIcon(R.drawable.store_icon)
                                    .setLargeIcon(bitmap)
                                    .setContentTitle(title)
                                    .setContentText(messageText)
                                    .setSubText(subText)
                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(messageText))
                                    .setAutoCancel(true)
                                    .setSound(defaultSoundUri)
                                    .setContentIntent(pendingIntent);

                    int smallIconId = getApplicationContext().getResources().getIdentifier("right_icon", "id", Objects.requireNonNull(android.R.class.getPackage()).getName());
                    Notification notification = notificationBuilder.build();
                    if (smallIconId != 0) {
                        if (notification.contentView != null)
                            notification.contentView.setViewVisibility(smallIconId, View.INVISIBLE);
                        if (notification.bigContentView != null)
                            notification.bigContentView.setViewVisibility(smallIconId, View.INVISIBLE);
                    }

                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    // Since android Oreo notification channel is needed.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(channelId,
                                "Channel human readable title",
                                NotificationManager.IMPORTANCE_DEFAULT);
                        channel.enableLights(true);
                        if (notificationManager != null) {
                            notificationManager.createNotificationChannel(channel);
                        }
                    }

                    try {
                        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
                        if (pm != null) {
                            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "Store3C:ScreenLockNotificationTag");
                            wl.acquire(30000);
                            wl.release();
                        }
                    } catch (Exception e) {
                        Log.i("Exception ==> ",  e.getClass().toString());
                    }
                    int notificationId = new Random().nextInt(60000);
                    if (notificationManager != null) {
                        notificationManager.notify(notificationId /* ID of notification */, notification);
                    }

                } else {
                    message = data.get("messageText");
                    imageUrl = data.get("imagePath");
                    picture = getBitmapfromUrl(imageUrl);
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                            PendingIntent.FLAG_ONE_SHOT);

                    String channelId = getString(R.string.default_notification_channel_id);
                    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.store_icon);
                    Notification notification =
                            new NotificationCompat.Builder(this, channelId)
                                    .setSmallIcon(R.drawable.store_icon)
                                    .setLargeIcon(bitmap)
                                    .setContentTitle(title)
                                    .setContentText(message)
                                    .setStyle(new NotificationCompat.BigPictureStyle()
                                            .setSummaryText(message)
                                            .bigPicture(picture))
                                    .setLights(Color.WHITE, 300, 300)
                                    .setPriority(Notification.PRIORITY_DEFAULT)
                                    .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS)
                                    .setAutoCancel(true)
                                    .setSound(defaultSoundUri)
                                    .setContentIntent(pendingIntent).build();

                    int smallIconId = getApplicationContext().getResources().getIdentifier("right_icon", "id", Objects.requireNonNull(android.R.class.getPackage()).getName());

                    if (smallIconId != 0) {
                        if (notification.contentView != null)
                            notification.contentView.setViewVisibility(smallIconId, View.INVISIBLE);
                        if (notification.bigContentView != null)
                            notification.bigContentView.setViewVisibility(smallIconId, View.INVISIBLE);
                    }

                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

                    // Since android Oreo notification channel is needed.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(channelId,
                                "Channel human readable title",
                                NotificationManager.IMPORTANCE_DEFAULT);
                        channel.enableLights(true);
                        if (notificationManager != null) {
                            notificationManager.createNotificationChannel(channel);
                        }
                    } else {
                        notification.ledARGB = Color.WHITE;
                        notification.ledOnMS = 300;
                        notification.ledOffMS = 300;
                        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
                        //notification.defaults |= Notification.DEFAULT_LIGHTS;
                    }

                    try {
                        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
                        if (pm != null) {
                            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "Store3C:ScreenLockNotificationTag");
                            wl.acquire(30000);
                            wl.release();
                        }
                    } catch (Exception e) {
                        Log.i("Exception ==> ",  e.getClass().toString());
                    }
                    int notificationId = new Random().nextInt(60000);
                    if (notificationManager != null) {
                        notificationManager.notify(notificationId /* ID of notification */, notification);
                    }
                }
            }
        }

        // Check if message contains a notification payload.
        else if (remoteMessage.getNotification() != null) {
            Log.i("Messaging===> ", "Message Notification Body:  "+remoteMessage.getNotification().getBody());

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            title = remoteMessage.getNotification().getTitle();
            message = remoteMessage.getNotification().getBody();

            String channelId = getString(R.string.default_notification_channel_id);
            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.store_icon);
            Notification notification =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.store_icon)
                            .setLargeIcon(bitmap)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setStyle(new NotificationCompat.BigPictureStyle()
                                    .setSummaryText(message)
                                    .bigPicture(picture))
                            .setLights(Color.WHITE,300,300)
                            .setPriority(Notification.PRIORITY_DEFAULT)
                            .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent).build();

            int smallIconId = getApplicationContext().getResources().getIdentifier("right_icon", "id", Objects.requireNonNull(android.R.class.getPackage()).getName());

            if (smallIconId != 0) {
                if (notification.contentView!=null)
                    notification.contentView.setViewVisibility(smallIconId, View.INVISIBLE);
                if (notification.bigContentView != null)
                    notification.bigContentView.setViewVisibility(smallIconId, View.INVISIBLE);
            }

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.enableLights(true);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }
            else {
                notification.ledARGB = Color.WHITE;
                notification.ledOnMS = 300;
                notification.ledOffMS = 300;
                notification.flags |= Notification.FLAG_SHOW_LIGHTS;
                //notification.defaults |= Notification.DEFAULT_LIGHTS;
            }

            int notificationId = new Random().nextInt(60000);
            if (notificationManager != null) {
                notificationManager.notify(notificationId /* ID of notification */, notification);
            }

            try {
                PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
                if (pm != null) {
                    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "Store3C:ScreenLockNotificationTag");
                    wl.acquire(30000);
                    wl.release();
                }
            } catch (Exception e) {
                Log.i("Exception ==> ",  e.getClass().toString());
            }

            //WindowManager.LayoutParams params = ((Activity) getBaseContext()).getWindow().getAttributes();
            //params.screenBrightness = 1;
            //((Activity) getBaseContext()).getWindow().setAttributes(params);

        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        /*if (InternetConnection.checkConnection(PromotionFirebaseInstanceIdService.this)) {
            mAuth = FirebaseAuth.getInstance();
        }
        if (mAuth.getCurrentUser() == null) {
            mAuth.signInAnonymously();
        }*/
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference totalTokenRef;


        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        try {
            if (!setFirebaseDbPersistence) {
                db.setPersistenceEnabled(true);
                setFirebaseDbPersistence = true;
            }
        }
        catch (Exception e) {
            Toast.makeText(PromotionFirebaseMessagingService.this, "Pick image timeout, reload data.", Toast.LENGTH_SHORT).show();
            Log.i("Pick image timeout: " , "reload data.");
        }
        userTokenRef = db.getReference("userToken");
        totalTokenRef = userTokenRef.child("AppTokenAmount").getRef();
        userTokenRef.keepSynced(true);
        totalTokenRef.keepSynced(true);

        // Get updated InstanceID token.
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                refreshedToken = instanceIdResult.getToken();
                instanceId = FirebaseInstanceId.getInstance().getId();

                Log.i("InstanceId===> ", "refresh token: "+refreshedToken);
                Log.i("InstanceId===> ", "instance Id: "+instanceId);

                totalTokenRef.runTransaction(new Transaction.Handler() {
                    @Override
                    public @NonNull Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        Integer counter = mutableData.getValue(Integer.class);
                        boolean findToken = false;

                        dbhelper = new AccountDbAdapter(PromotionFirebaseMessagingService.this);
                        if (counter == null) {
                            Log.i("Firebase ==>", "Total User Amount is: null");
                        }
                        else {
                            totalUserAmount = counter;
                            Log.i("Firebase ==>", "Total User Amount is: " + totalUserAmount);
                            if (totalUserAmount == 0) {
                                String key = userTokenRef.child("token").push().getKey();
                                if (dbhelper.IsDbUserEmpty()) {
                                    //userRef.child("token").child(key).setValue(new UserItem(refreshedToken, "defaultEmail", "defaultName"));

                                    UserItem newUser = new UserItem(refreshedToken, "defaultEmail", "defaultName");
                                    Map<String, Object> userValues = newUser.toMap();
                                    Map<String, Object> userUpdates = new HashMap<>();
                                    userUpdates.put("/token/" + key, userValues);
                                    userTokenRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            if (databaseError != null) {
                                                Toast.makeText(PromotionFirebaseMessagingService.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                Log.i("updateChildren saved: ", "fail !" + databaseError.getMessage());
                                            } else {
                                                Log.i("updateChildren saved: ", "successfully !");
                                            }
                                        }
                                    });

                                }
                                else {
                                    try {
                                        Cursor cursor = dbhelper.getSimpleUserData();
                                        dbUserName = cursor.getString(1);
                                        dbUserEmail = cursor.getString(2);
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                    //userRef.child("token").child(key).setValue(new UserItem(refreshedToken, dbUserEmail, dbUserName));

                                    UserItem newUser = new UserItem(refreshedToken, dbUserEmail, dbUserName);
                                    Map<String, Object> userValues = newUser.toMap();
                                    Map<String, Object> userUpdates = new HashMap<>();
                                    userUpdates.put("/token/" + key, userValues);
                                    userTokenRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            if (databaseError != null) {
                                                Toast.makeText(PromotionFirebaseMessagingService.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                Log.i("updateChildren saved: ", "fail !" + databaseError.getMessage());
                                            } else {
                                                Log.i("updateChildren saved: ", "successfully !");
                                            }
                                        }
                                    });

                                }
                                mutableData.setValue(++totalUserAmount);
                            }
                            else {
                                MutableData tokenSnapshot = mutableData.child("token");
                                Iterable<MutableData> tokenChildren = tokenSnapshot.getChildren();
                                for (MutableData token : tokenChildren) {
                                    UserItem c = token.getValue(UserItem.class);
                                    if (c != null) {
                                        Log.d("user: ", c.getUserToken() + "  " + c.getUserEmail() + "  " + c.getUserName());
                                        if (c.getUserToken().equals(refreshedToken)) {
                                            findToken = true;
                                        }
                                    }
                                }
                                if (!findToken) {
                                    totalUserAmount++;
                                    String key = userTokenRef.child("token").push().getKey();
                                    if (dbhelper.IsDbUserEmpty()) {
                                        //userRef.child("token").child(key).setValue(new UserItem(refreshedToken, "defaultEmail", "defaultName"));

                                        UserItem newUser = new UserItem(refreshedToken, "defaultEmail", "defaultName");
                                        Map<String, Object> userValues = newUser.toMap();
                                        Map<String, Object> userUpdates = new HashMap<>();
                                        userUpdates.put("/token/" + key, userValues);
                                        userTokenRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                if (databaseError != null) {
                                                    Toast.makeText(PromotionFirebaseMessagingService.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                    Log.i("updateChildren saved: ", "fail !" + databaseError.getMessage());
                                                } else {
                                                    Log.i("updateChildren saved: ", "successfully !");
                                                }
                                            }
                                        });

                                    } else {
                                        try {
                                            Cursor cursor = dbhelper.getSimpleUserData();
                                            dbUserName = cursor.getString(1);
                                            dbUserEmail = cursor.getString(2);
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                        //userRef.child("token").child(key).setValue(new UserItem(refreshedToken, dbUserEmail, dbUserName));

                                        UserItem newUser = new UserItem(refreshedToken, dbUserEmail, dbUserName);
                                        Map<String, Object> userValues = newUser.toMap();
                                        Map<String, Object> userUpdates = new HashMap<>();
                                        userUpdates.put("/token/" + key, userValues);
                                        userTokenRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                if (databaseError != null) {
                                                    Toast.makeText(PromotionFirebaseMessagingService.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                    Log.i("updateChildren saved: ", "fail !" + databaseError.getMessage());
                                                } else {
                                                    Log.i("updateChildren saved: ", "successfully !");
                                                }
                                            }
                                        });

                                    }
                                    mutableData.setValue(totalUserAmount);
                                }
                            }
                        }
                        dbhelper.close();
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b,
                                           DataSnapshot dataSnapshot) {
                        // Transaction completed
                        Log.i("runTransaction===>", "postTransaction:onComplete: " + databaseError);
                        if (databaseError != null) {
                            Log.i("runTransaction saved: ", "fail !" + databaseError.getMessage());
                            Toast.makeText(PromotionFirebaseMessagingService.this, "DatabaseError: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.i("runTransaction saved: ", "successfully !");
                            //Toast.makeText(PromotionActivity.this, "Version: " + Build.VERSION.SDK_INT, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });


    }

    public Bitmap getBitmapfromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true); connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
