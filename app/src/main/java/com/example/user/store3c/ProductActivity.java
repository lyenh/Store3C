package com.example.user.store3c;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

import static com.example.user.store3c.MainActivity.isTab;
import static com.example.user.store3c.MainActivity.rotationScreenWidth;
import static com.example.user.store3c.MainActivity.rotationTabScreenWidth;

@Keep
public class ProductActivity extends AppCompatActivity implements View.OnClickListener{

    private String menu_item = "DISH", up_menu_item = "", product_name, product_price, product_intro, order_list = "", search_list = "";
    private String notification_list = "", firebaseNotification = "";
    private byte[] product_pic;
    private ActivityManager.AppTask preTask = null;
    private boolean recentTaskProduct = false, firebaseDataPayload = false;
    private AccountDbAdapter dbHelper = null;
    private int DbRecentTaskId = -1, DbMainActivityTaskId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        ImageView pic, buy_b;
        TextView name,price,intro;
        Button ret_b;
        ScrollView introView;
        Bitmap product_img;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setLogo(R.drawable.store_logo);
            getSupportActionBar().setTitle("產品簡介");
        }
        pic = findViewById(R.id.productImg_id);
        name = findViewById(R.id.productName_id);
        price = findViewById(R.id.productPrice_id);
        intro = findViewById(R.id.productIntro_id);
        introView = findViewById(R.id.productIntroView_id);
        ret_b = findViewById(R.id.productReturnBtn_id);
        buy_b = findViewById(R.id.buyBtn_id);

        Intent Intent = getIntent();
        Bundle bundle = Intent.getExtras();
        String retainRecentTask;
        if (bundle != null) {
            notification_list = bundle.getString("Notification");
            retainRecentTask = bundle.getString("RetainRecentTask");
            firebaseNotification = bundle.getString("Firebase");
            if (retainRecentTask != null) {
                if (retainRecentTask.equals("RECENT_ACTIVITY")) {       // productActivity task
                    recentTaskProduct = true;
                }
            }
            if (firebaseNotification != null) {
                if (firebaseNotification.equals("DATA_PAYLOAD")) {       // productActivity task
                    firebaseDataPayload = true;
                }
            }
            if (notification_list != null) {   // notification promotion product
                menu_item = "DISH";
            }
            else {
                if (bundle.getString("Order") != null) {
                    order_list = bundle.getString("Order");
                }
                if (bundle.getString("Search") != null) {
                    search_list = bundle.getString("Search");
                }
                menu_item = bundle.getString("Menu");
                if (bundle.getString("upMenu") != null) {
                    up_menu_item = bundle.getString("upMenu");
                }
            }
            product_pic = bundle.getByteArray("Pic");
            if (product_pic != null) {
                product_img = BitmapFactory.decodeByteArray(product_pic, 0, product_pic.length);
                pic.setImageBitmap(product_img);
            }
            product_name = bundle.getString("Name");
            name.setText(product_name);
            product_price = bundle.getString("Price");
            price.setText(product_price);
            product_intro = bundle.getString("Intro");
        }

        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int imgHeight;
        if (isTab) {
            if (screenWidth > rotationTabScreenWidth) {
                imgHeight = (product_intro.length() / 27 + 1) * 100;
                introView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imgHeight));
            }
        }
        else {
            if (screenWidth > rotationScreenWidth) {
                imgHeight = (product_intro.length() / 27 + 1) * 60;
                introView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imgHeight));
            }
        }

        if (dbHelper == null) {
            dbHelper = new AccountDbAdapter(this);
        }
        if (!dbHelper.IsDbTaskIdEmpty()) {
            try {
                Cursor cursor = dbHelper.getTaskIdList();
                DbRecentTaskId = cursor.getInt(1);
                DbMainActivityTaskId = cursor.getInt(2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        intro.setText(product_intro);
        ret_b.setOnClickListener(this);
        buy_b.setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.productReturnBtn_id:
                onBackPressed();
                break;

            case R.id.buyBtn_id:
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                if (search_list.equals("SEARCH")) {
                    bundle.putString("Search", "SEARCH");
                }
                bundle.putString("Menu", menu_item);
                if (!up_menu_item.equals("")) {
                    bundle.putString("upMenu", up_menu_item);
                }
                if (recentTaskProduct) {
                    bundle.putString("RetainRecentTask", "RECENT_ACTIVITY");
                }
                bundle.putByteArray("Pic", product_pic);
                bundle.putString("Name", product_name);
                bundle.putString("Price", product_price);
                bundle.putString("Intro", product_intro);
                intent.putExtras(bundle);
                Toast.makeText(this, "已加入購物車!", Toast.LENGTH_SHORT).show();
                intent.setClass(ProductActivity.this, OrderActivity.class);

                if (notification_list != null) {
                    ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.AppTask> tasks;
                    ActivityManager.AppTask currentTask = null;
                    synchronized(tasks = am.getAppTasks()) {
                        preTask = null;
                        if (tasks.size() > 1) {
                            for (int i = 0; i < tasks.size(); i++) {
                                if (tasks.get(i) != null && tasks.get(i).getTaskInfo().persistentId == DbMainActivityTaskId && DbMainActivityTaskId != getTaskId()) {
                                    preTask = tasks.get(i);     // Should be the main task
                                    //Toast.makeText(this, "Got main preTask! ", Toast.LENGTH_LONG).show();
                                }
                                if (tasks.get(i) != null && tasks.get(i).getTaskInfo().persistentId == getTaskId()) {
                                    currentTask = tasks.get(i);
                                }
                            }
                            if (preTask == null) {
                                if (MainActivity.taskIdOrderActivity != -1) {
                                    for (int i = 0; i < tasks.size(); i++) {
                                        if (tasks.get(i) != null && tasks.get(i).getTaskInfo().persistentId == MainActivity.taskIdOrderActivity) {
                                            if (tasks.get(i) != null && tasks.get(i).getTaskInfo().persistentId != getTaskId()) {
                                                preTask = tasks.get(i);
                                                //Toast.makeText(this, "Got order preTask! ", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Bundle orderTaskBundle = new Bundle();
                    orderTaskBundle.putString("OrderTask", "ORDER_ACTIVITY");
                    intent.putExtras(orderTaskBundle);
                    if (preTask != null) {
                        Bundle retainRecentTaskBundle = new Bundle();
                        retainRecentTaskBundle.putString("Menu", "DISH");
                        retainRecentTaskBundle.putString("RetainRecentTask", "RECENT_ACTIVITY");
                        intent.putExtras(retainRecentTaskBundle);
                        try {
                            if (preTask.getTaskInfo().persistentId == DbRecentTaskId) {
                                preTask.moveToFront();
                            }
                            preTask.startActivity(this, intent, bundle);
                            //Toast.makeText(this, "startActivity!", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            //Toast.makeText(this, "preTask: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.i("preTask ===>", "no startActivity: " + e.getMessage());
                            intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                            retainRecentTaskBundle = new Bundle();
                            retainRecentTaskBundle.putString("RetainRecentTask", "RECENT_ACTIVITY");
                            intent.putExtras(retainRecentTaskBundle);
                            startActivity(intent);
                            preTask.finishAndRemoveTask();
                        }
                        if (currentTask != null) {
                            currentTask.finishAndRemoveTask();
                        }
                        else {
                            this.finishAndRemoveTask();
                        }
                    }
                    else {
                        //Toast.makeText(this, "No preTask! ", Toast.LENGTH_LONG).show();
                        intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                        Bundle retainRecentTaskBundle = new Bundle();
                        retainRecentTaskBundle.putString("RetainRecentTask", "RECENT_ACTIVITY");
                        retainRecentTaskBundle.putString("Menu", "DISH");
                        intent.putExtras(retainRecentTaskBundle);
                        //Toast.makeText(ProductActivity.this, "Task created by document", Toast.LENGTH_LONG).show();
                        startActivity(intent);
                        finishAndRemoveTask();
                    }
                }
                else {
                    startActivity(intent);
                    ProductActivity.this.finish();
                }
                break;

        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        Bundle bundle;

        if (order_list.equals("ORDER")) {
            bundle = new Bundle();
            bundle.putString("Menu", menu_item);
            if (!up_menu_item.equals("")) {
                bundle.putString("upMenu", up_menu_item);
            }
            if (recentTaskProduct) {
                bundle.putString("RetainRecentTask", "RECENT_ACTIVITY");
            }
            intent.putExtras(bundle);
            intent.setClass(ProductActivity.this, OrderActivity.class);
        }
        else if (search_list.equals("SEARCH")) {
            bundle = new Bundle();
            bundle.putString("Menu", menu_item);
            if (!up_menu_item.equals("")) {
                bundle.putString("upMenu", up_menu_item);
            }
            intent.putExtras(bundle);
            intent.setClass(ProductActivity.this, SearchActivity.class);
        }
        else {
            switch (menu_item) {
                case "DISH":
                    intent.setClass(ProductActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    break;
                case "CAKE":
                    intent.setClass(ProductActivity.this, CakeActivity.class);
                    break;
                case "PHONE":
                    intent.setClass(ProductActivity.this, PhoneActivity.class);
                    break;
                case "CAMERA":
                    intent.setClass(ProductActivity.this, CameraActivity.class);
                    break;
                case "BOOK":
                    intent.setClass(ProductActivity.this, BookActivity.class);
                    break;
                case "MEMO":
                    bundle = new Bundle();
                    bundle.putString("Menu", up_menu_item);
                    intent.putExtras(bundle);
                    intent.setClass(ProductActivity.this, MemoActivity.class);
                    break;
                default:
                    intent.setClass(ProductActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
        }
        if (notification_list != null) {
            ActivityManager.AppTask currentTask = null;
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.AppTask> tasks;
            synchronized(tasks = am.getAppTasks()) {
                preTask = null;
                if (tasks.size() > 1) {
                    for (int i = 0; i < tasks.size(); i++) {
                        if (tasks.get(i) != null && tasks.get(i).getTaskInfo().persistentId == DbMainActivityTaskId && DbMainActivityTaskId != getTaskId()) {
                            preTask = tasks.get(i);     // do getAppTasks again, it should be the main task
                        }
                        if (tasks.get(i) != null && tasks.get(i).getTaskInfo().persistentId == getTaskId()) {
                            currentTask = tasks.get(i);
                        }
                    }
                    if (preTask == null) {
                        if (MainActivity.taskIdOrderActivity != -1) {
                            for (int i = 0; i < tasks.size(); i++) {
                                if (tasks.get(i) != null && tasks.get(i).getTaskInfo().persistentId == MainActivity.taskIdOrderActivity) {
                                    if (tasks.get(i) != null && tasks.get(i).getTaskInfo().persistentId != getTaskId()) {
                                        preTask = tasks.get(i);
                                        //Toast.makeText(this, "Got order preTask! ", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }
                    }
                    if (preTask == null) {
                        preTask = tasks.get(tasks.size() - 1);
                        Log.i("Task Id ===>", "MainActivity is not loaded  then go to another loaded activity.");
                    }
                }
            }
            if (preTask != null) {
                try {
                    preTask.moveToFront();
                    intent.replaceExtras(new Bundle());
                    intent.setAction("");
                    intent.setData(null);
                    intent.setFlags(0);
                    if (currentTask != null) {
                        currentTask.finishAndRemoveTask();
                    }
                    else {
                        this.finishAndRemoveTask();
                    }
                }catch (Exception e) {      // prevent the system drop the preTask
                    synchronized(tasks = am.getAppTasks()) {
                        preTask = null;
                        currentTask = null;
                        if (tasks.size() > 1) {
                            for (int i = 0; i < tasks.size(); i++) {
                                if (tasks.get(i) != null && tasks.get(i).getTaskInfo().persistentId == DbMainActivityTaskId && DbMainActivityTaskId != getTaskId()) {
                                    preTask = tasks.get(i);     // Should be the main task
                                }
                                if (tasks.get(i) != null && tasks.get(i).getTaskInfo().persistentId == getTaskId()) {
                                    currentTask = tasks.get(i);
                                }
                            }
                            if (preTask == null) {
                                if (MainActivity.taskIdOrderActivity != -1) {
                                    for (int i = 0; i < tasks.size(); i++) {
                                        if (tasks.get(i) != null && tasks.get(i).getTaskInfo().persistentId == MainActivity.taskIdOrderActivity) {
                                            if (tasks.get(i) != null && tasks.get(i).getTaskInfo().persistentId != getTaskId()) {
                                                preTask = tasks.get(i);
                                                //Toast.makeText(this, "Got order preTask! ", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }
                                }
                            }
                            if (preTask == null) {
                                preTask = tasks.get(tasks.size() - 1);
                                Log.i("Task Id ===>", "MainActivity is not loaded  then go to another loaded activity.");
                            }
                            preTask.moveToFront();
                            intent.replaceExtras(new Bundle());
                            intent.setAction("");
                            intent.setData(null);
                            intent.setFlags(0);
                            if (currentTask != null) {
                                currentTask.finishAndRemoveTask();
                            } else {
                                this.finishAndRemoveTask();
                            }
                        } else {
                            if (firebaseDataPayload) {
                                intent.setFlags(0);
                                if (recentTaskProduct) {
                                    intent = Intent.makeRestartActivityTask(new ComponentName(getApplicationContext(), MainActivity.class));
                                    startActivity(intent);
                                    ProductActivity.this.finish();
                                } else {
                                    intent = Intent.makeMainActivity(new ComponentName(getApplicationContext(), MainActivity.class));
                                    intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS);
                                    Bundle retainRecentTaskBundle = new Bundle();
                                    retainRecentTaskBundle.putString("RetainRecentTask", "RECENT_TASK");
                                    intent.putExtras(retainRecentTaskBundle);
                                    startActivity(intent);
                                    tasks.get(0).finishAndRemoveTask();
                                }
                            } else {
                                if (recentTaskProduct) {
                                    Bundle retainRecentTaskBundle = new Bundle();
                                    retainRecentTaskBundle.putString("RetainRecentTask", "RECENT_TASK");
                                    intent.putExtras(retainRecentTaskBundle);
                                }
                                startActivity(intent);
                                ProductActivity.this.finish();
                            }
                        }
                    }
                }
            }
            else {
                Log.i("PreTask===> ", "null !");        //default value, have only one task
                 if (firebaseDataPayload) {
                     intent.setFlags(0);
                    if (recentTaskProduct) {
                        intent = Intent.makeRestartActivityTask (new ComponentName(getApplicationContext(), MainActivity.class));
                        startActivity(intent);
                        ProductActivity.this.finish();
                    }
                    else {
                        intent = Intent.makeMainActivity (new ComponentName(getApplicationContext(), MainActivity.class));
                        intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS);
                        Bundle retainRecentTaskBundle = new Bundle();
                        retainRecentTaskBundle.putString("RetainRecentTask", "RECENT_TASK");
                        intent.putExtras(retainRecentTaskBundle);
                        startActivity(intent);
                        tasks.get(0).finishAndRemoveTask();
                    }
                } else {
                     if (recentTaskProduct) {
                         Bundle retainRecentTaskBundle = new Bundle();
                         retainRecentTaskBundle.putString("RetainRecentTask", "RECENT_TASK");
                         intent.putExtras(retainRecentTaskBundle);
                     }
                    startActivity(intent);
                    ProductActivity.this.finish();
                }
            }
        }
        else {
            startActivity(intent);
            ProductActivity.this.finish();
        }
    }

}
