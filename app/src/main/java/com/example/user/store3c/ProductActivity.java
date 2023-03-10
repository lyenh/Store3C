package com.example.user.store3c;

import android.app.ActivityManager;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.Objects;

import static android.content.Intent.ACTION_MAIN;
import static android.content.Intent.CATEGORY_LAUNCHER;
import static com.example.user.store3c.MainActivity.isTab;
import static com.example.user.store3c.MainActivity.rotationScreenWidth;
import static com.example.user.store3c.MainActivity.rotationTabScreenWidth;
import static com.example.user.store3c.MainActivity.taskIdMainActivity;

public class ProductActivity extends AppCompatActivity implements View.OnClickListener{

    private String menu_item = "DISH", up_menu_item = "", product_name, product_price, product_intro, order_list = "", search_list = "";
    private String notification_list = "", firebase_message = "";
    private byte[] product_pic;
    private ActivityManager.AppTask preTask = null;

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
        if (bundle != null) {
            notification_list = bundle.getString("Notification");
            firebase_message = bundle.getString("Firebase");
            if (notification_list != null || firebase_message != null) {   // notification promotion product
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
        intro.setText(product_intro);
        ret_b.setOnClickListener(this);
        buy_b.setOnClickListener(this);

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
                bundle.putByteArray("Pic", product_pic);
                bundle.putString("Name", product_name);
                bundle.putString("Price", product_price);
                bundle.putString("Intro", product_intro);
                intent.putExtras(bundle);
                Toast.makeText(this, "已加入購物車!", Toast.LENGTH_SHORT).show();

                intent.setClass(ProductActivity.this, OrderActivity.class);

                if (notification_list != null) {
                    ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.AppTask> tasks = am.getAppTasks();
                    preTask = null;
                    if (tasks.size() > 1) {
                        for (int i = 0; i < tasks.size(); i++) {
                            if ( tasks.get(i).getTaskInfo().persistentId == taskIdMainActivity) {
                                    preTask = tasks.get(i);     // Should be the main task
                            }
                        }
                        if (preTask == null) {
                            preTask = tasks.get(tasks.size()-1);
                            Log.i("Task Id ===>", "MainActivity is not loaded  then go to another loaded activity.");
                        }
                    }
                    if (preTask != null) {
                        intent.setFlags(0);
                        Bundle retainRecentTaskBundle = new Bundle();
                        retainRecentTaskBundle.putString("RetainRecentTask", "RECENT_ACTIVITY");
                        intent.putExtras(retainRecentTaskBundle);
                        intent.setFlags( Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        preTask.startActivity(this, intent, bundle);
                        finishAndRemoveTask();
                    }
                    else {
                        intent.setFlags(0);
                        intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                        if (intent.getPackage() == null) {
                            Bundle retainRecentTaskBundle = new Bundle();
                            retainRecentTaskBundle.putString("RetainRecentTask", "RECENT_ACTIVITY");
                            intent.putExtras(retainRecentTaskBundle);
                            Toast.makeText(ProductActivity.this, "Task created by document", Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(ProductActivity.this, "Task created by system", Toast.LENGTH_LONG).show();
                        }
                        startActivity(intent);
                        finishAndRemoveTask();
                    }
                }
                else if (firebase_message != null) {
                    if (firebase_message.equals("MESSAGE")) {
                        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                        List<ActivityManager.AppTask> tasks = am.getAppTasks();
                        preTask = null;
                        if (tasks.size() > 1) {
                            for (int i = 0; i < tasks.size(); i++) {
                                if ( tasks.get(i).getTaskInfo().persistentId == taskIdMainActivity) {
                                    preTask = tasks.get(i);     // do getAppTasks again, it should be the main task
                                }
                            }
                            if (preTask == null) {
                                preTask = tasks.get(tasks.size()-1);
                                Log.i("Task Id ===>", "MainActivity is not loaded  then go to another loaded activity.");
                            }
                        }
                        if (preTask != null) {
                            intent.setFlags( Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                            Bundle retainRecentTaskBundle = new Bundle();
                            retainRecentTaskBundle.putString("RetainRecentTask", "RECENT_ACTIVITY");
                            intent.putExtras(retainRecentTaskBundle);
                            preTask.startActivity(this, intent, bundle);
                            finishAndRemoveTask();
                        }
                        else {
                            intent.setFlags(0);
                            Bundle retainRecentTaskBundle = new Bundle();
                            retainRecentTaskBundle.putString("RetainRecentTask", "RECENT_ACTIVITY");
                            intent.putExtras(retainRecentTaskBundle);
                            intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                            startActivity(intent);
                            finishAndRemoveTask();
                        }
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
                    intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
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
                    intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            }
        }
        if (notification_list != null) {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.AppTask> tasks = am.getAppTasks();
            preTask = null;
            if (tasks.size() > 1) {
                for (int i = 0; i < tasks.size(); i++) {
                    if ( tasks.get(i).getTaskInfo().persistentId == taskIdMainActivity) {
                        preTask = tasks.get(i);     // do getAppTasks again, it should be the main task
                    }
                }
                if (preTask == null) {
                    preTask = tasks.get(tasks.size()-1);
                    Log.i("Task Id ===>", "MainActivity is not loaded  then go to another loaded activity.");
                }
            }
            if (preTask != null) {
                try {
                    preTask.moveToFront();
                    intent.replaceExtras(new Bundle());
                    intent.setAction("");
                    intent.setData(null);
                    intent.setFlags(0);
                    finishAndRemoveTask();
                }catch (Exception e) {      // user has removed the task from the recent screen (task)
                    tasks = am.getAppTasks();
                    preTask = null;
                    if (tasks.size() > 1) {
                        for (int i = 0; i < tasks.size(); i++) {
                            if ( tasks.get(i).getTaskInfo().persistentId == taskIdMainActivity) {
                                preTask = tasks.get(i);     // Should be the main task
                            }
                        }
                        if (preTask == null) {
                            preTask = tasks.get(tasks.size()-1);
                            Log.i("Task Id ===>", "MainActivity is not loaded  then go to another loaded activity.");
                        }
                        preTask.moveToFront();
                        intent.replaceExtras(new Bundle());
                        intent.setAction("");
                        intent.setData(null);
                        intent.setFlags(0);
                        finishAndRemoveTask();
                    }
                    else {
                        intent.setFlags(0);
                        intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS);
                        Bundle retainRecentTaskBundle = new Bundle();
                        retainRecentTaskBundle.putString("RetainRecentTask", "RECENT_TASK");
                        intent.putExtras(retainRecentTaskBundle);
                        startActivity(intent);
                        finishAndRemoveTask();
                    }
                }
            }
            else {
                Log.i("PreTask===> ", "null !");        //default value, have only one task
                intent.setFlags(0);
                intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS);
                Bundle retainRecentTaskBundle = new Bundle();
                retainRecentTaskBundle.putString("RetainRecentTask", "RECENT_TASK");
                intent.putExtras(retainRecentTaskBundle);
                startActivity(intent);
                finishAndRemoveTask();
            }
        }
        else if (firebase_message != null) {
            if (firebase_message.equals("MESSAGE")) {     //      from firebase notification message
                intent.setClass(ProductActivity.this, MainActivity.class);
                bundle = intent.getExtras();
                if (bundle != null) {
                    bundle.clear();
                    intent.putExtras(bundle);
                }

                ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.AppTask> tasks = am.getAppTasks();
                preTask = null;
                if (tasks.size() > 1) {
                    for (int i = 0; i < tasks.size(); i++) {
                        if ( tasks.get(i).getTaskInfo().persistentId == taskIdMainActivity) {
                            preTask = tasks.get(i);     // do getAppTasks again, it should be the main task
                        }
                    }
                    if (preTask == null) {
                        preTask = tasks.get(tasks.size()-1);
                        Log.i("Task Id ===>", "MainActivity is not loaded  then go to another loaded activity.");
                    }
                }
                if (preTask != null) {
                    try {
                        preTask.moveToFront();
                        intent.replaceExtras(new Bundle());
                        intent.setAction("");
                        intent.setData(null);
                        intent.setFlags(0);
                        finishAndRemoveTask();
                    }catch (Exception e) {      // user has removed the task from the recent screen (task)
                        tasks = am.getAppTasks();
                        if (tasks.size() > 1) {
                            preTask = null;
                            for (int i = 0; i < tasks.size(); i++) {
                                if ( tasks.get(i).getTaskInfo().persistentId == taskIdMainActivity) {
                                    preTask = tasks.get(i);     // Should be the main task
                                }
                            }
                            if (preTask == null) {
                                preTask = tasks.get(tasks.size()-1);
                                Log.i("Task Id ===>", "MainActivity is not loaded  then go to another loaded activity.");
                            }
                            preTask.moveToFront();
                            intent.replaceExtras(new Bundle());
                            intent.setAction("");
                            intent.setData(null);
                            intent.setFlags(0);
                            finishAndRemoveTask();
                        }
                        else {
                            intent = Intent.makeRestartActivityTask (new ComponentName(getApplicationContext(), MainActivity.class));
                            Bundle retainRecentTaskBundle = new Bundle();
                            retainRecentTaskBundle.putString("RetainRecentTask", "RECENT_TASK");
                            intent.putExtras(retainRecentTaskBundle);
                            intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS);
                            startActivity(intent);
                            finishAndRemoveTask();
                        }
                    }
                }
                else {
                    intent = Intent.makeRestartActivityTask (new ComponentName(getApplicationContext(), MainActivity.class));
                    Bundle retainRecentTaskBundle = new Bundle();
                    retainRecentTaskBundle.putString("RetainRecentTask", "RECENT_TASK");
                    intent.putExtras(retainRecentTaskBundle);
                    Log.i("Package list:  ", "===> " + intent.getPackage());
                    Log.i("Category list:  ", "===> " + intent.getCategories());
                    Log.i("Action list:  ", "===> " + intent.getAction());
                    intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS);
                    startActivity(intent);
                    finishAndRemoveTask();
                }
            }
        }
        else {
            startActivity(intent);
            ProductActivity.this.finish();
        }

    }

}
