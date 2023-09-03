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
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;

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
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        Bitmap picture;
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

                    channelId = getString(R.string.orderForm_notification_channel_id);
                    defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.store_icon);
                    notificationBuilder =
                            new NotificationCompat.Builder(PromotionFirebaseMessagingService.this, channelId)
                                    .setSmallIcon(R.drawable.store_icon)
                                    .setLargeIcon(bitmap)
                                    .setContentTitle(title)
                                    .setContentText(messageText)
                                    .setSubText(subText)
                                    .setPriority(Notification.PRIORITY_MAX)
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

                    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    // Since android Oreo notification channel is needed.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(channelId,
                                "Store3C OrderForm Channel",
                                NotificationManager.IMPORTANCE_HIGH);
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

                } else  if (messageType.equals("Talend-promotion") || messageType.equals("FCM-console")) {        //broadcast message  :  Restlet talend API tester; Firebase message with Notification and Data(user defined) payload
                    message = data.get("messageText");
                    imageUrl = data.get("imagePath");

                    if (imageUrl == null) {
                        picture = BitmapFactory.decodeResource(getResources(), R.drawable.store_icon);
                        messagePrice = data.get("messagePrice");
                        messageIntro = data.get("messageIntro");
                        intent = new Intent(PromotionFirebaseMessagingService.this, ProductActivity.class);

                        am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                        tasks = am.getAppTasks();
                        Log.i("Notification===> ", "size:  " + tasks.size());

                        if (tasks.size() != 0) {
                            boolean appRunningForeground = false;
                            final List<ActivityManager.RunningAppProcessInfo> procInfos = am.getRunningAppProcesses();
                            if (procInfos != null) {
                                String packageName = getApplicationContext().getPackageName();
                                for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                                    if (processInfo.processName.equals(packageName)) {
                                        Log.i("Notification===> ", "find app package.  ");
                                        if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                                            appRunningForeground = true;
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
                            } else {
                                Log.i("Notification===> ", "Non running app.  ");
                            }
                        } else {          // user clear all app in recent screen
                            intent.putExtra("Notification", "UPPER_APP");
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
                                        .setPriority(Notification.PRIORITY_MAX)
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

                        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

                        // Since android Oreo notification channel is needed.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotificationChannel channel = new NotificationChannel(channelId,
                                    "Store3C onSale Channel",
                                    NotificationManager.IMPORTANCE_HIGH);
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
                            pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                            if (pm != null) {
                                wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "Store3C:ScreenLockNotificationTag");
                                wl.acquire(30000);
                                wl.release();
                            }
                        } catch (Exception e) {
                            Log.i("Exception ==> ", e.getClass().toString());
                        }
                        notificationId = new Random().nextInt(60000);
                        if (notificationManager != null) {
                            notificationManager.notify(notificationId /* ID of notification */, notification);
                        }
                    }
                    else {
                        executor.execute(() -> {
                            final Bitmap[] pictureFromExecutor = {getBitmapfromUrl(imageUrl)};

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    String messagePrice = "", messageIntro = "";
                                    Bitmap bitmap;
                                    Map<String, String> data;
                                    PendingIntent pendingIntent;
                                    Intent intent;
                                    NotificationManager notificationManager;
                                    String channelId;
                                    Uri defaultSoundUri;
                                    Notification notification;
                                    int smallIconId;
                                    PowerManager pm;
                                    PowerManager.WakeLock wl;
                                    int notificationId;
                                    Random r = new Random();
                                    int pendingIntentIndex;
                                    TaskStackBuilder stackBuilder;

                                    if (pictureFromExecutor[0] == null) {
                                        pictureFromExecutor[0] = BitmapFactory.decodeResource(getResources(), R.drawable.store_icon);
                                    }
                                    data = remoteMessage.getData();
                                    messagePrice = data.get("messagePrice");
                                    messageIntro = data.get("messageIntro");
                                    intent = new Intent(PromotionFirebaseMessagingService.this, ProductActivity.class);

                                    am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                                    tasks = am.getAppTasks();
                                    Log.i("Notification===> ", "size:  " + tasks.size());

                                    if (tasks.size() != 0) {
                                        boolean appRunningForeground = false;
                                        final List<ActivityManager.RunningAppProcessInfo> procInfos = am.getRunningAppProcesses();
                                        if (procInfos != null) {
                                            String packageName = getApplicationContext().getPackageName();
                                            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                                                if (processInfo.processName.equals(packageName)) {
                                                    Log.i("Notification===> ", "find app package.  ");
                                                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                                                        appRunningForeground = true;
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
                                        } else {
                                            Log.i("Notification===> ", "Non running app.  ");
                                        }
                                    } else {          // user clear all app in recent screen
                                        intent.putExtra("Notification", "UPPER_APP");
                                    }

                                    intent.putExtra("Pic", Bitmap2Bytes(pictureFromExecutor[0]));
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
                                            new NotificationCompat.Builder(PromotionFirebaseMessagingService.this, channelId)
                                                    .setSmallIcon(R.drawable.store_icon)
                                                    .setLargeIcon(bitmap)
                                                    .setContentTitle(title)
                                                    .setContentText(message)
                                                    .setStyle(new NotificationCompat.BigPictureStyle()
                                                            .setSummaryText(message)
                                                            .bigPicture(pictureFromExecutor[0]))
                                                    .setLights(Color.WHITE, 300, 300)
                                                    .setPriority(Notification.PRIORITY_MAX)
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

                                    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                    //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

                                    // Since android Oreo notification channel is needed.
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        NotificationChannel channel = new NotificationChannel(channelId,
                                                "Store3C onSale Channel",
                                                NotificationManager.IMPORTANCE_HIGH);
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
                                        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                                        if (pm != null) {
                                            wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "Store3C:ScreenLockNotificationTag");
                                            wl.acquire(30000);
                                            wl.release();
                                        }
                                    } catch (Exception e) {
                                        Log.i("Exception ==> ", e.getClass().toString());
                                    }
                                    notificationId = new Random().nextInt(60000);
                                    if (notificationManager != null) {
                                        notificationManager.notify(notificationId /* ID of notification */, notification);
                                    }
                                }
                            });
                        });
                    }
                }
            }
        }

        // Check if message contains a notification payload.
        else if (remoteMessage.getNotification() != null) {   // firebase cloud message with no data defined by user
            Log.i("Messaging===> ", "Message Notification Body:  " + remoteMessage.getNotification().getBody());
            title = remoteMessage.getNotification().getTitle();
            message = remoteMessage.getNotification().getBody();
            messagePrice = "100元";
            messageIntro = "挑選多種蔬菜水果，吐司、熱狗、豬排、煎蛋，豐富的食材，讓人有一種滿足的感覺!";
            Uri pic = remoteMessage.getNotification().getImageUrl();

            if (pic == null) {
                picture = BitmapFactory.decodeResource(getResources(), R.drawable.store_icon);
                intent = new Intent(PromotionFirebaseMessagingService.this, ProductActivity.class);
                am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
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
                                    appRunningForeground = true;
                                    break;
                                }
                            }
                        }
                        if (appRunningForeground) {
                            intent.putExtra("Notification", "IN_APP");
                            intent.putExtra("RetainRecentTask", "RECENT_ACTIVITY");
                            intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                            Log.i("Notification===> ", "fork a new task.  ");
                        } else {
                            Log.i("Notification===> ", "System tray send message to here. ");  //It should not happen, notification will send to MainActivity
                        }
                    } else {
                        Log.i("Notification===> ", "Non running app.  ");
                    }
                } else {          // user clear all app in recent screen
                    Log.i("Notification===> ", "System tray send message to here.");  //It should not happen, notification will send to the MainActivity
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
                                .setPriority(Notification.PRIORITY_MAX)
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
                            "Store3C onSale Channel",
                            NotificationManager.IMPORTANCE_HIGH);
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

                notificationId = new Random().nextInt(60000);
                if (notificationManager != null) {
                    notificationManager.notify(notificationId /* ID of notification */, notification);
                }

                try {
                    pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    if (pm != null) {
                        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "Store3C:ScreenLockNotificationTag");
                        wl.acquire(30000);
                        wl.release();
                    }
                } catch (Exception e) {
                    Log.i("Exception ==> ", e.getClass().toString());
                }
            } else {
                executor.execute(() -> {
                    final Bitmap[] pictureFromExecutor = {getBitmapfromUrl(pic.toString())};

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            String title, message, messagePrice, messageIntro;
                            Bitmap bitmap;
                            PendingIntent pendingIntent;
                            Intent intent;
                            NotificationManager notificationManager;
                            String channelId;
                            Uri defaultSoundUri;
                            Notification notification;
                            int smallIconId;
                            PowerManager pm;
                            PowerManager.WakeLock wl;
                            int notificationId;
                            Random r = new Random();
                            int pendingIntentIndex;
                            TaskStackBuilder stackBuilder;

                            title = remoteMessage.getNotification().getTitle();
                            message = remoteMessage.getNotification().getBody();
                            messagePrice = "100元";
                            messageIntro = "挑選多種蔬菜水果，吐司、熱狗、豬排、煎蛋，豐富的食材，讓人有一種滿足的感覺!";

                            if (pictureFromExecutor[0] == null) {
                                pictureFromExecutor[0] = BitmapFactory.decodeResource(getResources(), R.drawable.store_icon);
                            }
                            intent = new Intent(PromotionFirebaseMessagingService.this, ProductActivity.class);
                            am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
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
                                                appRunningForeground = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (appRunningForeground) {
                                        intent.putExtra("Notification", "IN_APP");
                                        intent.putExtra("RetainRecentTask", "RECENT_ACTIVITY");
                                        intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                                        Log.i("Notification===> ", "fork a new task.  ");
                                    } else {
                                        Log.i("Notification===> ", "System tray send message to here. ");  //It should not happen, notification will send to MainActivity
                                    }
                                } else {
                                    Log.i("Notification===> ", "Non running app.  ");
                                }
                            } else {          // user clear all app in recent screen
                                Log.i("Notification===> ", "System tray send message to here.");  //It should not happen, notification will send to the MainActivity
                            }

                            intent.putExtra("Pic", Bitmap2Bytes(pictureFromExecutor[0]));
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
                                    new NotificationCompat.Builder(PromotionFirebaseMessagingService.this, channelId)
                                            .setSmallIcon(R.drawable.store_icon)
                                            .setLargeIcon(bitmap)
                                            .setContentTitle(title)
                                            .setContentText(message)
                                            .setStyle(new NotificationCompat.BigPictureStyle()
                                                    .setSummaryText(message)
                                                    .bigPicture(pictureFromExecutor[0]))
                                            .setLights(Color.WHITE, 300, 300)
                                            .setPriority(Notification.PRIORITY_MAX)
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
                                        "Store3C onSale Channel",
                                        NotificationManager.IMPORTANCE_HIGH);
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

                            notificationId = new Random().nextInt(60000);
                            if (notificationManager != null) {
                                notificationManager.notify(notificationId /* ID of notification */, notification);
                            }

                            try {
                                pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                                if (pm != null) {
                                    wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "Store3C:ScreenLockNotificationTag");
                                    wl.acquire(30000);
                                    wl.release();
                                }
                            } catch (Exception e) {
                                Log.i("Exception ==> ", e.getClass().toString());
                            }
                        }
                    });
                });
            }
        }
    }

    @Override
    public void handleIntent(@NonNull Intent intent) {
         super.handleIntent(intent);
    }

    @Override
    public synchronized void onNewToken(@NonNull String s) {
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

    @Override
    public void onLowMemory() {
        int memoSize = 0;

        if (dbhelper == null) {
            dbhelper = new AccountDbAdapter(this);
        }
        if (!(dbhelper.IsDbMemoEmpty())) {
            try {
                Cursor cursor = dbhelper.listAllMemo();
                if (cursor.getCount() > 0) {
                    do {
                        memoSize++;
                    } while (cursor.moveToNext());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (dbhelper.createMemo(memoSize, "On low memory", "11") == -1) {
            Log.i("create Memo: ", "fail!");
        }

        super.onLowMemory();
    }

    @Override
    public synchronized void onTrimMemory(int level) {

    }

    public synchronized Bitmap getBitmapfromUrl(String imageUrl) {
        URL url;
        HttpURLConnection connection = null;
        Bitmap image = null;
        int memoSize = 0;
        String Device, Product, Hardware;

        if (dbhelper == null) {
            dbhelper = new AccountDbAdapter(PromotionFirebaseMessagingService.this);
        }
        if (!(dbhelper.IsDbMemoEmpty())) {
            try {
                Cursor cursor = dbhelper.listAllMemo();
                if (cursor.getCount() > 0) {
                    do {
                        memoSize++;
                    } while (cursor.moveToNext());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (InternetConnection.checkConnection(PromotionFirebaseMessagingService.this)) {
            try {
                url = new URL(imageUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setConnectTimeout(300000);
                connection.setReadTimeout(300000);

                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream input = connection.getInputStream();
                    byte[] imageData = new byte[4096];
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    int read;
                    while ((read = input.read(imageData)) != -1) {
                        outputStream.write(imageData, 0, read);
                    }
                    outputStream.close();
                    input.close();
                    byte [] data = outputStream.toByteArray();
                    image  = BitmapFactory.decodeByteArray(data, 0, data.length);
                }
                else {
                    connection.connect();
                    if (dbhelper.createMemo(memoSize++, "HTTP not OK", "1") == -1) {
                        Log.i("create Memo: ", "fail!");
                    }
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream input = connection.getInputStream();
                        byte[] imageData = new byte[4096];
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        int read;
                        while ((read = input.read(imageData)) != -1) {
                            outputStream.write(imageData, 0, read);
                        }
                        outputStream.close();
                        input.close();
                        byte [] data = outputStream.toByteArray();
                        image  = BitmapFactory.decodeByteArray(data, 0, data.length);
                    }
                    else {
                        if (dbhelper.createMemo(memoSize++, "HTTP retry not OK", "1_1") == -1) {
                            Log.i("create Memo: ", "fail!");
                        }
                    }
                }
                if (image == null) {
                    if (dbhelper.createMemo(memoSize++, "image null", "10") == -1) {
                        Log.i("create Memo: ", "fail!");
                    }
                }
                else {
                    if (dbhelper.createMemo(memoSize++, "add image", "1000") == -1) {
                        Log.i("create Memo: ", "fail!");
                    }
                }
            } catch (Exception e) {
                if (dbhelper.createMemo(memoSize++, Arrays.toString(e.getStackTrace()), "100_1") == -1) {
                    Log.i("create Memo: ", "fail!");
                }
                e.printStackTrace();
                boolean connected = false;
                int counter = 0;
                do {
                    counter++;
                    try {
                        if (connection != null) {
                            connection.disconnect();
                        }
                        url = new URL(imageUrl);


                        connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
                        connection.setRequestMethod("GET");
                        connection.setDoInput(true);
                        connection.setConnectTimeout(300000);
                        connection.setReadTimeout(300000);
                        connection.setInstanceFollowRedirects(true);
                        connection.setUseCaches(false);

                        Properties systemProperties = System.getProperties();
                        String agent = systemProperties.getProperty("http.agent");
                        connection.setRequestProperty("User-Agent", agent);
                        //connection.setRequestProperty("Cookie", "*");

                        connection.connect();
                        InputStream input = connection.getInputStream();
                        byte[] imageData = new byte[4096];
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        int read;
                        while ((read = input.read(imageData)) != -1) {
                            outputStream.write(imageData, 0, read);
                        }
                        outputStream.close();
                        input.close();
                        byte [] data = outputStream.toByteArray();
                        image  = BitmapFactory.decodeByteArray(data, 0, data.length);
                        connected = true;
                        if (image == null) {
                            if (dbhelper.createMemo(memoSize++, "image null", "10_1: " + counter) == -1) {
                                Log.i("create Memo: ", "fail!");
                            }
                        } else {
                            if (dbhelper.createMemo(memoSize++, "add image", "1000_1: " + counter) == -1) {
                                Log.i("create Memo: ", "fail!");
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        if (dbhelper.createMemo(memoSize++, ex.getMessage(), "100_2: " + counter) == -1) {
                            Log.i("create Memo: ", "fail!");
                        }
                    }
                } while (!connected && counter < 10);
            }
            finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return image;
        } else {
            Log.i("網路未連線! ", " ==>PromotionFirebaseMessagingService");
            if (dbhelper.createMemo(memoSize++, "No net !", "100") == -1) {
                Log.i("create Memo: ", "fail!");
            }
            return null;
        }
    }

    private synchronized byte [] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos =  new  ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,  100 , baos);
        return  baos.toByteArray();
    }

    public PromotionFirebaseMessagingService() {
        super();
    }

}
