package com.example.user.store3c;

import android.app.ActivityManager;
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
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static com.example.user.store3c.MainActivity.setFirebaseDbPersistence;

@Keep
public class PromotionFirebaseMessagingService extends FirebaseMessagingService {
    private static Integer totalUserAmount = 0;

    private DatabaseReference userTokenRef;
    private AccountDbAdapter dbhelper = null;
    private String dbUserName, dbUserEmail;
    private volatile ActivityManager am;
    private volatile List<ActivityManager.AppTask> tasks;

    public String userId = "defaultId";
    public String refreshedToken;

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public synchronized void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i("Messaging===> ", "from: "+remoteMessage.getFrom());
        String title, messageType, messageText, subText, message, imageUrl; // "http://appserver.000webhostapp.com/store3c/image/dish/d16.jpg"
        String messagePrice = "", messageIntro = "";
        Bitmap picture = BitmapFactory.decodeResource(getResources(), R.drawable.store_icon);
        Bitmap bitmap;
        Map<String, String> data;
        PendingIntent pendingIntent, resultPendingIntent;
        Intent intent, resultIntent;
        NotificationManager notificationManager;
        String channelId;
        Uri defaultSoundUri;
        NotificationCompat.Builder notificationBuilder;
        int smallIconId;
        Notification notification;
        PowerManager pm;
        PowerManager.WakeLock wl;
        int notificationId;
        Random r = new Random();
        int pendingIntentIndex;
        TaskStackBuilder stackBuilder;

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            data = remoteMessage.getData();
            Log.i("Messaging===> ", "Message data payload:  "+ data);

            title = data.get("titleText");
            messageType = data.get("messageType");
            if (messageType != null) {
                if (messageType.equals("promotion")) {          //Message from cloud function server; orderFormActivity
                    messageText = data.get("messageText");
                    subText = data.get("subText");
                    resultIntent = new Intent(this, com.example.user.store3c.OrderFormActivity.class);

                    am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    tasks = am.getAppTasks();
                    Log.i("Notification===> ", "size:  " +  tasks.size());

                    if (tasks.size() != 0) {
                        boolean appRunningForeground = false;
                        final List<ActivityManager.RunningAppProcessInfo> procInfos = am.getRunningAppProcesses();
                        if (procInfos != null) {
                            String packageName = getApplicationContext().getPackageName();
                            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                                if (processInfo.processName.equals(packageName)) {
                                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                                        appRunningForeground =  true;
                                        break;
                                    }
                                }
                            }
                            if (appRunningForeground) {
                                resultIntent.putExtra("Notification", "IN_APP");
                                resultIntent.putExtra("RetainRecentTask", "RECENT_ACTIVITY");
                                resultIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                            } else {
                                resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                resultIntent.putExtra("Notification", "UPPER_APP");
                            }
                        }
                    }
                    else {          // user clear all app in recent screen
                        resultIntent.putExtra("Notification", "UPPER_APP");
                    }
                    resultIntent.putExtra("OrderMessageText", "    " + messageText);

                    pendingIntentIndex = r.nextInt(1000000);
                    stackBuilder = TaskStackBuilder.create(PromotionFirebaseMessagingService.this);
                    stackBuilder.addNextIntent(resultIntent);
                    resultPendingIntent = stackBuilder.getPendingIntent(pendingIntentIndex, PendingIntent.FLAG_UPDATE_CURRENT);

                    channelId = getString(R.string.default_notification_channel_id);
                    defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.store_icon);
                    notificationBuilder =
                            new NotificationCompat.Builder(PromotionFirebaseMessagingService.this, channelId)
                                    .setSmallIcon(R.drawable.store_icon)
                                    .setLargeIcon(bitmap)
                                    .setContentTitle(title)
                                    .setContentText(messageText)
                                    .setSubText(subText)
                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(messageText))
                                    .setAutoCancel(true)
                                    .setSound(defaultSoundUri)
                                    .setContentIntent(resultPendingIntent);

                    smallIconId = getApplicationContext().getResources().getIdentifier("right_icon", "id", Objects.requireNonNull(android.R.class.getPackage()).getName());
                    notification = notificationBuilder.build();
                    if (smallIconId != 0) {
                        if (notification.contentView != null)
                            notification.contentView.setViewVisibility(smallIconId, View.INVISIBLE);
                        if (notification.bigContentView != null)
                            notification.bigContentView.setViewVisibility(smallIconId, View.INVISIBLE);
                    }

                    notificationManager =
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
                        pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
                        if (pm != null) {
                            wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "Store3C:ScreenLockNotificationTag");
                            wl.acquire(30000);
                            wl.release();
                        }
                    } catch (Exception e) {
                        Log.i("Exception ==> ",  e.getClass().toString());
                    }
                    notificationId = new Random().nextInt(60000);
                    if (notificationManager != null) {
                        notificationManager.notify(notificationId /* ID of notification */, notification);
                    }

                } else  if (messageType.equals("Talend-promotion") || messageType.equals("FCM-console")) {        //broadcast message  :  Restlet talend API tester; Firebase message with Notification and Data(user defined) payload
                    message = data.get("messageText");
                    imageUrl = data.get("imagePath");

                    if (getBitmapfromUrl(imageUrl) != null) {
                        picture = getBitmapfromUrl(imageUrl);
                    }
                    messagePrice = data.get("messagePrice");
                    messageIntro = data.get("messageIntro");
                    intent = new Intent(PromotionFirebaseMessagingService.this, ProductActivity.class);

                    am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    tasks = am.getAppTasks();
                    Log.i("Notification===> ", "size:  " +  tasks.size());

                    if (tasks.size() != 0) {
                        boolean appRunningForeground = false;
                        final List<ActivityManager.RunningAppProcessInfo> procInfos = am.getRunningAppProcesses();
                        if (procInfos != null) {
                            String packageName = getApplicationContext().getPackageName();
                            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                                if (processInfo.processName.equals(packageName)) {
                                    Log.i("Notification===> ", "find app package.  ");
                                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                                        appRunningForeground =  true;
                                        break;
                                    }
                                }
                            }
                            if (appRunningForeground) {
                                intent.putExtra("Notification", "IN_APP");
                                intent.putExtra("RetainRecentTask", "RECENT_ACTIVITY");
                                intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                                Log.i("Notification===> ", "fork new task.  ");
                            } else {
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("Notification", "UPPER_APP");
                            }
                        }
                        else {
                            Log.i("Notification===> ", "Non running app.  ");
                        }
                    }
                    else {          // user clear all app in recent screen
                        intent.putExtra("Notification", "UPPER_APP");
                    }

                    if (picture == null) {
                        picture = BitmapFactory.decodeResource(getResources(), R.drawable.store_icon);
                    }
                    intent.putExtra("Pic", Bitmap2Bytes(picture));
                    intent.putExtra("Name", message);
                    intent.putExtra("Price", messagePrice);
                    intent.putExtra("Intro", messageIntro);

                    pendingIntentIndex = r.nextInt(1000000);
                    stackBuilder = TaskStackBuilder.create(PromotionFirebaseMessagingService.this);
                    stackBuilder.addNextIntent(intent);
                    pendingIntent = stackBuilder.getPendingIntent(pendingIntentIndex, PendingIntent.FLAG_UPDATE_CURRENT);

                    channelId = getString(R.string.default_notification_channel_id);
                    defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.store_icon);
                    notification =
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

                    smallIconId = getApplicationContext().getResources().getIdentifier("right_icon", "id", Objects.requireNonNull(android.R.class.getPackage()).getName());

                    if (smallIconId != 0) {
                        if (notification.contentView != null)
                            notification.contentView.setViewVisibility(smallIconId, View.INVISIBLE);
                        if (notification.bigContentView != null)
                            notification.bigContentView.setViewVisibility(smallIconId, View.INVISIBLE);
                    }

                    notificationManager =
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
                         pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
                        if (pm != null) {
                            wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "Store3C:ScreenLockNotificationTag");
                            wl.acquire(30000);
                            wl.release();
                        }
                    } catch (Exception e) {
                        Log.i("Exception ==> ",  e.getClass().toString());
                    }
                    notificationId = new Random().nextInt(60000);
                    if (notificationManager != null) {
                        notificationManager.notify(notificationId /* ID of notification */, notification);
                    }
                }
            }
        }

        // Check if message contains a notification payload.
        else if (remoteMessage.getNotification() != null) {   // firebase cloud message with no data defined by user
            Log.i("Messaging===> ", "Message Notification Body:  "+remoteMessage.getNotification().getBody());
            title = remoteMessage.getNotification().getTitle();
            message = remoteMessage.getNotification().getBody();
            messagePrice = "100元";
            messageIntro = "挑選多種蔬菜水果，吐司、熱狗、豬排、煎蛋，豐富的食材，讓人有一種滿足的感覺!";

            Uri pic = remoteMessage.getNotification().getImageUrl();
            if (pic != null) {
                picture = getBitmapfromUrl(pic.toString());
            }

            intent = new Intent(PromotionFirebaseMessagingService.this, ProductActivity.class);
            am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            tasks = am.getAppTasks();

            if (tasks.size() != 0) {
                boolean appRunningForeground = false;
                final List<ActivityManager.RunningAppProcessInfo> procInfos = am.getRunningAppProcesses();
                if (procInfos != null) {
                    String packageName = getApplicationContext().getPackageName();
                    for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                        if (processInfo.processName.equals(packageName)) {
                            Log.i("Notification===> ", "find app package.  ");
                            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                                appRunningForeground =  true;
                                break;
                            }
                        }
                    }
                    if (appRunningForeground) {
                        intent.putExtra("Notification", "IN_APP");
                        intent.putExtra("RetainRecentTask", "RECENT_ACTIVITY");
                        intent.setFlags( Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                        Log.i("Notification===> ", "fork a new task.  ");
                    } else {
                        Log.i("Notification===> ", "System tray send message to here. ");  //It should not happen, notification will send to MainActivity
                    }
                }
                else {
                    Log.i("Notification===> ", "Non running app.  ");
                }
            }
            else {          // user clear all app in recent screen
                Log.i("Notification===> ", "System tray send message to here.");  //It should not happen, notification will send to the MainActivity
            }

            if (picture == null) {
                picture = BitmapFactory.decodeResource(getResources(), R.drawable.store_icon);
            }
            intent.putExtra("Pic", Bitmap2Bytes(picture));
            intent.putExtra("Name", message);
            intent.putExtra("Price", messagePrice);
            intent.putExtra("Intro", messageIntro);

            pendingIntentIndex = r.nextInt(1000000);
            stackBuilder = TaskStackBuilder.create(PromotionFirebaseMessagingService.this);
            stackBuilder.addNextIntent(intent);
            pendingIntent = stackBuilder.getPendingIntent(pendingIntentIndex, PendingIntent.FLAG_UPDATE_CURRENT);

            channelId = getString(R.string.default_notification_channel_id);
            defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.store_icon);
            notification =
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

            smallIconId = getApplicationContext().getResources().getIdentifier("right_icon", "id", Objects.requireNonNull(android.R.class.getPackage()).getName());

            if (smallIconId != 0) {
                if (notification.contentView!=null)
                    notification.contentView.setViewVisibility(smallIconId, View.INVISIBLE);
                if (notification.bigContentView != null)
                    notification.bigContentView.setViewVisibility(smallIconId, View.INVISIBLE);
            }

            notificationManager =
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

            notificationId = new Random().nextInt(60000);
            if (notificationManager != null) {
                notificationManager.notify(notificationId /* ID of notification */, notification);
            }

            try {
                pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
                if (pm != null) {
                    wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "Store3C:ScreenLockNotificationTag");
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
    public void handleIntent(@NonNull Intent intent) {
         super.handleIntent(intent);
    }

    @Override
    synchronized public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference totalTokenRef;

        try {
            if (!setFirebaseDbPersistence) {
                db.setPersistenceEnabled(true);
                setFirebaseDbPersistence = true;
            }
        }
        catch (Exception e) {
            Log.i("Pick image timeout: " , "reload data.");
        }
        userTokenRef = db.getReference("userToken");
        totalTokenRef = userTokenRef.child("AppTokenAmount").getRef();
        userTokenRef.keepSynced(true);
        totalTokenRef.keepSynced(true);

        // Get updated Instance token.
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                refreshedToken = task.getResult();
                Log.i("InstanceId===> ", "refresh token: "+refreshedToken);

                totalTokenRef.runTransaction(new Transaction.Handler() {
                    @Override
                    public @NonNull Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        Integer counter = mutableData.getValue(Integer.class);
                        boolean findToken = false;
                        dbhelper = new AccountDbAdapter(PromotionFirebaseMessagingService.this);
                        if (counter == null) {
                            Log.i("Firebase ==>", "Total User Amount is null");
                        } else {
                            synchronized (totalUserAmount = counter) {
                                Log.i("Firebase ==>", "Total User Amount is: " + totalUserAmount);
                                if (totalUserAmount == 0) {
                                    String key = userTokenRef.child("token").push().getKey();
                                    if (dbhelper.IsDbUserEmpty()) {
                                        //userRef.child("token").child(key).setValue(new UserItem(refreshedToken, "defaultEmail", "defaultName"));

                                        UserItem newUser = new UserItem(userId, refreshedToken, "defaultEmail", "defaultName");
                                        Map<String, Object> userValues = newUser.toMap();
                                        Map<String, Object> userUpdates = new HashMap<>();
                                        userUpdates.put("/token/" + key, userValues);
                                        userTokenRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                if (databaseError != null) {
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

                                        UserItem newUser = new UserItem(userId, refreshedToken, dbUserEmail, dbUserName);
                                        Map<String, Object> userValues = newUser.toMap();
                                        Map<String, Object> userUpdates = new HashMap<>();
                                        userUpdates.put("/token/" + key, userValues);
                                        userTokenRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                if (databaseError != null) {
                                                    Log.i("updateChildren saved: ", "fail !" + databaseError.getMessage());
                                                } else {
                                                    Log.i("updateChildren saved: ", "successfully !");
                                                }
                                            }
                                        });

                                    }
                                    mutableData.setValue(++totalUserAmount);
                                } else {
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

                                            UserItem newUser = new UserItem(userId, refreshedToken, "defaultEmail", "defaultName");
                                            Map<String, Object> userValues = newUser.toMap();
                                            Map<String, Object> userUpdates = new HashMap<>();
                                            userUpdates.put("/token/" + key, userValues);
                                            userTokenRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                    if (databaseError != null) {
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

                                            UserItem newUser = new UserItem(userId, refreshedToken, dbUserEmail, dbUserName);
                                            Map<String, Object> userValues = newUser.toMap();
                                            Map<String, Object> userUpdates = new HashMap<>();
                                            userUpdates.put("/token/" + key, userValues);
                                            userTokenRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                    if (databaseError != null) {
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
                           } else {
                            Log.i("runTransaction saved: ", "successfully !");
                        }
                    }
                });
            }
        });

    }

    public Bitmap getBitmapfromUrl(String imageUrl) {
        if (InternetConnection.checkConnection(PromotionFirebaseMessagingService.this)) {
            URL url;
            HttpURLConnection connection = null;
            try {
                url = new URL(imageUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setConnectTimeout(100000);
                connection.connect();
                InputStream input = new BufferedInputStream(connection.getInputStream());
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "BitmapFromUrl: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } else {
            Log.i("網路未連線! ", " ==>PromotionFirebaseMessagingService");
            return null;
        }
    }

    private byte [] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos =  new  ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,  100 , baos);
        return  baos.toByteArray();
    }

    public PromotionFirebaseMessagingService() {
        super();
    }

}
